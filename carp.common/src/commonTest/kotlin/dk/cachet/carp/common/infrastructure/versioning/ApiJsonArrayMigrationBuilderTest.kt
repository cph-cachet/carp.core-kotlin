package dk.cachet.carp.common.infrastructure.versioning

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlin.test.*


/**
 * Tests for [ApiJsonArrayMigrationBuilder].
 */
class ApiJsonArrayMigrationBuilderTest
{
    @Test
    fun objects_filters_out_objects()
    {
        val valueField = "value"
        val arrayContents = listOf(
            JsonObject( mapOf( valueField to JsonPrimitive( 1 ) ) ),
            JsonPrimitive( 2 ),
            JsonObject( mapOf( valueField to JsonPrimitive( 3 ) ) )
        )
        val jsonArray = JsonArray( arrayContents )

        val newValue = 42
        val migrated = migrate( jsonArray ) {
            objects {
                json[ valueField ] = JsonPrimitive( newValue )
            }
        }

        assertEquals( arrayContents.size, migrated.size )
        migrated.filterIsInstance<JsonObject>().forEach {
            assertEquals( newValue, (it[ valueField ] as? JsonPrimitive)?.int )
        }
    }

    private fun migrate( json: JsonArray, migration: ApiJsonArrayMigrationBuilder.() -> Unit ): JsonArray =
        ApiJsonArrayMigrationBuilder( json, 0, 1 ).apply( migration ).build()
}
