package dk.cachet.carp.detekt.extensions.rules

import dk.cachet.carp.detekt.extensions.areAligned
import dk.cachet.carp.detekt.extensions.getPrecedingElement
import dk.cachet.carp.detekt.extensions.isDefinedOnOneLine
import dk.cachet.carp.detekt.extensions.startsOnNewLine
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtReturnExpression


/**
 * A rule which verifies whether curly braces of blocks are placed on separate lines (except for function literals),
 * aligned with the start of the definition the block is associated with,
 */
class CurlyBracesOnSeparateLine : Rule()
{
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Curly braces of blocks need to be placed on separate lines (except for function literals), aligned with the start of the definition the block is associated with.",
        Debt.FIVE_MINS
    )

    override fun visitBlockExpression( expression: KtBlockExpression )
    {
        super.visitBlockExpression( expression )

        // This rule should not verify function literal blocks.
        if ( expression.parent is KtFunctionLiteral ) return

        visitBlock( expression )
    }

    override fun visitClassBody( classBody: KtClassBody )
    {
        super.visitClassBody( classBody )

        visitBlock( classBody )
    }

    private fun visitBlock( element: KtElement )
    {
        val node = element.node

        // Do not report blocks which are fully defined on one line.
        if ( isDefinedOnOneLine( node ) ) return

        // Determine what should be considered the 'parent' to align with, i.e., 'return' or 'if.
        var parent = element.parent
        val spacing = getPrecedingElement( parent )
        if ( spacing is PsiWhiteSpace )
        {
            var precedingParent = getPrecedingElement( spacing )?.parent
            if ( precedingParent is KtReturnExpression || precedingParent is KtIfExpression )
            {
                parent = precedingParent
            }
        }

        // Multi-line blocks require curly braces on separate lines, aligned with each other and with the parent.
        val leftBrace = node.firstChildNode
        val rightBrace = node.lastChildNode
        val bracesAligned = areAligned( leftBrace, rightBrace )
        val blockOpensOnNewLine = startsOnNewLine( leftBrace )
        if ( !bracesAligned || !blockOpensOnNewLine || !areAligned( parent.node, leftBrace ) )
        {
            report( CodeSmell( issue, Entity.from( element ), issue.description ) )
        }
    }
}
