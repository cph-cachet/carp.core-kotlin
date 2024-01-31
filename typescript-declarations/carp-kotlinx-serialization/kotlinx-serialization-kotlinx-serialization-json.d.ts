declare module "@cachet/kotlinx-serialization-kotlinx-serialization-json"
{
    namespace $_$
    {
        interface JsonImpl
        {
            // encodeToString
            t13( serializer: any, instance: any ): string

            // decodeFromString
            u13( serializer: any, string: string ): string
        }
        function Default_getInstance(): JsonImpl
    }
}
