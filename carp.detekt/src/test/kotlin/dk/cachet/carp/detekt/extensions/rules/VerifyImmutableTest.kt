package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.KtTestCompiler
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import kotlin.test.*


/**
 * Tests for [VerifyImmutable].
 */
class VerifyImmutableTest
{
    @Test
    fun only_verify_annotated_classes()
    {
        val notAnnotated = "class NotImmutable( var invalidMember: String )"
        val isIgnored = isImmutable( notAnnotated )
        assertTrue( isIgnored ) // Even though this class is mutable, the check should not happen.
    }

    @Test
    fun verify_classes_extending_from_annotated_classes()
    {
        val notAllImmutable =
            """
            @Immutable
            abstract class BaseClass
             
            class NotImmutable( var invalidMember: Int = 42 ) : BaseClass()
            """
        assertFalse( isImmutable( notAllImmutable ) )
    }

    @Test
    fun verify_full_inheritance_tree()
    {
        val notAllImmutable =
            """
            annotation class Immutable
            
            @Immutable
            abstract class LastBase
            
            abstract class BaseClass : LastBase()
             
            class NotImmutable( var invalidMember: Int = 42 ) : BaseClass()
            """
        assertFalse( isImmutable( notAllImmutable ) )
    }

    @Test
    fun implementations_should_be_data_classes()
    {
        val dataClass = "@Immutable data class ValidImmutable( val validMember: Int = 42 )"
        assertTrue( isImmutable( dataClass ) )

        val noDataClass = "@Immutable class NotImmutable( val validMember: Int = 42 )"
        assertFalse( isImmutable( noDataClass ) )
    }

    @Test
    fun abstract_classes_do_not_need_to_be_data_classes()
    {
        val abstractClass = "@Immutable abstract class BaseClass"
        assertTrue( isImmutable( abstractClass ) )
    }

    @Test
    fun constructor_properties_should_be_val()
    {
        val valProperty = "@Immutable data class ValidImmutable( val validMember: Int = 42 )"
        assertTrue( isImmutable( valProperty ) )

        val varProperty = "@Immutable data class ValidImmutable( var invalidMember: Int = 42 )"
        assertFalse( isImmutable( varProperty ) )
    }

    @Test
    fun properties_should_be_val()
    {
        val valProperty = "@Immutable data class ValidImmutable( val validMember: Int = 42 ) { val validProperty: Int = 42 } "
        assertTrue( isImmutable( valProperty ) )

        val varProperty = "@Immutable data class NotImmutable( val validMember: Int = 42 ) { var invalidProperty: Int = 42 }"
        assertFalse( isImmutable( varProperty ) )
    }

    private fun isImmutable( code: String ): Boolean
    {
        val rule = VerifyImmutable( "Immutable" )
        val env = KtTestCompiler.createEnvironment().env

        return rule.compileAndLintWithContext( env, code ).isEmpty()
    }
}
