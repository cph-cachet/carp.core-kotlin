import { Instant } from "@js-joda/core"
import * as kotlinDateTime from "Kotlin-DateTime-library-kotlinx-datetime-js-ir"


export namespace kotlinx.datetime
{
    export type Instant = kotlinDateTime.$crossModule$.Instant
    export namespace Instant
    {
        export const serializer = kotlinDateTime.$crossModule$.InstantIso8601Serializer_getInstance
    }

    export interface Clock
    {
        now(): Instant
    }

    export namespace Clock
    {
        export const System: Clock = {
            now(): Instant { return kotlinDateTime.$crossModule$.System_getInstance().now_0_k$() }
        }
    }
}
