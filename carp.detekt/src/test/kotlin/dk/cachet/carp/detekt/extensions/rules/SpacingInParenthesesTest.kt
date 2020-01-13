package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.*


/**
 * Tests for [SpacingInParentheses].
 */
class SpacingInParenthesesTest
{
    @Test
    fun spaces_are_required_in_common_parentheses()
    {
        val spaces = "fun spaces( correct: Int ) {}"
        assertEquals( 0, codeSmells( spaces ) )

        val noSpaces = "fun noSpaces(wrong: Int) {}"
        assertEquals( 1, codeSmells( noSpaces ) )
    }

    @Test
    fun no_spaces_in_empty_parentheses()
    {
        val noSpaces = "fun noParams() {}"
        assertEquals( 0, codeSmells( noSpaces ) )

        val spaces = "fun noParams( ) {}"
        assertEquals( 1, codeSmells( spaces ) )
    }

    @Test
    fun no_spaces_in_higher_order_functions()
    {
        val noSpaces = "val answer: (Int, Int) -> Int = { a: Int, _: Int -> a }"
        assertEquals( 0, codeSmells( noSpaces ) )

        val spaces = "val answer: ( Int, Int ) -> Int = { a: Int, _: Int -> a }"
        assertEquals( 1, codeSmells( spaces ) )
    }

    @Test
    fun spaces_are_required_in_exception_handlers()
    {
        val spaces = "fun test() { try {} catch ( e: Exception ) {} }"
        assertEquals( 0, codeSmells( spaces ) )

        val noSpaces = "fun test() { try {} catch (e: Exception) {} }"
        assertEquals( 1, codeSmells( noSpaces ) )
    }

    @Test
    fun multiline_parentheses_are_allowed()
    {
        val multiline =
            """
            data class Multiline(
                val one: Int,
                val two: Int
            )
            """
        assertEquals( 0, codeSmells( multiline ) )
    }


    private fun codeSmells( code: String ): Int
    {
        val rule = SpacingInParentheses()
        return rule.lint( code ).count()
    }
}
