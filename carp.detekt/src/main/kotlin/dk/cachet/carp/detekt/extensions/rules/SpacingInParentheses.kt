package dk.cachet.carp.detekt.extensions.rules

import dk.cachet.carp.detekt.extensions.getNextElement
import dk.cachet.carp.detekt.extensions.getPrecedingElement
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.stubs.elements.KtParameterElementType


/**
 * A rule which verifies whether spaces are added in all parentheses, except those of higher-order functions.
 */
class SpacingInParentheses( config: Config = Config.empty ) : Rule( config )
{
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Spaces are needed inside parentheses, except for higher-order functions.",
        Debt.FIVE_MINS
    )

    override fun visitPropertyAccessor( accessor: KtPropertyAccessor )
    {
        super.visitPropertyAccessor( accessor )

        // Get accessors do not have a parameter list, so need to be validated here.
        // They are always empty, so should not contains spaces.
        if ( accessor.isGetter )
        {
            val noSpaces = accessor.text.startsWith( "get()" )
            if ( !noSpaces )
            {
                report( CodeSmell( issue, Entity.from( accessor ), "Get accessors should not contain spaces in the parentheses." ) )
            }
        }
    }

    override fun visitParameterList( list: KtParameterList )
    {
        super.visitParameterList( list )

        val node = list.node
        val parent = list.parent
        val isHigherOrder = parent is KtFunctionType || parent is KtFunctionLiteral
        val hasParameters = node.children().any { it.elementType is KtParameterElementType }

        // Higher-order function parameters do not expect spaces in parentheses.
        if ( isHigherOrder )
        {
            if ( parent is KtFunctionType && hasSpaces( node ) )
            {
                val message = "Higher-order function parentheses should not contain spaces."
                report( CodeSmell( issue, Entity.from( list ), message ) )
            }
        }
        // All other parentheses expect spaces, except when there are no parameters.
        else
        {
            if ( hasParameters )
            {
                if ( !hasSpaces( node ) )
                {
                    val message = "Spaces are needed inside parentheses."
                    report( CodeSmell( issue, Entity.from( list ), message ) )
                }
            }
            // Solely LPAR and RPAR are expected.
            else if ( node.children().count() != 2 )
            {
                val message = "Empty parentheses should contain no spaces."
                report( CodeSmell( issue, Entity.from( list ), message ) )
            }
        }
    }

    private fun hasSpaces( node: ASTNode ): Boolean
    {
        val spaceAfter = getNextElement( node.firstChildNode.psi )
        val spaceBefore = getPrecedingElement( node.lastChildNode.psi )

        return spaceAfter is PsiWhiteSpace && spaceBefore is PsiWhiteSpace
    }
}
