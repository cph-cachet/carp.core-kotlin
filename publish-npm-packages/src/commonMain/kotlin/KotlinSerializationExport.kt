import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.js.JsExport


/**
 * Refers to types in kotlinx serialization that aren't JS exported.
 * Referring to them here guarantees that they are included in `$crossModule$` of generated JS sources.
 * This way, custom TypeScript declarations augmentations can access them.
 */
@JsExport
class KotlinSerializationExport
{
    val json = Json::class
    fun jsonMembers( json: Json ) =
        object
        {
            val encodeToString = json.encodeToString( String.serializer(), "Test" )
        }
}
