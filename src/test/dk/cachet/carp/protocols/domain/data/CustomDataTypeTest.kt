package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.UnknownDataType
import kotlinx.serialization.json.JSON
import org.junit.jupiter.api.Test


/**
 * Tests for [CustomDataType].
 */
class CustomDataTypeTest
{
    @Test
    fun `initialization from json succeeds`()
    {
        val type = UnknownDataType( "Test" )
        val serialized: String = JSON.stringify( type )

        CustomDataType( UnknownDataType::class.qualifiedName!!, serialized )
    }
}