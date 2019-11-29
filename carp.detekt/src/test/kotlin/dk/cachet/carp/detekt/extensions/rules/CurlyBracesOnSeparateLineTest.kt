package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.*


/**
 * Tests for [CurlyBracesOnSeparateLine].
 */
class CurlyBracesOnSeparateLineTest
{
    @Test
    fun curly_braces_of_class_blocks_need_to_be_on_separate_lines()
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

    @Test
    fun classes_may_be_defined_on_one_line()
    {
        val oneLine = "class OneLine { val test: Int }"
        assertEquals( 0, codeSmells( oneLine ) )
    }


    private fun codeSmells( code: String ): Int
    {
        val rule = CurlyBracesOnSeparateLine()
        return rule.lint( code ).count()
    }
}