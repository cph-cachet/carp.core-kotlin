declare module "@cachet/kotlinx-serialization-kotlinx-serialization-json"
{
    namespace $_$
    {
        interface JsonImpl
        {
            // encodeToString
            h12( serializer: any, instance: any ): string

            // decodeFromString
            i12( serializer: any, string: string ): string
        }
        function Default_getInstance(): JsonImpl
    }
}
