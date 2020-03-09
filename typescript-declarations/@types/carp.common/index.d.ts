declare module 'carp.common'
{
    import { Long } from 'kotlin'
    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-runtime'
    import Json = kotlinx.serialization.json.Json
    
    
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


        class TimeSpan
        {
            constructor( microseconds: Long )

            static get Companion(): TimeSpan$Companion

            get totalMilliseconds(): number
        }
        interface TimeSpan$Companion
        {
            readonly INFINITE: TimeSpan;
            serializer(): any;
            fromMilliseconds_14dthe$( ms: number ): TimeSpan;
        }


        class Trilean
        {
            static readonly TRUE: Trilean
            static readonly FALSE: Trilean
            static readonly UNKNOWN: Trilean
            static values(): Array<Trilean>
        }
        function toTrilean_1v8dcc$( bool: boolean ): Trilean


        class UUID
        {
            constructor( stringRepresentation: string )

            static get Companion(): UUID$Companion

            readonly stringRepresentation: string
        }
        interface UUID$Companion
        {
            serializer(): any;
            randomUUID(): UUID;
        }
    }


    namespace dk.cachet.carp.common.users
    {
        abstract class AccountIdentity
        {
            static get Factory(): AccountIdentity$Factory
        }
        interface AccountIdentity$Factory
        {
            fromEmailAddress_61zpoe$( emailAddress: string ): EmailAccountIdentity;
            fromUsername_61zpoe$( username: string ): UsernameAccountIdentity;
        }

        class EmailAccountIdentity extends AccountIdentity
        {
            constructor( emailAddress: EmailAddress )

            static get Companion(): EmailAccountIdentity$Companion

            readonly emailAddress: EmailAddress
        }
        function EmailAccountIdentity_init_61zpoe$( emailAddress: string ): EmailAccountIdentity
        interface EmailAccountIdentity$Companion { serializer(): any }

        class UsernameAccountIdentity extends AccountIdentity
        {
            constructor( username: string )

            static get Companion(): UsernameAccountIdentity$Companion

            readonly username: string
        } 
        interface UsernameAccountIdentity$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.common.serialization
    {
        function createDefaultJSON_stpyu4$(): Json
    }
}
