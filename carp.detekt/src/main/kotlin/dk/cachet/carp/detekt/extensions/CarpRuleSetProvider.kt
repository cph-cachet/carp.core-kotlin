package dk.cachet.carp.detekt.extensions

import dk.cachet.carp.detekt.extensions.rules.CurlyBracesOnSeparateLine
import dk.cachet.carp.detekt.extensions.rules.SpacingInParentheses
import dk.cachet.carp.detekt.extensions.rules.VerifyDataClass
import dk.cachet.carp.detekt.extensions.rules.VerifyImmutable
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider


class CarpRuleSetProvider : RuleSetProvider
{
    override val ruleSetId: String = "carp"

    override fun instance( config: Config ): RuleSet =
        RuleSet(
            ruleSetId,
            listOf(
                SpacingInParentheses( config ),
                CurlyBracesOnSeparateLine( config ),
                VerifyDataClass( "dk.cachet.carp.common.ImplementAsDataClass", config ),
                VerifyImmutable( "dk.cachet.carp.common.ImmutableCheck", config )
            )
        )
}
