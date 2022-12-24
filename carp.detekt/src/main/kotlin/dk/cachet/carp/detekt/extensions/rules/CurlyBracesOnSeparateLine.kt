package dk.cachet.carp.detekt.extensions.rules

import dk.cachet.carp.detekt.extensions.areAligned
import dk.cachet.carp.detekt.extensions.getPrecedingElement
import dk.cachet.carp.detekt.extensions.isDefinedOnOneLine
import dk.cachet.carp.detekt.extensions.startsOnNewLine
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtReturnExpression


/**
 * A rule which verifies whether curly braces of blocks are placed on separate lines (except for trailing lambda arguments),
 * aligned with the start of the definition the block is associated with (e.g., class, function, object literal, or return).
 */
class CurlyBracesOnSeparateLine( config: Config = Config.empty ) : Rule( config )
{
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Curly braces of blocks need to be placed on separate lines (except for trailing lambda arguments), " +
        "aligned with the start of the definition the block is associated with " +
        "(e.g., class, function, object literal, or return).",
        Debt.FIVE_MINS
    )

    override fun visitBlockExpression( expression: KtBlockExpression )
    {
        super.visitBlockExpression( expression )

        // The function literal block visitor does not include the braces. It is defined in its parent instead.
        // Therefore, in case the block is part of a function literal, visit the parent instead.
        val toVisit: KtElement =
            if ( expression.parent is KtFunctionLiteral ) expression.parent as KtFunctionLiteral
            else expression

        // Lambda arguments require the first brace to be on the same line, so ignore them.
        // Visit all other blocks.
        if ( toVisit.parent?.parent !is KtLambdaArgument )
        {
            visitBlock( toVisit )
        }
    }

    override fun visitClassBody( classBody: KtClassBody )
    {
        super.visitClassBody( classBody )

        visitBlock( classBody )
    }

    private fun visitBlock( element: KtElement )
    {
        // Do not report blocks which are fully defined on one line.
        if ( isDefinedOnOneLine( element ) ) return

        // Determine what should be considered the 'parent' to align with, i.e., 'return' or 'if'.
        var parent = element.parent
        when ( val precedingKeyword = getPrecedingKeyword( parent ) )
        {
            // 'return' is the topmost expected parent.
            is KtReturnExpression -> parent = precedingKeyword
            // 'if' might still be preceded by 'return' or 'else'.
            is KtIfExpression ->
            {
                val beforeIf = getPrecedingKeyword( precedingKeyword )
                parent =
                    if ( beforeIf is KtReturnExpression || beforeIf is KtIfExpression ) beforeIf
                    else precedingKeyword
            }
        }

        // Multi-line blocks require curly braces on separate lines, aligned with each other and with the parent.
        val leftBrace = element.firstChild
        val rightBrace = element.lastChild
        val bracesAligned = areAligned( leftBrace, rightBrace )
        val blockOpensOnNewLine = startsOnNewLine( leftBrace )
        if ( !bracesAligned || !blockOpensOnNewLine || !areAligned( parent, leftBrace ) )
        {
            report( CodeSmell( issue, Entity.from( element ), issue.description ) )
        }
    }

    /**
     * Get the keyword expression preceding the given element, if any.
     */
    private fun getPrecedingKeyword( element: PsiElement ): PsiElement?
    {
        val spacing = getPrecedingElement( element )

        // In case the preceding element is not whitespace, we do not expect a preceding keyword.
        if ( spacing !is PsiWhiteSpace ) return null

        return getPrecedingElement( spacing )?.parent
    }
}
