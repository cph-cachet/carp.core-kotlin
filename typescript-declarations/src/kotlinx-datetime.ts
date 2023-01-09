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
kotlinxDateTime.$_$.System.prototype.now = function(): kotlinx.datetime.Instant { return this.c5(); };
kotlinxDateTime.$_$.Instant_0.prototype.toEpochMilliseconds = function(): number { return this.k5(); };

// Export facade.
export namespace kotlinx.datetime
{
    export type Clock = kotlinxDateTime.kotlinx.datetime.Clock
    export type Instant = kotlinxDateTime.kotlinx.datetime.Instant
    export namespace Clock
    {
        export const System: kotlinx.datetime.Clock = kotlinxDateTime.$_$.System_getInstance()
    }
}
