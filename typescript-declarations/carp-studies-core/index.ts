import extend from "@cachet/carp-studies-core-generated"
import kotlinStdLib from "@cachet/carp-kotlin"
import kotlinDateTime from "@cachet/carp-kotlinx-datetime"
import kotlinSerialization from "@cachet/carp-kotlinx-serialization"
import carpCommon from "@cachet/carp-common"
import carpDeployments from "@cachet/carp-deployments-core"


declare module "@cachet/carp-studies-core-generated"
{
    // Declare missing types for which no imports were generated.
    namespace kotlin
    {
        type Long = kotlinStdLib.Long
    }
    namespace kotlin.reflect
    {
        // When used as a type parameter for a type exported through `forced-exports`, normally compiled as `any`,
        // `KClass` can't be resolved. But, no facade is implemented for `KClass` as it isn't needed yet by TS clients.
        type KClass<T> = any
    }
    namespace kotlin.time
    {
        type Duration = kotlinStdLib.time.Duration
    }
    namespace kotlin.collections
    {
        type Collection<T> = kotlinStdLib.collections.Collection<T>
        type List<T> = kotlinStdLib.collections.List<T>
        type Set<T> = kotlinStdLib.collections.Set<T>
        type Map<K, V> = kotlinStdLib.collections.Map<K, V>
    }
    namespace kotlinx.datetime
    {
        type Instant = kotlinDateTime.datetime.Instant
    }
    namespace kotlinx.serialization.json
    {
        type Json = kotlinSerialization.serialization.json.Json
    }
}


// Set namespace objects of dependent imported modules, so that they aren't "undefined" at runtime.
extend.dk.cachet.carp.common = carpCommon.dk.cachet.carp.common as any;
extend.dk.cachet.carp.deployments = carpDeployments.dk.cachet.carp.deployments as any;


// Re-export augmented types.
export { default } from "@cachet/carp-studies-core-generated"
