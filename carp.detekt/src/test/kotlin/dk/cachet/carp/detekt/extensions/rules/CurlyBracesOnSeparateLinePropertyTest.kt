package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.*


/**
 * Tests for [CurlyBracesOnSeparateLine] for getters/setters of properties.
 */
class CurlyBracesOnSeparateLinePropertyTest
{
    @Test
    fun curly_braces_of_get_need_to_be_on_separate_lines()
    {
        val newLine =
            """
            val answer: Int
                get()
                {
                    return 42
                }
            """
        assertEquals( 0, codeSmells( newLine ) )

        val noNewLineOpen =
            """
            val answer: Int
                get() {
                    return 42
                }
            """
        assertEquals( 1, codeSmells( noNewLineOpen ) )

        val noNewLineClose =
            """
            val answer: Int
                get()
                {
                    return 42 }
            }
            """
        assertEquals( 1, codeSmells( noNewLineClose ) )
    }

    @Test
    fun curly_braces_of_set_need_to_be_on_separate_lines()
    {
        val newLine =
            """
            var answer: Int = 42
                set( value )
                {
                    field = 42
                }
            """
        assertEquals( 0, codeSmells( newLine ) )

        val noNewLineOpen =
            """
            var answer: Int = 42
                set( value ) {
                    field = 42
                }
            """
        assertEquals( 1, codeSmells( noNewLineOpen ) )

        val noNewLineClose =
            """
            var answer: Int = 42
                set( value )
                {
                    field = 42 }
            """
        assertEquals( 1, codeSmells( noNewLineClose ) )
    }

    @Test
    fun get_may_be_defined_on_one_line()
    {
        val oneLine =
            """
            val answer: Int
                get() { return 42 }
            """
        assertEquals( 0, codeSmells( oneLine ) )
    }

    @Test
    fun set_may_be_defined_on_one_line()
    {
        val oneLine =
            """
            var answer: Int = 42
                set( value ) { field = 42 }
            """
        assertEquals( 0, codeSmells( oneLine ) )
    }

    @Test
    fun curly_braces_of_get_need_to_be_aligned()
    {
        val aligned =
            """
            val answer: Int
                get()
                {
                    return 42
                }
            """
        assertEquals( 0, codeSmells( aligned ) )

        val notAligned =
            """
            val answer: Int
                get()
                    {
                    return 42
                }
            """
        assertEquals( 1, codeSmells( notAligned ) )

        val notAligned2 =
            """
            val answer: Int
                get()
                {
                    return 42
            }
            }
            """
        assertEquals( 1, codeSmells( notAligned2 ) )
    }

    @Test
    fun curly_braces_of_set_need_to_be_aligned()
    {
        val aligned =
            """
            var answer: Int = 42
                set( value )
                {
                    field = 42
                }
            """
        assertEquals( 0, codeSmells( aligned ) )

        val notAligned =
            """
            var answer: Int = 42
                set( value )
                    {
                    field = 42
                }
            """
        assertEquals( 1, codeSmells( notAligned ) )

        val notAligned2 =
            """
            var answer: Int = 42
                set( value )
                {
                    field = 42
            }
            """
        assertEquals( 1, codeSmells( notAligned2 ) )
    }

    @Test
    fun get_indentation_should_be_aligned_with_start_of_definition()
    {
        val wrongIndentation =
            """
            val answer: Int
                get()
                    {
                        return 42
                    }
            """
        assertEquals( 1, codeSmells( wrongIndentation ) )
    }

    @Test
    fun set_indentation_should_be_aligned_with_start_of_definition()
    {
        val wrongIndentation =
            """
            var answer: Int = 42
                set( value )
                    {
                        field = 42
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
            
            val answer: Int
                get()
                {
                    return 42
                }
            """
        assertEquals( 0, codeSmells( notAtStart ) )
    }


    private fun codeSmells( code: String ): Int
    {
        val rule = CurlyBracesOnSeparateLine()
        return rule.lint( code ).count()
    }
}
