import * as extend from "@cachet/carp-studies-core-generated"
import * as kotlinStdLib from "@cachet/carp-kotlin"
import * as kotlinDateTime from "@cachet/carp-kotlinx-datetime"
import * as kotlinSerialization from "@cachet/carp-kotlinx-serialization"
import * as carpCommon from "@cachet/carp-common"
import * as carpDeployments from "@cachet/carp-deployments-core"


declare module "@cachet/carp-studies-core-generated"
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
        type Collection<T> = kotlinStdLib.kotlin.collections.Collection<T>
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


// Set namespace objects of dependent imported modules, so that they aren't "undefined" at runtime.
extend.dk.cachet.carp.common = carpCommon.dk.cachet.carp.common as any;
extend.dk.cachet.carp.deployments = carpDeployments.dk.cachet.carp.deployments as any;


// Export facade.
export * from "@cachet/carp-studies-core-generated"
