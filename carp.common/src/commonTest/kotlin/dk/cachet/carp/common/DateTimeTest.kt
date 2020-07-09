package dk.cachet.carp.common

import dk.cachet.carp.common.serialization.createDefaultJSON
import kotlin.test.*


/**
 * Tests for [DateTime].
 */
class DateTimeTest
{
    private val isoRegex = Regex( """\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d.\d{3}Z""" )

    @Test
    fun toString_is_ISO8601_UTC()
    {
        val nowString = DateTime.now().toString()

        val isIsoRepresentation = nowString.matches( isoRegex )
        assertTrue( isIsoRepresentation, "\"$nowString\" does not match the expected ISO format." )
    }

    @Test
    fun toString_has_precision_of_3_decimal_fraction_digits()
    {
        val utcZero = DateTime( 0 ).toString() // Ends in all zeros, so tailing zeros might be omitted.

        val isIsoRepresentation = utcZero.matches( isoRegex )
        assertTrue( isIsoRepresentation, "\"$utcZero\" does not match the expected ISO format." )
    }

    @Test
    fun can_serialize_and_deserialize_DateTime_using_JSON()
    {
        val dateTime = DateTime.now()

        val json = createDefaultJSON()
        val serialized = json.stringify( DateTime.serializer(), dateTime )
        val parsed = json.parse( DateTime.serializer(), serialized )

        assertEquals( dateTime, parsed )
    }
}
