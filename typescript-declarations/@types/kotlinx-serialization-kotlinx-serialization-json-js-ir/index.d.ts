declare module "kotlinx-serialization-kotlinx-serialization-json-js-ir"
{
    namespace $_$
    {
        interface JsonImpl
        {
            // encodeToString
            v12( serializer: any, instance: any ): string

            // decodeFromString
            w12( serializer: any, string: string ): string
        }
        function Default_getInstance(): JsonImpl
    }
}
