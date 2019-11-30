package dk.cachet.carp.detekt.extensions

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.psiUtil.children


/**
 * Verifies whether the entire node is defined on one line.
 */
fun isDefinedOnOneLine( node: ASTNode ): Boolean
{
    return node.children()
        .filterIsInstance<PsiWhiteSpace>()
        .none { it.text.contains( "\n" ) }
}

/**
 * Determines whether the given [node] starts on a new line, without anything other than whitespace preceding it.
 */
fun startsOnNewLine( node: ASTNode ): Boolean
{
    // In case there is no preceding node, it has to start on a new line.
    val beforeNode = node.treePrev ?: return true

    return beforeNode is PsiWhiteSpace && (beforeNode as PsiWhiteSpace).text.startsWith( "\n" )
}

/**
 * Verifies whether [node1] and [node2] are on separate lines and have the same indentation.
 */
fun areAligned( node1: ASTNode, node2: ASTNode ): Boolean
{
    val node1Indent = getPrecedingNode( node1 )
    val node2Indent = getPrecedingNode( node2 )

    // node1 and node2 cannot possibly be on separate lines if not preceded by PsiWhiteSpace, which includes newlines.
    if ( node1Indent !is PsiWhiteSpace || node2Indent !is PsiWhiteSpace ) return false

    return node1Indent.textMatches( node2Indent )
}

/**
 * Get the node preceding the given node, regardless of whether or not it is owned by a different parent.
 *
 * @return The preceding node, or null when there is no preceding node.
 */
fun getPrecedingNode( node: ASTNode ): ASTNode?
{
    // If preceding node is part of the same parent, no need to start traversing parent.
    if ( node.treePrev != null ) return node.treePrev

    // If no preceding node exists, search in the parent if available.
    if ( node.treeParent != null )
    {
        return getPrecedingNode( node.treeParent )
    }

    // No preceding node and no parent with preceding nodes. This must be the first node.
    return null
}