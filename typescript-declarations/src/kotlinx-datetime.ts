import * as extend from "Kotlin-DateTime-library-kotlinx-datetime-js-ir"


// Facade with better method names and type conversions for internal types.
export namespace kotlinx.datetime
{
    export interface Clock
    {
        now(): Instant
    }
    export namespace Clock
    {
        export const System: Clock = extend.$_$.System_getInstance()
    }
    export interface Instant
    {
        toEpochMilliseconds(): number
    }
}


// Augment internal types to implement facade.
declare module "Kotlin-DateTime-library-kotlinx-datetime-js-ir"
{
    namespace $_$
    {
        interface System extends kotlinx.datetime.Clock {}
        abstract class System implements kotlinx.datetime.Clock {}
        interface Instant_0 extends kotlinx.datetime.Instant {}
        abstract class Instant_0 implements kotlinx.datetime.Instant {}
    }
}


// Implement base interfaces in internal types.
extend.$_$.System.prototype.now = function(): kotlinx.datetime.Instant { return this.x11(); };
extend.$_$.Instant_0.prototype.toEpochMilliseconds = function(): number { return this.k12(); };


// Re-export augmented types.
export * from "Kotlin-DateTime-library-kotlinx-datetime-js-ir"
