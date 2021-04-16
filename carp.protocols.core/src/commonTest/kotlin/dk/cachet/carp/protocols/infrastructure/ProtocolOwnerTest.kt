package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.domain.ProtocolOwner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.*


/**
 * Tests for [ProtocolOwner] relying on core infrastructure.
 */
class ProtocolOwnerTest
{
    @Test
    fun can_serialize_and_deserialize_protocol_owner_using_JSON()
    {
        val owner = ProtocolOwner()

        val serialized: String = JSON.encodeToString( owner )
        val parsed: ProtocolOwner = JSON.decodeFromString( serialized )

        assertEquals( owner, parsed )
    }
}
