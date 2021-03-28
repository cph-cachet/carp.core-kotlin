package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.*


/**
 * Tests for [DateTime].
 */
class DateTimeTest
{
    private val isoRegex = Regex( """\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d(.\d{1,9})?Z""" )

    @Test
    fun toString_is_ISO8601_UTC()
    {
        val nowString = DateTime.now().toString()

        val isIsoRepresentation = nowString.matches( isoRegex )
        assertTrue( isIsoRepresentation, "\"$nowString\" does not match the expected ISO format." )
    }

    @Test
    fun toString_may_omit_fractional_seconds()
    {
        val utcZero = DateTime( 0 ).toString() // Ends in all zeros, so tailing zeros might be omitted.

        val isIsoRepresentation = utcZero.matches( isoRegex )
        assertTrue( isIsoRepresentation, "\"$utcZero\" does not match the expected ISO format." )
    }

    @Test
    fun toString_must_be_lossless()
    {
        val dateTime = DateTime( 1234567891234 )

        val string = dateTime.toString()
        val parsed = DateTime.fromString( string )

        assertEquals( dateTime, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_DateTime_using_JSON()
    {
        val dateTime = DateTime.now()

        val json = createDefaultJSON()
        val serialized = json.encodeToString( DateTime.serializer(), dateTime )
        val parsed = json.decodeFromString( DateTime.serializer(), serialized )

        assertEquals( dateTime, parsed )
    }
}
