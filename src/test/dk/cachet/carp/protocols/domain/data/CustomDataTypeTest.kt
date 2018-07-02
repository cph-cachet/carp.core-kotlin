package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.UnknownDataType
import kotlinx.serialization.json.JSON
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


/**
 * Tests for [CustomDataType].
 */
class CustomDataTypeTest
{
    @Test
    fun `initialization from json succeeds`()
    {
        val type = UnknownDataType()
        val serialized: String = JSON.stringify( type )

        val custom = CustomDataType( UnknownDataType::class.qualifiedName!!, serialized )
        assertEquals( type.category, custom.category )
    }
}