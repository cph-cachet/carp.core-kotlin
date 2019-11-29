package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.parents


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

    override fun visitClassBody( classBody: KtClassBody )
    {
        super.visitClassBody( classBody )

        val node = classBody.node

        // Do not report classes which are fully defined on one line.
        val isOneLineClass = node.children()
            .filterIsInstance<PsiWhiteSpace>()
            .none { it.text.contains( "\n" ) }
        if ( isOneLineClass ) return

        // Multi-line class definitions require curly braces on separate lines.
        var invalidCurlyBraces = false
        val beforeOpen = node.treePrev
        val beforeClose = node.lastChildNode.treePrev
        val areWhitespaces = beforeOpen is PsiWhiteSpace && beforeClose is PsiWhiteSpace
        invalidCurlyBraces = !areWhitespaces
        if ( areWhitespaces )
        {
            val sameIndentation = beforeOpen.text == beforeClose.text
            val blockOpensOnNewLine = beforeClose.text.startsWith( "\n" )
            invalidCurlyBraces = !sameIndentation || !blockOpensOnNewLine
        }
        if ( invalidCurlyBraces )
        {
            report( CodeSmell( issue, Entity.from( classBody ), issue.description ) )
        }
    }
}