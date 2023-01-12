import * as extend from "kotlinx-serialization-kotlinx-serialization-json-js-ir"


declare module "kotlinx-serialization-kotlinx-serialization-json-js-ir"
{
    // Base interfaces with better method names for internal types.
    namespace kotlinx.serialization
    {
        interface Json
        {
            encodeToString( serializer: any, value: any ): string
            decodeFromString( serializer: any, string: string ): any
        }
        namespace Json
        {
            const Default: Json
        }
    }

    // Augment internal types to implement desired base interfaces.
    namespace $_$
    {
        abstract class JsonImpl implements kotlinx.serialization.Json
        {
            encodeToString( serializer: any, value: any ): string
            decodeFromString( serializer: any, string: string ): any
        }
    }
}

// Implement base interfaces in internal types.
extend.$_$.JsonImpl.prototype.encodeToString =
    function( serializer: any, value: any ): string
    {
        return this.w10( serializer, value );
    };
extend.$_$.JsonImpl.prototype.decodeFromString =
    function( serializer: any, string: string ): any
    {
        return this.x10( serializer, string );
    };

// Export facade.
export * from "kotlinx-serialization-kotlinx-serialization-json-js-ir"
export namespace kotlinx.serialization
{
    export type Json = extend.kotlinx.serialization.Json
    export namespace Json
    {
        export const Default = extend.$_$.Default_getInstance()
    }
}
