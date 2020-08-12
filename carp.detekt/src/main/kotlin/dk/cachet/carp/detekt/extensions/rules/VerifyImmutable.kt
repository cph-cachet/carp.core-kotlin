package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithSource
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.KtTypeElement
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.getReferenceTargets
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.source.getPsi


// TODO: In case annotation class cannot be found in bindingContext, report an error.
class VerifyImmutable( private val immutableAnnotation: String, config: Config = Config.empty ) : Rule( config )
{
    companion object
    {
        private val immutableBaseTypes = listOf(
            Boolean::class, Byte::class, Double::class, Float::class, Int::class, Long::class, Short::class,
            String::class, Unit::class )
            .map { it.qualifiedName!! }
        private val immutableCollections = listOf( Map::class, Set::class, List::class )
            .map { it.qualifiedName!! }
    }


    override val issue: Issue = Issue(
        javaClass.simpleName,
        Severity.Defect,
        "Classes or classes extending from classes with an @Immutable annotation may not " +
        "contain mutable properties or properties of mutable types.",
        Debt.TWENTY_MINS
    )

    // TODO: Cache already linted types as mutable or immutable.
    private val isTypeImmutableCache: MutableMap<String, Boolean>

    init {
        val configuredImmutableTypes: List<String> = valueOrDefault( "assumeImmutable", emptyList() )

        isTypeImmutableCache =
            immutableBaseTypes.map { it to true }
            .plus( configuredImmutableTypes.map { it to true } )
            .toMap().toMutableMap()
    }

    override fun visitClassOrObject( classOrObject: KtClassOrObject )
    {
        val immutableVisitor = ImmutableVisitor( bindingContext )
        classOrObject.accept( immutableVisitor )
        if ( immutableVisitor.shouldBeImmutable )
        {
            val implementationVisitor = ImmutableImplementationVisitor( bindingContext )
            classOrObject.accept( implementationVisitor )
            if ( !implementationVisitor.isImmutable )
            {
                implementationVisitor.mutableEntities.forEach {
                    val message = "`${classOrObject.name}` is not immutable due to: ${it.second}"
                    report( CodeSmell( issue, it.first, message ) )
                }
            }
        }

        super.visitClassOrObject( classOrObject )
    }


    /**
     * Determines whether or not the class needs to be immutable.
     */
    internal inner class ImmutableVisitor( private val bindingContext: BindingContext ) : DetektVisitor()
    {
        var shouldBeImmutable: Boolean = false
            private set


        override fun visitClassOrObject( classOrObject: KtClassOrObject )
        {
            // Verify whether the immutable annotation is applied to the base class.
            shouldBeImmutable = classOrObject.annotationEntries
                .any {
                    val type = it.typeReference?.typeElement as KtUserType
                    val name = type.referenceExpression
                        ?.getReferenceTargets( bindingContext )
                        ?.filterIsInstance<ClassConstructorDescriptor>()?.firstOrNull()
                        ?.constructedClass?.fqNameSafe?.asString()
                    name == immutableAnnotation
                }

            // Recursively verify whether the next base class might have the immutable annotation.
            if ( !shouldBeImmutable )
            {
                classOrObject.superTypeListEntries
                    .map { it.typeAsUserType?.referenceExpression?.getResolvedCall( bindingContext )?.resultingDescriptor }
                    .filterIsInstance<ClassConstructorDescriptor>()
                    .mapNotNull {
                        val superTypeConstructor = it.constructedClass.source.getPsi() as KtClassOrObject?
                        if ( superTypeConstructor == null )
                        {
                            val cantAnalyze = Issue( issue.id, Severity.Warning, issue.description, Debt.FIVE_MINS )
                            val message = "Cannot verify whether base class `${it.constructedClass.name}` should be immutable since the source is not loaded."
                            report( CodeSmell( cantAnalyze, Entity.from( classOrObject ), message ) )
                        }
                        superTypeConstructor
                    }
                    .forEach { it.accept( this ) }
            }
        }
    }

