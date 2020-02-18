package dk.cachet.carp.detekt.extensions.rules

import io.gitlab.arturbosch.detekt.test.lint
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


    private fun isImmutable( code: String ): Boolean
    {
        val rule = VerifyImmutable( "Immutable" )
        return rule.lint( code ).isEmpty()
    }
}