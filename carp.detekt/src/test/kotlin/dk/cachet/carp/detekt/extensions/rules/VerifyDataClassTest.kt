package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import kotlin.test.*


/**
 * Tests for [VerifyDataClass].
 */
class VerifyDataClassTest
{
    @Test
    fun implementations_should_be_data_classes()
    {
        val dataClass = "@ImplementAsDataClass data class IsDataClass( val member: Int = 42 )"
        assertTrue( isDataClass( dataClass ) )

        val noDataClass = "@ImplementAsDataClass class NoDataClass( val member: Int = 42 )"
        assertFalse( isDataClass( noDataClass ) )
    }

    @Test
    fun abstract_classes_do_not_need_to_be_data_classes()
    {
        val abstractClass = "@ImplementAsDataClass abstract class BaseClass"
        assertTrue( isDataClass( abstractClass ) )
    }

    @Test
    fun sealed_classes_do_not_need_to_be_data_classes()
    {
        val sealedClass = "@ImplementAsDataClass sealed class Sealed"
        assertTrue( isDataClass( sealedClass ) )
    }

    private fun isDataClass( code: String ): Boolean
    {
        // Add require data class annotation.
        val fullCode = code.plus( "annotation class ImplementAsDataClass" )

        val rule = VerifyDataClass( "ImplementAsDataClass" )
        val env = KtTestCompiler.createEnvironment().env

        return rule.compileAndLintWithContext( env, fullCode ).isEmpty()
    }
}
