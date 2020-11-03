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
    fun defaultFormat_is_ISO8601_UTC()
    {
        val nowString = DateTime.now().defaultFormat()

        val isIsoRepresentation = nowString.matches( isoRegex )
        assertTrue( isIsoRepresentation, "\"$nowString\" does not match the expected ISO format." )
    }

    @Test
    fun defaultFormat_has_precision_of_3_decimal_fraction_digits()
    {
        val utcZero = DateTime( 0 ).defaultFormat() // Ends in all zeros, so tailing zeros might be omitted.

        val isIsoRepresentation = utcZero.matches( isoRegex )
        assertTrue( isIsoRepresentation, "\"$utcZero\" does not match the expected ISO format." )
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
