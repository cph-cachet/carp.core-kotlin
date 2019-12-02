package dk.cachet.carp.detekt.extensions

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace


/**
 * Verifies whether the entire [element] is defined on one line.
 */
fun isDefinedOnOneLine( element: PsiElement ): Boolean =
    !element.textContains( '\n' )

/**
 * Determines whether the given [element] starts on a new line, without anything other than whitespace preceding it.
 */
fun startsOnNewLine( element: PsiElement ): Boolean
{
    // In case there is no preceding element, it has to start on a new line.
    val before = getPrecedingElement( element ) ?: return true

    return before is PsiWhiteSpace && before.text.contains( "\n" )
}

/**
 * Verifies whether [element1] and [element2] are on separate lines and have the same indentation.
 *
 * @throws UnsupportedOperationException when tabs are encountered. Only spaces and newlines are considered.
 */
fun areAligned( element1: PsiElement, element2: PsiElement ): Boolean
{
    val node1Indent = getIndentSize( element1 )
    val node2Indent = getIndentSize( element2 )

    return node1Indent == node2Indent
}

/**
 * Gets the position of the element on the current line.
 *
 * @throws UnsupportedOperationException when tabs are encountered. Only spaces and newlines are considered.
 */
fun getIndentSize( element: PsiElement ): Int
{
    var indentSize = 0
    var curElement = element
    var foundNewline = false
    while ( !foundNewline )
    {
        // Look at preceding element, or early out in case no more elements.
        val preceding = getPrecedingElement( curElement ) ?: break

        if ( preceding is PsiWhiteSpace && preceding.text.contains( '\n' ) )
        {
            val whitespace = preceding.text
            val lastNewline = whitespace.lastIndexOf( '\n' )

            // Currently, tabs are not supported.
            // TODO: Can tabs be supported as well?
            if ( whitespace.contains( '\t' ) )
                throw UnsupportedOperationException( "getIndentSize does not support tabs." )

            // When a newline is found, count all characters from the newline.
            foundNewline = true
            indentSize += whitespace.length - 1 - lastNewline
        }
        else
        {
            indentSize += preceding.textLength
        }
        curElement = preceding
    }

    return indentSize
}

/**
 * Get the element preceding the given [element], regardless of whether or not it is owned by a different parent.
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
