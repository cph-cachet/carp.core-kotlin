@file:Suppress( "MagicNumber" )

import kotlin.js.JsExport


/**
 * Refers to types in the kotlin standard library that aren't JS exported.
 * Referring to them here guarantees that they are included in `$crossModule$` of generated JS sources.
 * This way, custom TypeScript declarations augmentations can access them.
 */
@JsExport
class KotlinExport
{
    val list = List::class
    val listOf = listOf( 42, 43 )

    val set = Set::class

    fun collectionMembers( collection: Collection<*> ) =
        object
        {
            val contains = collection.contains( 42 )
        }
}
