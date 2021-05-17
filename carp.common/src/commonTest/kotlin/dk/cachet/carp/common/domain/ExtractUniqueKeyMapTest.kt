package dk.cachet.carp.common.domain

import kotlin.test.*


/**
 * Tests for [ExtractUniqueKeyMap].
 */
class ExtractUniqueKeyMapTest
{
    class SomeObject( val key: String )


    @Test
    fun tryAddIfKeyIsNew_succeeds()
    {
        val map = ExtractUniqueKeyMap<String, SomeObject>( { s -> s.key } ) { Error() }
        val toAdd = SomeObject( "New" )

        val isAdded: Boolean = map.tryAddIfKeyIsNew( toAdd )
        assertTrue( isAdded )
        assertEquals( map[ "New" ], toAdd )
    }

    @Test
    fun tryAddIfKeyIsNew_multiple_times_only_adds_first_time()
    {
        val map = ExtractUniqueKeyMap<String, SomeObject>( { s -> s.key } ) { Error() }
        val toAdd = SomeObject( "Test" )
        map.tryAddIfKeyIsNew( toAdd )

        val isAdded: Boolean = map.tryAddIfKeyIsNew( toAdd )
        assertFalse( isAdded )
        assertEquals( 1, map.count() )
    }

    @Test
    fun tryAddIfKeyIsNew_throws_on_existing_key()
    {
        val map = ExtractUniqueKeyMap<String, SomeObject>( { s -> s.key } ) { key -> Error( "$key exists" ) }
        map.tryAddIfKeyIsNew( SomeObject( "Existing" ) )

        assertFailsWith<Error>("Existing exists" ) { map.tryAddIfKeyIsNew( SomeObject( "Existing" ) ) }
    }

    @Test
    fun remove_succeeds()
    {
        val map = ExtractUniqueKeyMap<String, SomeObject>( { s -> s.key } ) { Error() }
        val toRemove = SomeObject( "Remove" )
        map.tryAddIfKeyIsNew( toRemove )

        val isRemoved = map.remove( toRemove )
        assertTrue( isRemoved )
    }

    @Test
    fun remove_returns_false_when_element_not_present()
    {
        val map = ExtractUniqueKeyMap<String, SomeObject>( { s -> s.key } ) { Error() }
        val toRemove = SomeObject( "Remove" )

        val isRemoved = map.remove( toRemove )
        assertFalse( isRemoved )
    }
}
