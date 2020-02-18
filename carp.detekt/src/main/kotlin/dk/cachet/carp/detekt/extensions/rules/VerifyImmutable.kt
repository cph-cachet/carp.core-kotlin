package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.isAbstract


class VerifyImmutable( private val immutableAnnotation: String ) : Rule()
{
    override val issue: Issue = Issue(
        javaClass.simpleName,
        Severity.Defect,
        "Classes or classes extending from classes with an @VerifyImmutable annotation applied to them should be data classes, " +
        "may not contain mutable properties, and may only contain basic types and other Immutable properties.",
        Debt.TWENTY_MINS
    )

    override fun visitClass( klass: KtClass )
    {
        super.visitClass( klass )

        // TODO: How to navigate to the super type source code? If the supertype needs to be immutable, perform the check.

        // Only analyze classes marked as immutable.
        val hasImmutableAnnotation = klass.annotationEntries.any { it.shortName.toString() == immutableAnnotation }
        if ( !hasImmutableAnnotation ) return

        // Final immutable classes need to be data classes. It does not make sense NOT to make them data classes.
        if ( !klass.isAbstract() && !klass.isData() )
        {
            val message = "Immutable types need to be data classes."
            report( CodeSmell( issue, Entity.from( klass ), message ) )
            return
        }
    }
}
