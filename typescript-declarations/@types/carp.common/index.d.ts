declare module 'carp.common'
{
    import { Long } from 'kotlin'
    
    namespace dk.cachet.carp.common
    {
        class DateTime
        {
            constructor( msSinceUTC: Long )

            static get Companion(): DateTime$Companion

            readonly msSinceUTC: Long
            toString(): string
        }

        interface DateTime$Companion
        {
            serializer(): any;
            now(): DateTime;
        }

        class EmailAddress
        {
            constructor( address: string )

            static get Companion(): EmailAddress$Companion

            readonly address: string
        }

        interface EmailAddress$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.common.serialization
    {
        function createDefaultJSON_stpyu4$(): any
    }
}
