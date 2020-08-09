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
    fun use_fully_qualified_annotation_name()
    {
        val fullyQualified =
            """
            package some.namespace
            
            annotation class Immutable
            
            @Immutable class Mutable
            """

        val rule = VerifyImmutable( "some.namespace.Immutable" )
        val env = KtTestCompiler.createEnvironment().env

        val errorsReported = rule.compileAndLintWithContext( env, fullyQualified ).isNotEmpty()
        assertTrue( errorsReported )
    }

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
    fun verify_sealed_classes()
    {
        val innerNotImmutable =
            """
            @Immutable
            sealed class Outer
            {
                class Inner( var mutable: Int ) : Outer()
            }
            """
        assertFalse( isImmutable( innerNotImmutable ) )
    }

    @Test
    fun verify_used_typealias()
    {
        val immutable =
            """
            data class ValidImmutable( val mutable: Int )
            typealias AliasedValidImmutable = ValidImmutable
            
            @Immutable
            data class UsesTypealias( val mutable: AliasedValidImmutable ) 
            """
        assertTrue( isImmutable( immutable ) )
    }

    @Test
    fun do_not_allow_type_inference()
    {
        val hasTypeInference =
            """
            @Immutable data class WithTypeInference { val inferred = 42 }    
            """
        assertFalse( isImmutable( hasTypeInference) )

        val noTypeInference =
            """
            @Immutable data class WithoutTypeInference { val inferred: Int = 42 }    
            """
        assertTrue( isImmutable( noTypeInference ) )
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
    fun sealed_classes_do_not_need_to_be_data_classes()
    {
        val sealedClass = "@Immutable sealed class Sealed"
        assertTrue( isImmutable( sealedClass ) )
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
    fun constructor_properties_should_be_immutable_types()
    {
        val immutableProperty =
            """
            @Immutable data class ImmutableMember( val number: Int = 42 )
            @Immutable data class ValidImmutable( val validMember: ImmutableMember )
            """
        assertTrue( isImmutable( immutableProperty ) )

        val mutableProperty =
            """
            data class MutableMember( var number: Int = 42 )
            @Immutable data class ValidImmutable( val validMember: MutableMember )
            """
        assertFalse( isImmutable( mutableProperty ) )
    }

    @Test
    fun properties_should_be_val()
    {
        val valProperty = "@Immutable data class ValidImmutable( val validMember: Int = 42 ) { val validProperty: Int = 42 } "
        assertTrue( isImmutable( valProperty ) )

        val varProperty = "@Immutable data class NotImmutable( val validMember: Int = 42 ) { var invalidProperty: Int = 42 }"
        assertFalse( isImmutable( varProperty ) )
    }

    @Test
    fun properties_should_be_immutable_types()
    {
        val immutableProperty =
            """
            @Immutable data class ImmutableMember( val number: Int = 42 )
            @Immutable data class ValidImmutable( val test: Int )
            {
                val validMember: ImmutableMember = ImmutableMember( 42 )
            }
            """
        assertTrue( isImmutable( immutableProperty ) )

        val mutableProperty =
            """
            data class MutableMember( var number: Int = 42 )
            @Immutable data class ValidImmutable( val test: Int )
            {
                val invalidMember: MutableMember = MutableMember( 42 )
            }
            """
        assertFalse( isImmutable( mutableProperty ) )
    }

    @Test
    fun report_multiple_mutable_findings()
    {
        val twoMutableMembers =
            """
            annotation class Immutable
            @Immutable data class NotImmutable( val immutable: Int )
            {
                var one: Int = 42
                var two: Int = 42
            }
            """
        val rule = VerifyImmutable( "Immutable" )
        val env = KtTestCompiler.createEnvironment().env

        assertEquals( 2, rule.compileAndLintWithContext( env, twoMutableMembers ).count() )
    }

    private fun isImmutable( code: String ): Boolean
    {
        // Add immutable annotation.
        val fullCode = code.plus( "annotation class Immutable" )

        val rule = VerifyImmutable( "Immutable" )
        val env = KtTestCompiler.createEnvironment().env

        return rule.compileAndLintWithContext( env, fullCode ).isEmpty()
    }
}
