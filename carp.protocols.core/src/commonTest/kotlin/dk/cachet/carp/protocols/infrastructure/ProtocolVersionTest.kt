package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.protocols.domain.ProtocolVersion
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

        val serialized: String = version.toJson()
        val parsed: ProtocolVersion = ProtocolVersion.fromJson( serialized )

        assertEquals( version, parsed )
    }
}
