@file:Suppress(
    "NON_EXPORTABLE_TYPE",
    "UNUSED_VARIABLE" // The variable names show up in generated JS sources which is useful to look up mangled names.
)

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.js.JsExport


/**
 * Refers to types/methods in the kotlinx serialization library to ensure they aren't removed from compiled sources
 * as part of the JS IR backend's compiler optimizations.
 * The exported JS sources for this class can also be used to look up mangled method names.
 */
@JsExport
class KotlinxSerializationExport
{
    fun json( json: Json )
    {
        val default = Json.Default
        val encodeToString = json.encodeToString( String.serializer(), "Test" )
        val decodeFromString = json.decodeFromString( String.serializer(), "Test" )
    }
}
