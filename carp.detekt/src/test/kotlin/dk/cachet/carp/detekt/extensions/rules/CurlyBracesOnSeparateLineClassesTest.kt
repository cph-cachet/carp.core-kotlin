package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.*


/**
 * Tests for [CurlyBracesOnSeparateLine] for class and object definitions.
 */
class CurlyBracesOnSeparateLineClassesTest
{
    @Test
    fun curly_braces_of_class_need_to_be_on_separate_lines()
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
    fun classes_may_be_defined_on_one_line()
    {
        val oneLine = "class OneLine { val test: Int }"
        assertEquals( 0, codeSmells( oneLine ) )
    }

    @Test
    fun curly_braces_of_classes_need_to_be_aligned()
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
            class NotAligned()
                {
                fun answer(): Int = 42
            }
            """
        assertEquals( 1, codeSmells( notAligned ) )

        val notAligned2 =
            """
            class NotAligned()
            {
                fun answer(): Int = 42
        }
            """
        assertEquals( 1, codeSmells( notAligned2 ) )
    }

    @Test
    fun indentation_should_be_aligned_with_start_of_definition()
    {
        val wrongIndentation =
            """
            class WrongIndentation()
                {
                    fun answer(): Int = 42
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
            
            class PositionedElsewhere()
            {
                fun answer(): Int = 42
            }
            """
        assertEquals( 0, codeSmells( notAtStart ) )
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

    @Test
    fun object_literal_should_be_treated_as_start_of_definition()
    {
        val objectLiteral =
            """
            fun getObject() =
                object : Comparable<Int>
                {
                    override fun compareTo( other: Int ): Int = 0
                } 
            """
        assertEquals( 0, codeSmells( objectLiteral ) )

        val objectLiteral2 =
            """
            fun getObject() =
                object :
                    Comparable<Int>
                {
                    override fun compareTo( other: Int ): Int = 0
                } 
            """
        assertEquals( 0, codeSmells( objectLiteral2 ) )
    }

    @Test
    fun return_object_literal_should_be_treated_as_start_of_definition()
    {
        val objectLiteral =
            """
            fun getObject(): Comparable<Int>
            {
                return object : Comparable<Int>
                {
                    override fun compareTo( other: Int ): Int = 0
                }
            }
            """
        assertEquals( 0, codeSmells( objectLiteral ) )

        val objectLiteral2 =
            """
            fun getObject(): Comparable<Int>
            {
                return object :
                    Comparable<Int>
                {
                    override fun compareTo( other: Int ): Int = 0
                }
            }
            """
        assertEquals( 0, codeSmells( objectLiteral2 ) )
    }


    private fun codeSmells( code: String ): Int
    {
        val rule = CurlyBracesOnSeparateLine()
        return rule.lint( code ).count()
    }
}
