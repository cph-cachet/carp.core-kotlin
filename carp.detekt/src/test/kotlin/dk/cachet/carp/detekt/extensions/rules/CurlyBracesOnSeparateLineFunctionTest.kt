package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.*


/**
 * Tests for [CurlyBracesOnSeparateLine] for functions.
 */
class CurlyBracesOnSeparateLineFunctionTest
{
    @Test
    fun curly_braces_of_functions_need_to_be_on_separate_lines()
    {
        val newLine =
            """
            fun answer(): Int
            {
                return 42
            }
            """
        assertEquals( 0, codeSmells( newLine ) )

        val noNewLineOpen =
            """
            fun answer(): Int {
                return 42
            }
            """
        assertEquals( 1, codeSmells( noNewLineOpen ) )

        val noNewLineClose =
            """
            fun answer(): Int
            {
                return 42 }
            """
        assertEquals( 1, codeSmells( noNewLineClose ) )
    }

    @Test
    fun function_may_be_defined_on_one_line()
    {
        val oneLine = "fun answer(): Int { return 42 }"
        assertEquals( 0, codeSmells( oneLine ) )
    }

    @Test
    fun curly_braces_of_function_need_to_be_aligned()
    {
        val aligned =
            """
            fun answer(): Int
            {
                return 42
            }
            """
        assertEquals( 0, codeSmells( aligned ) )

        val notAligned =
            """
            fun answer(): Int
                {
                    return 42
            }
            """
        assertEquals( 1, codeSmells( notAligned ) )

        val notAligned2 =
            """
            fun answer(): Int
            {
                return 42
                }
            """
        assertEquals( 1, codeSmells( notAligned2 ) )
    }

    @Test
    fun indentation_should_be_aligned_with_start_of_definition()
    {
        val wrongIndentation =
            """
            fun answer(): Int
                {
                    return 42
                }
            """
        assertEquals( 1, codeSmells( wrongIndentation ) )
    }

    @Test
    fun return_anonymous_function_should_be_treated_as_parent()
    {
        val returnIf =
            """
            fun test(): () -> Int
            {
                return fun(): Int
                {
                    return 42
                }
            }
            """
        assertEquals( 0, codeSmells( returnIf ) )
    }


    private fun codeSmells( code: String ): Int
    {
        val rule = CurlyBracesOnSeparateLine()
        return rule.lint( code ).count()
    }
}
