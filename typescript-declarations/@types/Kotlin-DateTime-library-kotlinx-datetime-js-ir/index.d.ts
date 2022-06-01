declare module 'Kotlin-DateTime-library-kotlinx-datetime-js-ir'
{
    interface System
    {
        now_0_k$(): $crossModule$.Instant
    }

    namespace $crossModule$
    {
        abstract class Instant { }
        function System_getInstance(): System
        function InstantIso8601Serializer_getInstance(): any
    }
}
