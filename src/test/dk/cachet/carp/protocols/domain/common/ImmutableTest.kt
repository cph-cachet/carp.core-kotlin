package dk.cachet.carp.protocols.domain.common

import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test


/**
 * Tests for [Immutable].
 *
 * TODO: Verify whether this works for generics (with type parameters), nullable types, and other potential special cases.
 */
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
    fun `implementations should be data classes`()
    {
        ValidImmutable()

        // Invalid implementation (not a data class).
        assertFailsWith<Immutable.NotImmutableError>
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
    private data class ContainsRecursiveVar( val containsVar: TypeWithVar = TypeWithVar("Invalid" ) ) : Immutable()

    @Test
    fun `implementations should only contain immutable properties`()
    {
        // All members need to be defined as 'val'.
        assertFailsWith<Immutable.NotImmutableError>
        {
            ContainsVar()
        }

        // All members (recursively) need to be defined as 'val'.
        assertFailsWith<Immutable.NotImmutableError>
        {
            ContainsRecursiveVar()
        }
    }


    abstract class AbstractImmutable : Immutable()
    private data class ImplementsImmutable( val oneMember: String ) : AbstractImmutable()
    private data class ContainsImmutable( val abstractMember: AbstractImmutable ) : Immutable()

    @Test
    fun `implementations may contain properties which guarantee immutability by deriving from Immutable`()
    {
        ContainsImmutable( ImplementsImmutable( "" ) )
    }


    data class WithList<out T>(val list: List<T> = listOf() ) : Immutable()

    @Test
    fun `kotlin List for immutable elements should be allowed`()
    {
        WithList<ValidImmutable>()

        assertFailsWith<Immutable.NotImmutableError>
        {
            WithList<ContainsVar>()
        }
    }


    enum class WithNoVar { One, Two }
    enum class WithVar { One; var property: Int = 42 }

    @Test
    fun `enums which contain no vars should be allowed`()
    {
        WithNoVar.One

        assertFailsWith<Immutable.NotImmutableError>
        {
            WithVar.One
        }
    }
}