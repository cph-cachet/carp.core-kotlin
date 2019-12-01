package dk.cachet.carp.detekt.extensions

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace


/**
 * Verifies whether the entire node is defined on one line.
 */
fun isDefinedOnOneLine( node: ASTNode ): Boolean =
    !node.textContains( '\n' )

/**
 * Determines whether the given [node] starts on a new line, without anything other than whitespace preceding it.
 */
fun startsOnNewLine( node: ASTNode ): Boolean
{
    // In case there is no preceding node, it has to start on a new line.
    val beforeNode = node.treePrev ?: return true

    return beforeNode is PsiWhiteSpace && (beforeNode as PsiWhiteSpace).text.contains( "\n" )
}

/**
 * Verifies whether [node1] and [node2] are on separate lines and have the same indentation.
 */
fun areAligned( node1: ASTNode, node2: ASTNode ): Boolean
{
    val node1Indent = getIndentSize( node1 )
    val node2Indent = getIndentSize( node2 )

    return node1Indent == node2Indent
}

private fun getIndentSize( node: ASTNode ): Int
{
    val beforeNode = getPrecedingNode( node )

    var indentSize = 0
    if ( beforeNode is PsiWhiteSpace )
    {
        val text = ( beforeNode as PsiWhiteSpace ).text
        indentSize = text.count { it != '\n' } // Do not count preceding new lines.
    }

    return indentSize
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

/**
 * Get the element preceding the given element, regardless of whether or not it is owned by a different parent.
 *
 * @return The preceding element, or null when there is no preceding element.
 */
fun getPrecedingElement( element: PsiElement ): PsiElement?
{
    // If preceding element is part of the same parent, no need to start traversing parent.
    if ( element.prevSibling != null ) return element.prevSibling

    // If no preceding element exists, search in the parent if available.
    if ( element.parent != null )
    {
        return getPrecedingElement( element.parent )
    }

    // No preceding element and no parent with preceding elements. This must be the first element.
    return null
}