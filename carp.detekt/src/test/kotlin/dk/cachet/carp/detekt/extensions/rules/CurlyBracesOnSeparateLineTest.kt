package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.*


/**
 * Tests for [CurlyBracesOnSeparateLine].
 */
class CurlyBracesOnSeparateLineTest
{
    @Test
    fun curly_braces_of_class_blocks_on_separate_lines()
    {
        val newLine =
            """
            class NewLine()
            {
                fun answer(): Int = 42
            }
            """
        assertEquals( 0, codeSmells( newLine ) )

        val noNewLine =
            """
            class NoNewLine() {
                fun answer(): Int = 42
            }
            """
        assertEquals( 1, codeSmells( noNewLine ) )
    }


    private fun codeSmells( code: String ): Int
    {
        val rule = CurlyBracesOnSeparateLine()
        return rule.lint( code ).count()
    }
}