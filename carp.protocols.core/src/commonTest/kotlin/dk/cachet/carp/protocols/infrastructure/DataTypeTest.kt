package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.FullyQualifiedName
import dk.cachet.carp.common.data.DataType
import kotlin.test.*


/**
 * Tests for [DataType] relying on core infrastructure.
 */
class DataTypeTest
{
    @Test
    fun can_serialize_and_deserialize_data_type_using_JSON()
    {
        val type = DataType( FullyQualifiedName( "some.namespace", "type" ) )

        val serialized = type.toJson()
        val parsed = DataType.fromJson( serialized )

        assertEquals( type, parsed )
    }
}
