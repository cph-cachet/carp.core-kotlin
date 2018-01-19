package bhrp.studyprotocols.domain.common

import org.junit.jupiter.api.*
import kotlin.test.*


/**
 * Tests for [ExtractUniqueKeyMap].
 */
class ExtractUniqueKeyMapTest
{
    class SomeObject( val key: String )


    @Test
    fun `tryAddIfKeyIsNew succeeds`()
    {
        val map = ExtractUniqueKeyMap<String, SomeObject>( { s -> s.key }, Error() )
        val toAdd = SomeObject( "New" )

        val isAdded: Boolean = map.tryAddIfKeyIsNew( toAdd )
        assertTrue( isAdded )
        assertEquals( map[ "New" ], toAdd )
    }

    @Test
    fun `tryAddIfKeyIsNew multiple times only adds first time`()
    {
        val map = ExtractUniqueKeyMap<String, SomeObject>( { s -> s.key }, Error() )
        val toAdd = SomeObject( "Test" )
        map.tryAddIfKeyIsNew( toAdd )

        val isAdded: Boolean = map.tryAddIfKeyIsNew( toAdd )
        assertFalse( isAdded )
        assertEquals( 1, map.count() )
    }

    @Test
    fun `tryAddIfKeyIsNew throws on existing key`()
    {
        val error = Error( "Fails" )
        val map = ExtractUniqueKeyMap<String, SomeObject>( {s -> s.key }, error )
        map.tryAddIfKeyIsNew( SomeObject( "Existing" ) )

        assertFailsWith<Error>("Fails" ) { map.tryAddIfKeyIsNew( SomeObject( "Existing" ) ) }
    }

    @Test
    fun `remove succeeds`()
    {
        val map = ExtractUniqueKeyMap<String, SomeObject>( {s -> s.key }, Error() )
        val toRemove = SomeObject( "Remove" )
        map.tryAddIfKeyIsNew( toRemove )

        val isRemoved = map.remove( toRemove )
        assertTrue( isRemoved )
    }

    @Test
    fun `remove returns false when element not present`()
    {
        val map = ExtractUniqueKeyMap<String, SomeObject>( {s -> s.key }, Error() )
        val toRemove = SomeObject( "Remove" )

        val isRemoved = map.remove( toRemove )
        assertFalse( isRemoved )
    }
}