package dk.cachet.carp.common

import dk.cachet.carp.common.serialization.createDefaultJSON
import kotlin.test.*


/**
 * Tests for [MACAddress].
 */
class MACAddressTest
{
    @Test
    fun can_serialize_and_deserialize_MACAddress_using_JSON()
    {
        val mac = MACAddress( "00-11-22-33-44-55" )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( MACAddress.serializer(), mac )
        val parsed = json.decodeFromString( MACAddress.serializer(), serialized )

        assertEquals( mac, parsed )
    }

    @Test
    fun cant_initialize_incorrect_MACAddress()
    {
        // Incorrect addresses.
        assertFailsWith<IllegalArgumentException> { MACAddress( "Invalid" ) }
        assertFailsWith<IllegalArgumentException> { MACAddress( "00-11-22-33-44" ) } // Not long enough.
        assertFailsWith<IllegalArgumentException> { MACAddress( "00-00-00-00-00-00-FF" ) } // Too long.
        assertFailsWith<IllegalArgumentException> { MACAddress( "... 00-00-00-00-00-00 ..." ) } // Excess chars.
        assertFailsWith<IllegalArgumentException> { MACAddress( "G0-00-00-00-00-00" ) } // Invalid character.

        // Non-standard formats.
        assertFailsWith<IllegalArgumentException> { MACAddress( "aa-bb-cc-dd-ee-ff" ) } // Lower case.
        assertFailsWith<IllegalArgumentException> { MACAddress( "00:11:22:33:44:55" ) } // Incorrect separators.
    }

    @Test
    fun parse_succeeds_with_alternate_representations()
    {
        val expected = "AA-BB-CC-DD-EE-FF"

        val lowerCase = MACAddress.parse( "aa-bb-cc-dd-ee-ff" )
        assertEquals( expected, lowerCase.address )

        val mixedCase = MACAddress.parse( "aA-bB-cC-dD-eE-fF" )
        assertEquals( expected, mixedCase.address )

        val colonSeparator = MACAddress.parse( "AA:BB:CC:DD:EE:FF" )
        assertEquals( expected, colonSeparator.address )
    }

    @Test
    fun parse_fails_for_unsupported_representations()
    {
        // Mixed separators
        assertFailsWith<IllegalArgumentException> { MACAddress.parse( "00-11:22-33:44-55" ) }

        // Unsupported separators.
        assertFailsWith<IllegalArgumentException> { MACAddress.parse( "00 11 22 33 44 55" ) }
    }
}
