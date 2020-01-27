package dk.cachet.carp.common

import kotlin.test.*


/**
 * Tests for [DateTime].
 */
class DateTimeTest
{
    @Test
    fun toString_is_ISO8601_UTC()
    {
        val nowString = DateTime.now().toString()

        val isoRegex = Regex( """\d{4}-\d\d-\d\dT\d\d:\d\d:\d\dZ""" )
        val isIsoRepresentation = nowString.matches( isoRegex )
        assertTrue( isIsoRepresentation, "\"$nowString\" does not match the expected ISO format." )
    }
}