    /**
     * Determines for a class which needs to be immutable whether the implementation is immutable.
     */
    internal inner class ImmutableImplementationVisitor( private val bindingContext: BindingContext ) : DetektVisitor()
    {
        private val _mutableEntities: MutableList<Pair<Entity, String>> = mutableListOf()
        val mutableEntities: List<Pair<Entity, String>> = _mutableEntities

        val isImmutable: Boolean get() = _mutableEntities.isEmpty()
        var isVisitingInner: Boolean = false


        override fun visitClassOrObject( classOrObject: KtClassOrObject )
        {
            // Do not visit inner classes within the original one that is visited; they are analyzed separately.
            if ( isVisitingInner ) return
            isVisitingInner = true

            super.visitClassOrObject( classOrObject )
        }

        override fun visitPrimaryConstructor( constructor: KtPrimaryConstructor )
        {
            val properties = constructor.valueParameters
                .filter { it.valOrVarKeyword != null }

            // Verify whether any properties in the constructor are defined as var.
            if ( properties.any { it.isMutable } )
            {
                _mutableEntities.add(
                    Entity.from( constructor ) to
                    "Immutable types may not contain var constructor parameters." )
            }

            // Verify whether any of the property types in the constructor are not immutable.
            for ( property in properties )
            {
                val userType = property.typeReference?.typeElement as KtTypeElement
                verifyType( userType, property )
            }

            super.visitPrimaryConstructor( constructor )
        }

        override fun visitProperty( property: KtProperty )
        {
            // Only verify class members.
            if ( !property.isMember ) return

            // Verify whether the property is defined as var.
            if ( property.isVar )
            {
                _mutableEntities.add(
                    Entity.from( property ) to
                    "Immutable types may not contain var properties." )
            }

            // Verify whether the property type is immutable.
            val userType = property.typeReference?.typeElement as KtUserType?
            if ( userType == null )
            {
                _mutableEntities.add(
                    Entity.from( property ) to
                    "Could not verify whether property type is immutable since type inference is used. Specify type explicitly."
                )
            }
            else verifyType( userType, property )

            super.visitProperty( property )
        }

        private fun verifyType( type: KtTypeElement, locationUsed: PsiElement )
        {
            val descriptor = getDescriptor( type )
            val klazz = descriptor?.let { getKlazz( it ) }
            val name = descriptor?.fqNameSafe?.asString() ?: type.text

            // Early out in case this type is a known immutable type.
            // TODO: The plan is to also store known mutable types; deal with this.
            if ( name in isTypeImmutableCache ) return

            // For immutable collections, the types stored within the collection need to be checked.
            // TODO: Can this be generalized to generics?
            if ( name in immutableCollections )
            {
                // TODO: Implement.
                return
            }
            // Verify a simple type.
            else
            {
                // In case the type name is not known and source cannot be verified, report.
                if ( klazz == null )
                {
                    _mutableEntities.add(
                        Entity.from( locationUsed ) to
                        "Could not verify whether property of type `$name` is immutable." )
                }
                // Recursively verify the type is immutable.
                else
                {
                    val isImmutableVisitor = ImmutableImplementationVisitor( bindingContext )
                    klazz.accept( isImmutableVisitor )
                    if ( !isImmutableVisitor.isImmutable )
                    {
                        _mutableEntities.add(
                            Entity.from( locationUsed ) to
                            "Type `$name` is not immutable." )
                    }
                }
            }
        }

        private fun getDescriptor( type: KtTypeElement ): DeclarationDescriptorWithSource?
        {
            return when ( type )
            {
                is KtUserType ->
                    type.referenceExpression
                        // TODO: What if there are more reference targets?
                        ?.getReferenceTargets( bindingContext )?.firstOrNull() as DeclarationDescriptorWithSource?
                is KtNullableType -> getDescriptor( type.innerType!! )
                else -> throw UnsupportedOperationException( "VerifyImmutable does not support `getDescriptor` for `$type`.")
            }
        }

        private fun getKlazz( descriptor: DeclarationDescriptorWithSource ): KtClassOrObject?
        {
            return when ( val sourceElement = descriptor.source.getPsi() )
            {
                null -> null
                is KtClassOrObject -> sourceElement
                is KtTypeAlias ->
                {
                    val aliasedType = sourceElement.getTypeReference()?.typeElement as KtUserType
                    val aliasedTypeDescriptor = getDescriptor( aliasedType )
                    aliasedTypeDescriptor?.let { getKlazz( it ) }
                }
                else -> throw UnsupportedOperationException( "VerifyImmutable does not support `getKlazz` for `$sourceElement`." )
            }
        }
    }
}
