package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtObjectLiteralExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.psiUtil.children


/**
 * A rule which verifies whether curly braces of class definitions are placed on separate lines,
 * aligned with the start of the line on which the class is defined.
 */
class CurlyClassBracesOnSeparateLine : Rule()
{
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Curly braces of class definitions need to be placed on separate lines, aligned with the start of the line on which the class is defined.",
        Debt.FIVE_MINS
    )

    override fun visitClassBody( classBody: KtClassBody )
    {
        super.visitClassBody( classBody )

        val node = classBody.node

        // Do not report classes which are fully defined on one line.
        val isOneLineClass = node.children()
            .filterIsInstance<PsiWhiteSpace>()
            .none { it.text.contains( "\n" ) }
        if ( isOneLineClass ) return

        // Multi-line class definitions require curly braces on separate lines, aligned with the class definition.
        var invalidCurlyBraces: Boolean
        val beforeOpen = node.treePrev
        val beforeClose = node.lastChildNode.treePrev
        val areWhitespaces = beforeOpen is PsiWhiteSpace && beforeClose is PsiWhiteSpace
        invalidCurlyBraces = !areWhitespaces
        if ( areWhitespaces )
        {
            val bracesAligned = beforeOpen.text == beforeClose.text
            val blockOpensOnNewLine = beforeClose.text.startsWith( "\n" )
            invalidCurlyBraces = !bracesAligned || !blockOpensOnNewLine || !isAlignedWithParent( classBody )
        }
        if ( invalidCurlyBraces )
        {
            report( CodeSmell( issue, Entity.from( classBody ), issue.description ) )
        }
    }

    private fun isAlignedWithParent( classBody: KtClassBody ): Boolean
    {
        // Determine what should be considered the 'parent':
        // - Object declarations are wrapped in object literals.
        // - When returning object literals, the return statement should be considered the parent.
        var parent = classBody.parent
        if ( parent is KtObjectDeclaration && parent.parent is KtObjectLiteralExpression )
        {
            parent = parent.parent

            // Verify whether the statement is: "return object : SomeInterface <KtClassBody>"
            val possibleReturn = parent.prevSibling?.prevSibling?.parent
            if ( possibleReturn is KtReturnExpression )
            {
                parent = possibleReturn
            }
        }

        // Find parent indentation and opening brace indentation.
        val parentIndent = getIndentSize( parent )
        val braceIndent = getIndentSize( classBody )

        return parentIndent == braceIndent
    }

    private fun getIndentSize( element: PsiElement ): Int
    {
        val beforeElement = element.node.treePrev

        var indentSize = 0
        if ( beforeElement is PsiWhiteSpace )
        {
            val text = (beforeElement as PsiWhiteSpace).text
            indentSize = text.count { it != '\n' } // Do not count preceding new lines.
        }

        return indentSize
    }
}