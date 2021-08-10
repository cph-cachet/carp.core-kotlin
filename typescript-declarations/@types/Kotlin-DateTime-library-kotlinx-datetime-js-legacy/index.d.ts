declare module 'Kotlin-DateTime-library-kotlinx-datetime-js-legacy'
{
    import { Long } from 'kotlin'

    namespace kotlinx.datetime
    {
        class Clock
        {
            static get System(): Clock$System
        }
        interface Clock$System
        {
            now(): Instant
        }

        class Instant
        {
            static get Companion(): Instant$Companion

            toEpochMilliseconds(): Long
        }
        interface Instant$Companion { serializer(): any }
    }
}
