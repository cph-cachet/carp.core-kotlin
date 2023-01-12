declare module "kotlinx-serialization-kotlinx-serialization-json-js-ir"
{
    namespace $_$
    {
        interface JsonImpl
        {
            // encodeToString
            w10( serializer: any, instance: any ): string

            // decodeFromString
            x10( serializer: any, string: string ): string
        }
        function Default_getInstance(): JsonImpl
    }
}
