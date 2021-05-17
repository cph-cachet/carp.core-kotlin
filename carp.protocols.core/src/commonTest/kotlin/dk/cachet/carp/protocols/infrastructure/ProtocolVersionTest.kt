package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.application.ProtocolVersion
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [ProtocolVersion] relying on core infrastructure.
 */
class ProtocolVersionTest
{
    @Test
    fun can_serialize_and_deserialize_protocol_version_using_JSON()
    {
        val version = ProtocolVersion( "Test", DateTime.now() )

        val serialized: String = JSON.encodeToString( version )
        val parsed: ProtocolVersion = JSON.decodeFromString( serialized )

        assertEquals( version, parsed )
    }
}
