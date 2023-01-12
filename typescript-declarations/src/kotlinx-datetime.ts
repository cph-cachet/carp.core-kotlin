import * as kotlinxDateTime from "Kotlin-DateTime-library-kotlinx-datetime-js-ir"


declare module "Kotlin-DateTime-library-kotlinx-datetime-js-ir"
{
    // Base interfaces with better method names for internal types.
    namespace kotlinx.datetime
    {
        interface Clock
        {
            now(): Instant
        }
        namespace Clock
        {
            const System: Clock
        }
        interface Instant
        {
            toEpochMilliseconds(): number
        }
    }

    // Augment internal types to implement desired base interfaces.
    namespace $_$
    {
        interface System extends kotlinx.datetime.Clock {}
        abstract class System implements kotlinx.datetime.Clock {}
        interface Instant_0 extends kotlinx.datetime.Instant {}
        abstract class Instant_0 implements kotlinx.datetime.Instant {}
    }
}

// Implement base interfaces in internal types.
kotlinxDateTime.$_$.System.prototype.now = function(): kotlinxDateTime.kotlinx.datetime.Instant { return this.i10(); };
kotlinxDateTime.$_$.Instant_0.prototype.toEpochMilliseconds = function(): number { return this.t10(); };

// Export facade.
export * from "Kotlin-DateTime-library-kotlinx-datetime-js-ir"
export namespace kotlinx.datetime
{
    export type Clock = kotlinxDateTime.kotlinx.datetime.Clock
    export namespace Clock
    {
        export const System: Clock = kotlinxDateTime.$_$.System_getInstance()
    }
}
