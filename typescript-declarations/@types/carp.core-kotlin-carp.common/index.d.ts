declare module 'carp.core-kotlin-carp.common'
{
    import { kotlin } from 'kotlin'
    import { Long } from 'kotlin'
    import HashSet = kotlin.collections.HashSet
    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-jsLegacy'
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
            fromString_61zpoe$( s: string ): DateTime
        }

        
        class EmailAddress
        {
            constructor( address: string )

            static get Companion(): EmailAddress$Companion

            readonly address: string
        }
        interface EmailAddress$Companion { serializer(): any }


        class NamespacedId
        {
            constructor( namespace: string, name: string )

            static get Companion(): NamespacedId$Companion

            readonly namespace: string
            readonly name: string
        }
        interface NamespacedId$Companion { serializer(): any }


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
        import InputElement = dk.cachet.carp.common.data.input.InputElement
        import InputDataTypeList = dk.cachet.carp.common.data.input.InputDataTypeList


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


        abstract class ParticipantAttribute
        {
            static get Companion(): ParticipantAttribute$Companion

            readonly inputType: NamespacedId

            getInputElement_zbztje$( registeredInputDataTypes: InputDataTypeList ): InputElement
            isValidInput_jon1ci$( registeredInputDataTypes: InputDataTypeList, input: any ): boolean
            inputToData_jon1ci$( registeredInputDataTypes: InputDataTypeList, input: any ): any
            isValidData_1evfk3$( registeredInputDataTypes: InputDataTypeList, data: any ): boolean
            dataToInput_1evfk3$( registeredInputDataTypes: InputDataTypeList, data: any ): any
        }
        interface ParticipantAttribute$Companion { serializer(): any }

        namespace ParticipantAttribute
        {
            class DefaultParticipantAttribute extends ParticipantAttribute
            {
                constructor( inputType: NamespacedId )
            }

            class CustomParticipantAttribute extends ParticipantAttribute
            {
                constructor( input: InputElement )
            }
        }
    }


    namespace dk.cachet.carp.common.data.input
    {
        interface InputElement
        {
            readonly name: string

            isValid_trkh7z$( input: any ): boolean
        }

        // No need to initialize this from TypeScript right now. Access to `CarpInputDataTypes` is sufficient.
        class InputDataTypeList { constructor() }
        const CarpInputDataTypes: InputDataTypeList
    }

    namespace dk.cachet.carp.common.data.input.element
    {
        import InputElement = dk.cachet.carp.common.data.input.InputElement

        
        class Text implements InputElement
        {
            constructor( name: string )

            static get Companion(): Text$Companion

            readonly name: string
            isValid_trkh7z$( input: any ): boolean
        }
        interface Text$Companion { serializer(): any }

        class SelectOne implements InputElement
        {
            constructor( name: string, options: HashSet<string> )

            static get Companion(): SelectOne$Companion

            readonly name: string
            readonly options: HashSet<string>
            isValid_trkh7z$( input: any ): boolean
        }
        interface SelectOne$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.common.serialization
    {
        function createDefaultJSON_18xi4u$(): Json
    }
}
