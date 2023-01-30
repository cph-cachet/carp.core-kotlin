import * as extend from "carp.core-kotlin-carp.data.core"
import * as kotlinStdLib from "./kotlin"
import * as kotlinDateTime from "./kotlinx-datetime"


declare module "carp.core-kotlin-carp.data.core"
{
    // Declare missing types for which no imports were generated.
    namespace kotlin
    {
        type Long = kotlinStdLib.kotlin.Long
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
}


// Export facade.
export * from "carp.core-kotlin-carp.data.core"
