package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.protocols.domain.ProtocolOwner
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

        val serialized: String = owner.toJson()
        val parsed: ProtocolOwner = ProtocolOwner.fromJson( serialized )

        assertEquals( owner, parsed )
    }
}
