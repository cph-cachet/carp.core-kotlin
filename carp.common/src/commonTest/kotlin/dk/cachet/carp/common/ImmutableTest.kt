package dk.cachet.carp.common

import dk.cachet.carp.test.JsIgnore
import kotlin.test.*


/**
 * Tests for [Immutable].
 *
 * TODO: Verify whether this works for generics (with type parameters), nullable types, and other potential special cases.
 */
@JsIgnore
class ImmutableTest
{
    /**
     * A correct implementation of [Immutable] because it is a data class and only contains immutable properties.
     */
    private data class ValidImmutable( val validMember: String = "Valid" )

    /**
     * An incorrect implementation of [Immutable] because it is not defined as a data class.
     */
    private class NoDataClass : Immutable()

    @Test
    fun implementations_should_be_data_classes()
    {
        ValidImmutable()

        // Invalid implementation (not a data class).
        assertFailsWith<NotImmutableError>
        {
            NoDataClass()
        }
    }


    /**
     * An incorrect implementation of [Immutable] because it contains a mutable property.
     */
    private data class ContainsVar( var invalidVar: String = "Invalid" ) : Immutable()

    private data class TypeWithVar( var invalidVar: String )
    /**
     * An incorrect implementation of [Immutable] because its member contains a mutable property.
     */
    private data class ContainsRecursiveVar( val containsVar: TypeWithVar = TypeWithVar( "Invalid" ) ) : Immutable()

    @Test
    fun implementations_should_only_contain_immutable_properties()
    {
        // All members need to be defined as 'val'.
        assertFailsWith<NotImmutableError>
        {
            ContainsVar()
        }

        // All members (recursively) need to be defined as 'val'.
        assertFailsWith<NotImmutableError>
        {
            ContainsRecursiveVar()
        }
    }


    abstract class AbstractImmutable : Immutable()
    private data class ImplementsImmutable( val oneMember: String ) : AbstractImmutable()
    private data class ContainsImmutable( val abstractMember: AbstractImmutable ) : Immutable()

    @Test
    fun implementations_may_contain_properties_which_guarantee_immutability_by_deriving_from_Immutable()
    {
        ContainsImmutable( ImplementsImmutable( "" ) )
    }


    data class WithList<out T>( val list: List<T> = listOf() ) : Immutable()

    @Test
    @Ignore
    fun kotlin_List_for_immutable_elements_should_be_allowed()
    {
        WithList<ValidImmutable>()

        assertFailsWith<NotImmutableError>
        {
            WithList<ContainsVar>()
        }
    }


    enum class WithNoVar { One, Two }
    enum class WithVar { One; var property: Int = 42 }

    @Test
    @Ignore
    fun enums_which_contain_no_vars_should_be_allowed()
    {
        WithNoVar.One

        assertFailsWith<NotImmutableError>
        {
            WithVar.One
        }
    }
}
