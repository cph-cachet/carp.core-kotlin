package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.*


/**
 * Tests for [CurlyBracesOnSeparateLine] for if conditionals.
 */
class CurlyBracesOnSeparateLineIfTest
{
    @Test
    fun curly_braces_of_if_need_to_be_on_separate_lines()
    {
        val newLine =
            """
            fun answer(): Int
            {
                if ( true )
                {
                    return 42
                }
            }
            """
        assertEquals( 0, codeSmells( newLine ) )

        val noNewLineOpen =
            """
            fun answer(): Int
            {
                if ( true ) {
                    return 42
                }
            }
            """
        assertEquals( 1, codeSmells( noNewLineOpen ) )

        val noNewLineClose =
            """
            fun answer(): Int
            {
                if ( true )
                {
                    return 42 }
            }
            """
        assertEquals( 1, codeSmells( noNewLineClose ) )
    }

    @Test
    fun curly_braces_of_else_need_to_be_on_separate_lines()
    {
        val newLine =
            """
            fun answer(): Int
            {
                if ( false ) 0
                else
                {
                    return 42
                }
            }
            """
        assertEquals( 0, codeSmells( newLine ) )

        val noNewLineOpen =
            """
            fun answer(): Int
            {
                if ( false ) 0
                else {
                    return 42
                }
            }
            """
        assertEquals( 1, codeSmells( noNewLineOpen ) )

        val noNewLineClose =
            """
            fun answer(): Int
            {
                if ( false ) 0
                else
                {
                    return 42 }
            }
            """
        assertEquals( 1, codeSmells( noNewLineClose ) )
    }

    @Test
    fun curly_braces_of_else_if_should_be_on_separate_lines()
    {
        val elseIf =
            """
            fun answer(): Int
            {
                if ( false ) 0
                else if ( true )
                {
                    return 42
                }
            }
            """
        assertEquals( 0, codeSmells( elseIf ) )
    }

    @Test
    fun if_may_be_defined_on_one_line()
    {
        val oneLine =
            """
            fun answer(): Int
            {
                if ( true ) return 42
            }
            """
        assertEquals( 0, codeSmells( oneLine ) )
    }

    @Test
    fun curly_braces_of_if_need_to_be_aligned()
    {
        val aligned =
            """
            fun answer(): Int
            {
                if ( true )
                {
                    return 42
                }
            }
            """
        assertEquals( 0, codeSmells( aligned ) )

        val notAligned =
            """
            fun answer(): Int
            {
                if ( true )
                    {
                    return 42
                }
            }
            """
        assertEquals( 1, codeSmells( notAligned ) )

        val notAligned2 =
            """
            fun answer(): Int
            {
                if ( true )
                {
                    return 42
            }
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
                if ( true )
                    {
                        return 42
                    }
            }
            """
        assertEquals( 1, codeSmells( wrongIndentation ) )
    }

    @Test
    fun position_in_file_should_not_impact_rule()
    {
        val notAtStart =
            """
            import kotlin.text.*
            
            fun answer(): Int
            {
                if ( true )
                {
                    return 42
                }
            }
            """
        assertEquals( 0, codeSmells( notAtStart ) )
    }

    @Test
    fun nesting_should_not_impact_rule()
    {
        val companionAligned =
            """
            fun answer(): Int
            {
                if ( true )
                {
                    if ( true )
                    {
                        return 42
                    }
                }
            }
            """
        assertEquals( 0, codeSmells( companionAligned ) )
    }

    @Test
    fun return_if_condition_should_be_treated_as_parent()
    {
        val returnIf =
            """
            fun answer(): Int =
                return if ( true )
                {
                    42
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
