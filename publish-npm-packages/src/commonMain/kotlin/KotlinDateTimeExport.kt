import kotlinx.datetime.Instant
import kotlin.js.JsExport


/**
 * Refers to types in kotlinx datetime that aren't JS exported.
 * Referring to them here guarantees that they are included in `$crossModule$` of generated JS sources.
 * This way, custom TypeScript declarations augmentations can access them.
 */
@JsExport
class KotlinDateTimeExport
{
    val instant = Instant::class
}
