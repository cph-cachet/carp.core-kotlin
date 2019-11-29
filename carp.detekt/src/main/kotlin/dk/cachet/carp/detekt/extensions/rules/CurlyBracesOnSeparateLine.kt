package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity


/**
 * A rule which verifies whether curly braces around code blocks are placed on separate lines.
 */
class CurlyBracesOnSeparateLine : Rule()
{
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Curly braces around code blocks need to be placed on separate lines.",
        Debt.FIVE_MINS
    )
}