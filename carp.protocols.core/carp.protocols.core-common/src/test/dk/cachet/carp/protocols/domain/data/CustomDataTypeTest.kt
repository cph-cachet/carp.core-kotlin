package dk.cachet.carp.protocols.domain.data

import dk.cachet.carp.protocols.domain.*
import kotlinx.serialization.json.JSON
import kotlin.test.*


/**
 * Tests for [CustomDataType].
 */
@JsIgnore
class CustomDataTypeTest
{
    @Test
    fun initialization_from_json_succeeds()
    {
        val type = UnknownDataType()
        val serialized: String = JSON.stringify( type )

        val custom = CustomDataType( "Irrelevant", serialized )
        assertEquals( type.category, custom.category )
    }
}