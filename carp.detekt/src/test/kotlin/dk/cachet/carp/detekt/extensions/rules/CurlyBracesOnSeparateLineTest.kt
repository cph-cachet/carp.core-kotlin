package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.*


/**
 * Tests for [CurlyBracesOnSeparateLine].
 */
class CurlyBracesOnSeparateLineTest
{
    @Test
    fun curly_braces_of_blocks_need_to_be_on_separate_lines()
    {
        val newLine =
            """
            class NewLine()
            {
                fun answer(): Int = 42
            }
            """
        assertEquals( 0, codeSmells( newLine ) )

        val noNewLineOpen =
            """
            class NoNewLine() {
                fun answer(): Int = 42
            }
            """
        assertEquals( 1, codeSmells( noNewLineOpen ) )

        val noNewLineClose =
            """
            class NoNewLine()
            {
                fun answer(): Int = 42 }
            """
        assertEquals( 1, codeSmells( noNewLineClose ) )
    }

    @Test
    fun blocks_may_be_defined_on_one_line()
    {
        val oneLine = "class OneLine { val test: Int }"
        assertEquals( 0, codeSmells( oneLine ) )
    }

    @Test
    fun curly_braces_of_blocks_need_to_be_aligned()
    {
        val aligned =
            """
            class Aligned()
            {
                fun answer(): Int = 42
            }
            """
        assertEquals( 0, codeSmells( aligned ) )

        val notAligned =
            """
            class Aligned()
                {
                fun answer(): Int = 42
            }
            """
        assertEquals( 1, codeSmells( notAligned ) )

        val notAligned2 =
            """
            class Aligned()
            {
                fun answer(): Int = 42
        }
            """
        assertEquals( 1, codeSmells( notAligned2 ) )
    }

    @Test
    fun indentation_should_be_aligned_with_parent()
    {
        val wrongIndentation =
            """
            class CompanionAligned()
                {
                    fun answer(): Int = 42
                }
            """
        assertEquals( 1, codeSmells( wrongIndentation ) )
    }

    @Test
    fun nesting_should_not_impact_rule()
    {
        val companionAligned =
            """
            class CompanionAligned()
            {
                companion object
                {
                    fun answer(): Int = 42
                }
            }
            """
        assertEquals( 0, codeSmells( companionAligned ) )
    }


    private fun codeSmells( code: String ): Int
    {
        val rule = CurlyBracesOnSeparateLine()
        return rule.lint( code ).count()
    }
}