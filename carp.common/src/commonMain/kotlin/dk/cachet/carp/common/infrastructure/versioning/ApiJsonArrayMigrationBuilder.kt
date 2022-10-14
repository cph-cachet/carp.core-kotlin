package dk.cachet.carp.common.infrastructure.versioning

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject


/**
 * A builder to help migrate a [JsonArray] part of an application service API
 * from [minimumMinorVersion] to [targetMinorVersion].
 */
class ApiJsonArrayMigrationBuilder(
    jsonArray: JsonArray,
    val minimumMinorVersion: Int,
    val targetMinorVersion: Int
)
{
    val json: MutableMap<Int, JsonElement> = jsonArray
        .mapIndexed { index, value -> index to value }
        .toMap()
        .toMutableMap()

    fun objects( migration: ApiJsonObjectMigrationBuilder.() -> Unit )
    {
        for ( (index, o) in json.filter { it.value is JsonObject } )
        {
            json.remove( index )
            val newJson = ApiJsonObjectMigrationBuilder( o as JsonObject, minimumMinorVersion, targetMinorVersion )
                .apply( migration ).build()
            json[ index ] = newJson
        }
    }

    fun build(): JsonArray = JsonArray( json.values.toList() )
}
