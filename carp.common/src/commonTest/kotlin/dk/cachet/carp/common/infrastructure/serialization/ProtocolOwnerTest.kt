package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.domain.ProtocolOwner
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
