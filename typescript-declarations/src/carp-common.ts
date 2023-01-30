import * as extend from "carp.core-kotlin-carp.common"
import * as kotlinStdLib from "./kotlin"
import * as kotlinDateTime from "./kotlinx-datetime"


declare module "carp.core-kotlin-carp.common"
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


    // Augment internal types to implement desired base interfaces.
    namespace dk.cachet.carp.common.application.users
    {
        interface AccountIdentity {}
        interface EmailAccountIdentity extends AccountIdentity {}
        interface UsernameAccountIdentity extends AccountIdentity {}
    }
}


// Export facade.
export * from "carp.core-kotlin-carp.common"
