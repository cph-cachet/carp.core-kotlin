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
            const System: Instant
        }
        interface Instant
        {
            toEpochMilliseconds(): number
        }
    }

    // Augment internal types to implement desired base interfaces.
    namespace $_$
    {
        abstract class System implements kotlinx.datetime.Clock
        {
            now(): kotlinx.datetime.Instant
        }
        abstract class Instant_0 implements kotlinx.datetime.Instant
        {
            toEpochMilliseconds(): number
        }
    }
}

// Implement base interfaces in internal types.
kotlinxDateTime.$_$.System.prototype.now = function(): kotlinxDateTime.kotlinx.datetime.Instant { return this.hm(); };
kotlinxDateTime.$_$.Instant_0.prototype.toEpochMilliseconds = function(): number { return this.qm(); };

// Export facade.
export * from "Kotlin-DateTime-library-kotlinx-datetime-js-ir"
export namespace kotlinx.datetime.Clock
{
    export const System = kotlinxDateTime.$_$.System_getInstance()
}
