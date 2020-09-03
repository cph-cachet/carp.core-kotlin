package dk.cachet.carp.common

import dk.cachet.carp.common.serialization.createDefaultJSON
import kotlin.test.*


/**
 * Tests for [TimeOfDay].
 */
class TimeOfDayTest
{
    @Test
    fun cant_initialize_incorrect_TimeOfDay()
    {
        assertFailsWith<IllegalArgumentException> { TimeOfDay( -1 ) }
        assertFailsWith<IllegalArgumentException> { TimeOfDay( 24 ) }
        assertFailsWith<IllegalArgumentException> { TimeOfDay( 0, -1 ) }
        assertFailsWith<IllegalArgumentException> { TimeOfDay( 0, 60 ) }
        assertFailsWith<IllegalArgumentException> { TimeOfDay( 0, 0, -1 ) }
        assertFailsWith<IllegalArgumentException> { TimeOfDay( 0, 0, 60 ) }
    }

    @Test
    fun toString_is_ISO8601()
    {
        assertEquals( "12:00:00", TimeOfDay( 12 ).toString() )
        assertEquals( "03:00:00", TimeOfDay( 3 ).toString() )
        assertEquals( "12:34:56", TimeOfDay( 12, 34, 56 ).toString() )
    }

    @Test
    fun can_serialize_and_deserialize_TimeOfDay_using_JSON()
    {
        val time = TimeOfDay( 12 )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( TimeOfDay.serializer(), time )
        val parsed = json.decodeFromString( TimeOfDay.serializer(), serialized )

        assertEquals( time, parsed )
    }
}
