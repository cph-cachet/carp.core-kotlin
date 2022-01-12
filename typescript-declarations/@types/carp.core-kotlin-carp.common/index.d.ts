declare module 'carp.core-kotlin-carp.common'
{
    import { kotlin } from 'kotlin'
    import HashMap = kotlin.collections.HashMap
    import HashSet = kotlin.collections.HashSet
    import Duration = kotlin.time.Duration

    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
    import Json = kotlinx.serialization.json.Json

    import { kotlinx as kxd } from 'Kotlin-DateTime-library-kotlinx-datetime-js-legacy'
    import Instant = kxd.datetime.Instant
    
    
    namespace dk.cachet.carp.common.application
    {
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

        class RecurrenceRule
        {
            static get Companion(): RecurrenceRule$Companion

            toString(): string
        }
        interface RecurrenceRule$Companion
        {
            serializer(): any
            fromString_61zpoe$( rrule: string ): RecurrenceRule
        }

        class TimeOfDay
        {
            constructor( hour: Number, minutes: Number, seconds: Number )

            static get Companion(): TimeOfDay$Companion
        }
        interface TimeOfDay$Companion { serializer(): any }

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


    namespace dk.cachet.carp.common.application.devices
    {
        abstract class DeviceDescriptor
        {
            readonly roleName: string
        }

        abstract class DeviceRegistration
        {
            static get Companion(): DeviceRegistration$Companion  
            
            readonly deviceId: string
            readonly deviceDisplayName: string | null;
            readonly registrationCreatedOn: Instant
        }
        interface DeviceRegistration$Companion { serializer(): any }

        class DefaultDeviceRegistration extends DeviceRegistration
        {
            constructor( deviceId?: string )
        }

        class Smartphone extends DeviceDescriptor
        {
            constructor( roleName: string, defaultSamplingConfiguration: HashMap<NamespacedId, any> )
        }
    }


    namespace dk.cachet.carp.common.application.tasks
    {
        abstract class TaskDescriptor
        {
            readonly name: string
            readonly description: string | null;
        }

        class WebTask extends TaskDescriptor
        {
            constructor( name: string, measures: any, description: string, url: string )

            static get Companion(): WebTask$Companion

            readonly url: string
        }
        interface WebTask$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.common.application.triggers
    {
        abstract class Trigger
        {
            readonly requiresMasterDevice: Boolean
            readonly sourceDeviceRoleName: string
        }

        class ElapsedTimeTrigger extends Trigger
        {
            constructor( sourceDeviceRoleName: string, elapsedTime: Duration )

            readonly elapsedTime: Duration

        }

        class ManualTrigger extends Trigger
        {
            constructor( sourceDeviceRoleName: string, label: string, description?: string | null )

            readonly label: string
            readonly description: string | null;
        }

        class ScheduledTrigger extends Trigger
        {
            constructor( sourceDeviceRoleName: string, time: TimeOfDay, recurrenceRule: RecurrenceRule )

            readonly time: TimeOfDay
            readonly recurrenceRule: RecurrenceRule
        }

        class TaskControl
        {
            constructor( triggerId: Number, taskName: string, destinationDeviceRoleName: string, control: Number )

            readonly triggerId: Number
            readonly taskName: string
            readonly destinationDeviceRoleName: string
            readonly control: Number
        }
    }


    namespace dk.cachet.carp.common.application.users
    {
        import InputElement = dk.cachet.carp.common.application.data.input.elements.InputElement
        import InputDataTypeList = dk.cachet.carp.common.application.data.input.InputDataTypeList


        class Username
        {
            constructor( name: string )

            static get Companion(): Username$Companion

            readonly name: string
        }
        interface Username$Companion { serializer(): any }

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
            constructor( username: Username )

            static get Companion(): UsernameAccountIdentity$Companion

            readonly username: Username
        }
        function UsernameAccountIdentity_init_61zpoe$( username: string ): UsernameAccountIdentity
        interface UsernameAccountIdentity$Companion { serializer(): any }


        abstract class ParticipantAttribute
        {
            static get Companion(): ParticipantAttribute$Companion

            readonly inputDataType: NamespacedId

            getInputElement_6eo89k$( registeredInputDataTypes: InputDataTypeList ): InputElement
            isValidInput_etkzhw$( registeredInputDataTypes: InputDataTypeList, input: any ): boolean
            inputToData_etkzhw$( registeredInputDataTypes: InputDataTypeList, input: any ): any
            isValidData_bq34fz$( registeredInputDataTypes: InputDataTypeList, data: any ): boolean
            dataToInput_bq34fz$( registeredInputDataTypes: InputDataTypeList, data: any ): any
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


    namespace dk.cachet.carp.common.application.data.input
    {
        // No need to initialize this from TypeScript right now. Access to `CarpInputDataTypes` is sufficient.
        class InputDataTypeList { constructor() }
        const CarpInputDataTypes: InputDataTypeList
    }

    namespace dk.cachet.carp.common.application.data.input.elements
    {
        interface InputElement
        {
            readonly prompt: string

            isValid_trkh7z$( input: any ): boolean
        }
        
        class Text implements InputElement
        {
            constructor( name: string )

            static get Companion(): Text$Companion

            readonly prompt: string
            isValid_trkh7z$( input: any ): boolean
        }
        interface Text$Companion { serializer(): any }

        class SelectOne implements InputElement
        {
            constructor( name: string, options: HashSet<string> )

            static get Companion(): SelectOne$Companion

            readonly prompt: string
            readonly options: HashSet<string>
            isValid_trkh7z$( input: any ): boolean
        }
        interface SelectOne$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.common.infrastructure.serialization
    {
        function createDefaultJSON_18xi4u$(): Json
    }
}
