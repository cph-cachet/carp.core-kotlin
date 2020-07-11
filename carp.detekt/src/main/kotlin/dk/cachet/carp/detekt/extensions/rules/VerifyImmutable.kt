package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.source.getPsi


class VerifyImmutable( private val immutableAnnotation: String ) : Rule()
{
    override val issue: Issue = Issue(
        javaClass.simpleName,
        Severity.Defect,
        "Classes or classes extending from classes with an @Immutable annotation applied to them should be data classes, " +
        "may not contain mutable properties, and may only contain basic types and other Immutable properties.",
        Debt.TWENTY_MINS
    )


    override fun visitClassOrObject( classOrObject: KtClassOrObject )
    {
        with ( ImmutableVisitor( bindingContext ) )
        {
            classOrObject.accept( this )
            if ( shouldBeImmutable )
            {
                classOrObject.accept( ImmutableImplementationVisitor() )
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
            super.visitClassOrObject( classOrObject )

            // Verify whether the immutable annotation is applied to the base class.
            // TODO: Use a fully qualified annotation class?
            shouldBeImmutable = classOrObject.annotationEntries
                .any { it.shortName.toString() == immutableAnnotation }

            // Recursively verify whether the next base class might have the immutable annotation.
            if ( !shouldBeImmutable )
            {
                classOrObject.superTypeListEntries
                    .map { it.typeAsUserType?.referenceExpression?.getResolvedCall( bindingContext )?.resultingDescriptor }
                    .filterIsInstance<ClassConstructorDescriptor>()
                    .map { it.constructedClass.source.getPsi() as KtClassOrObject }
                    .forEach { it.accept( this ) }
            }
        }
    }

    /**
     * Determines for a class which needs to be immutable whether the implementation is immutable.
     */
    internal inner class ImmutableImplementationVisitor : DetektVisitor()
    {
        override fun visitClassOrObject( classOrObject: KtClassOrObject )
        {
            val klass = classOrObject as? KtClass
            if ( klass != null )
            {
                // Final immutable classes need to be data classes. It does not make sense NOT to make them data classes.
                if ( !klass.isAbstract() && !klass.isData() )
                {
                    val message = "Immutable types need to be data classes."
                    report( CodeSmell( issue, Entity.from( klass ), message ) )
                    return
                }
            }

            super.visitClassOrObject( classOrObject )
        }

        override fun visitPrimaryConstructor( constructor: KtPrimaryConstructor )
        {
            val properties = constructor.valueParameters
                .filter { it.valOrVarKeyword != null }

            // Verify whether any properties in the constructor are defined as var.
            if ( properties.any { it.isMutable } )
            {
                val message = "Immutable types may not contain var constructor parameters."
                report( CodeSmell( issue, Entity.from( constructor ), message ) )
            }

            // TODO: Verify whether any of the property types in the constructor are not immutable.

            super.visitPrimaryConstructor( constructor )
        }

        override fun visitProperty( property: KtProperty )
        {
            // Verify whether the property is defined as var.
            if ( property.isVar )
            {
                val message = "Immutable types may not contain var properties."
                report( CodeSmell( issue, Entity.from( property ), message ) )
            }

            // TODO: Verify whether the property type is immutable.

            super.visitProperty( property )
        }
    }
}
