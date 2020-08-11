package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.getReferenceTargets
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.source.getPsi


// TODO: In case annotation class cannot be found in bindingContext, report an error.
class VerifyDataClass( private val verifyDataClassAnnotation: String, config: Config = Config.empty ) : Rule( config )
{
    override val issue: Issue = Issue(
        javaClass.simpleName,
        Severity.Defect,
        "Classes extending from classes with an @ImplementAsDataClass annotation applied to them should be data classes.",
        Debt.TWENTY_MINS
    )

    override fun visitClassOrObject( classOrObject: KtClassOrObject )
    {
        val dataClassVisitor = DataClassVisitor( bindingContext )
        classOrObject.accept( dataClassVisitor )

        if ( dataClassVisitor.shouldBeDataClass )
        {
            val klass = classOrObject as? KtClass
            if ( klass != null )
            {
                val isAbstract = klass.isAbstract() || klass.isSealed()
                if ( !isAbstract && !klass.isData() )
                {
                    val message = "`${classOrObject.name}` should be a data class."
                    report( CodeSmell( issue, Entity.from( classOrObject ), message ) )
                }
            }
        }

        super.visitClassOrObject( classOrObject )
    }

    /**
     * Determines whether or not the class needs to be a data class.
     *
     * TODO: This is a copy/paste from the visitor in `VerifyImmutable`. Extract what is common.
     */
    internal inner class DataClassVisitor( private val bindingContext: BindingContext ) : DetektVisitor()
    {
        var shouldBeDataClass: Boolean = false
            private set


        override fun visitClassOrObject( classOrObject: KtClassOrObject )
        {
            // Verify whether the annotation requiring a data class implementation is applied to the base class.
            shouldBeDataClass = classOrObject.annotationEntries
                .any {
                    val type = it.typeReference?.typeElement as KtUserType
                    val name = type.referenceExpression
                        ?.getReferenceTargets( bindingContext )
                        ?.filterIsInstance<ClassConstructorDescriptor>()?.firstOrNull()
                        ?.constructedClass?.fqNameSafe?.asString()
                    name == verifyDataClassAnnotation
                }

            // Recursively verify whether the next base class might have the annotation.
            if ( !shouldBeDataClass )
            {
                classOrObject.superTypeListEntries
                    .map { it.typeAsUserType?.referenceExpression?.getResolvedCall( bindingContext )?.resultingDescriptor }
                    .filterIsInstance<ClassConstructorDescriptor>()
                    .mapNotNull {
                        val superTypeConstructor = it.constructedClass.source.getPsi() as KtClassOrObject?
                        if ( superTypeConstructor == null )
                        {
                            val cantAnalyze = Issue( issue.id, Severity.Warning, issue.description, Debt.FIVE_MINS )
                            val message = "Cannot verify whether base class `${it.constructedClass.name}` should be a data class since the source is not loaded."
                            report( CodeSmell( cantAnalyze, Entity.from( classOrObject ), message ) )
                        }
                        superTypeConstructor
                    }
                    .forEach { it.accept( this ) }
            }
        }
    }
}
