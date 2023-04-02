import * as extend from "carp-protocols-core-generated"
import * as kotlinStdLib from "./kotlin"
import * as kotlinDateTime from "./kotlinx-datetime"
import * as kotlinSerialization from "./kotlinx-serialization"


declare module "carp-protocols-core-generated"
{
    // Declare missing types for which no imports were generated.
    namespace kotlin
    {
        type Long = kotlinStdLib.kotlin.Long
    }
    namespace kotlin.reflect
    {
        // When used as a type parameter for a type exported through `forced-exports`, normally compiled as `any`,
        // `KClass` can't be resolved. But, no facade is implemented for `KClass` as it isn't needed yet by TS clients.
        type KClass<T> = any
    }
    namespace kotlin.time
    {
        type Duration = kotlinStdLib.kotlin.time.Duration
    }
    namespace kotlin.collections
    {
        type List<T> = kotlinStdLib.kotlin.collections.List<T>
        type Set<T> = kotlinStdLib.kotlin.collections.Set<T>
        type Map<K, V> = kotlinStdLib.kotlin.collections.Map<K, V>
    }
    namespace kotlinx.datetime
    {
        type Instant = kotlinDateTime.kotlinx.datetime.Instant
    }
    namespace kotlinx.serialization.json
    {
        type Json = kotlinSerialization.kotlinx.serialization.json.Json
    }
}


// Export facade.
export * from "carp-protocols-core-generated"
