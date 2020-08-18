package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.*


/**
 * Tests for [CurlyBracesOnSeparateLine] for lambdas.
 */
class CurlyBracesOnSeparateLineLambdaTest
{
    @Test
    fun lambda_braces_need_to_be_on_separate_lines()
    {
        val newLine =
            """
            val test: (Int) -> Unit =
                {
                    _ ->
                }
            """
        assertEquals( 0, codeSmells( newLine ) )

        val noNewLineOpen =
            """
            val test: () -> Unit = {
                    _ ->
                }
            """
        assertEquals( 1, codeSmells( noNewLineOpen ) )

        val noNewLineClose =
            """
            val test: () -> Unit =
                {
                    _ -> val answer = 42 }
            """
        assertEquals( 1, codeSmells( noNewLineClose ) )
    }

    @Test
    fun lambdas_may_be_defined_on_one_line()
    {
        val oneLine = "val test: (Int) -> Unit = { _ -> }"
        assertEquals( 0, codeSmells( oneLine ) )
    }

    @Test
    fun curly_braces_of_lambdas_need_to_be_aligned()
    {
        val aligned =
            """
            val test: (Int) -> Unit =
                {
                    _ ->
                }
            """
        assertEquals( 0, codeSmells( aligned ) )

        val notAligned =
            """
            val test: (Int) -> Unit =
            {
                _ ->
                }
            """
        assertEquals( 1, codeSmells( notAligned ) )

        val notAligned2 =
            """
            val test: (Int) -> Unit =
                {
                _ ->
            }
            """
        assertEquals( 1, codeSmells( notAligned2 ) )
    }

    @Test
    fun lambdas_have_no_parent_definition_to_align_with()
    {
        // Currently this rule does not require aligning with any 'parent'.
        // Normal indentation rules (checked by another rule) should apply here, though.
        val indented =
            """
            val test: (Int) -> Unit =
                    {
                        _ ->
                    }
            """
        assertEquals( 0, codeSmells( indented ) )
    }

    @Test
    fun function_invocation_with_trailing_lambda_is_ignored()
    {
        val trailingLambda =
            """
            fun test( list: List<Int> )
            {
                list.forEach {
                    val answer = it }
            }
            """
        assertEquals( 0, codeSmells( trailingLambda ) )
    }


    private fun codeSmells( code: String ): Int
    {
        val rule = CurlyBracesOnSeparateLine()
        return rule.lint( code ).count()
    }
}
