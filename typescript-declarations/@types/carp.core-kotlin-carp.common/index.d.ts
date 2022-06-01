import * as carpCommon from 'carp.core-kotlin-carp.common'
import * as kotlinStdLib from 'kotlin-kotlin-stdlib-js-ir'
import * as kotlinDateTime from 'Kotlin-DateTime-library-kotlinx-datetime-js-ir'
import * as kotlinSerialization from 'kotlinx-serialization-kotlinx-serialization-json-js-ir'


declare module 'carp.core-kotlin-carp.common'
{
    // Kotlin
    export namespace kotlin
    {
        type Long = any
    }
    export namespace kotlin.collections
    {
        type Collection<T> = kotlinStdLib.$crossModule$.Collection<T>
        type List<T> = kotlinStdLib.$crossModule$.List<T>
        type Map<K, V> = any
        namespace Map
        {
            type Entry<K, V> = any
        }
        type MutableList<T> = any
        type Set<T> = kotlinStdLib.$crossModule$.Set<T>
    }
    export namespace kotlin.ranges
    {
        type IntRange = any
    }
    export namespace kotlin.reflect
    {
        type KClass<T> = any
    }
    export namespace kotlin.text
    {
        type Regex = any
    }
    export namespace kotlin.time
    {
        type Duration = any
    }

    // Kotlinx datetime
    export namespace kotlinx.datetime
    {
        type Instant = kotlinDateTime.$crossModule$.Instant
    }
    
    // Kotlinx serialization
    export namespace kotlinx.serialization
    {
        type KSerializer<T> = any
    }
    export namespace kotlinx.serialization.json
    {
        type Json = kotlinSerialization.$crossModule$.Json
    }
    export namespace kotlinx.serialization.modules
    {
        type SerializersModule = any
    }
    export namespace kotlinx.serialization.internal
    {
        type GeneratedSerializer<T> = any
        type SerializationConstructorMarker = any
        type SerializerFactory = any
    }
    export namespace kotlinx.serialization.descriptors
    {
        type SerialDescriptor = any
    }

    // CARP common
    namespace dk.cachet.carp.common.application
    {
        type RecurrenceRule = any
        type UUIDFactory = any
    }
    namespace dk.cachet.carp.common.application.data
    {
        type Data = any
        type DataTypeMetaDataMap = any
    }
    namespace dk.cachet.carp.common.application.devices
    {
        type AltBeaconDeviceRegistrationBuilder = any
        type DefaultDeviceRegistrationBuilder = any
        type MACAddressDeviceRegistrationBuilder = any
        type SmartphoneBuilder = any
    }
    namespace dk.cachet.carp.common.application.data.input
    {
        export interface InputDataTypeList
        {
            toArray(): Array<NamespacedId>
            _get_size__0_k$(): number
        }
    }
    namespace dk.cachet.carp.common.application.data.input.elements
    {
        type InputElement<T> = $crossModule$.InputElement
    }
    namespace dk.cachet.carp.common.application.sampling
    {
        type BatteryAwareSamplingConfigurationBuilder<TConfig, TBuilder> = any
        type DataTypeSamplingSchemeMap = any
        type GranularitySamplingConfigurationBuilder = any
        type IntervalSamplingConfigurationBuilder = any
        type SamplingConfiguration = any
        type SamplingConfigurationBuilder<TConfig> = any
    }
    namespace dk.cachet.carp.common.application.tasks
    {
        type BackgroundTaskBuilder = any
        type Measure = any
        export namespace Measure
        {
            type DataStream = any
        }
        type TaskConfiguration<T> = any
        export interface TaskConfigurationList
        {
            toArray(): Array<dk.cachet.carp.common.application.tasks.SupportedTaskConfiguration<any, any>>
            _get_size__0_k$(): number
        }
        type WebTask = any
        type WebTaskBuilder = any
    }
    namespace dk.cachet.carp.common.application.users
    {
        type AccountIdentity = any
        type AssignedTo = $crossModule$.AssignedTo
        type ParticipantAttribute = $crossModule$.ParticipantAttribute
    }

    namespace $crossModule$
    {
        import Set = kotlin.collections.Set
        import NamespacedId = dk.cachet.carp.common.application.NamespacedId
        import InputDataTypeList = dk.cachet.carp.common.application.data.input.InputDataTypeList

        interface InputElement
        {
            readonly prompt: string
        }

        class Companion_82 // ExpectedParticipantData.Companion
        {
            serializer(): any
        }
        interface AssignedTo { }
        class All implements AssignedTo { }
        class Roles implements AssignedTo
        {
            constructor( roleNames: Set<string> )
        }

        interface ParticipantAttribute
        {
            _get_inputDataType__0_k$(): NamespacedId
            getInputElement_kussp3_k$( registeredInputDataTypes: InputDataTypeList ): InputElement
            isValidInput_xr0dfw_k$( registeredInputDataTypes: InputDataTypeList, input: any ): boolean
            inputToData_xr0dfw_k$( registeredInputDataTypes: InputDataTypeList, input: any ): any
        }
        class DefaultParticipantAttribute implements ParticipantAttribute
        {
            constructor( inputDataType: NamespacedId )

            _get_inputDataType__0_k$(): NamespacedId
            getInputElement_kussp3_k$( registeredInputDataTypes: InputDataTypeList ): InputElement
            isValidInput_xr0dfw_k$( registeredInputDataTypes: InputDataTypeList, input: any ): boolean
            inputToData_xr0dfw_k$( registeredInputDataTypes: InputDataTypeList, input: any ): any
        }
        class CustomParticipantAttribute implements ParticipantAttribute
        {
            constructor( input: InputElement )

            _get_inputDataType__0_k$(): NamespacedId
            getInputElement_kussp3_k$( registeredInputDataTypes: InputDataTypeList ): InputElement
            isValidInput_xr0dfw_k$( registeredInputDataTypes: InputDataTypeList, input: any ): boolean
            inputToData_xr0dfw_k$( registeredInputDataTypes: InputDataTypeList, input: any ): any
        }

        function Trilean_UNKNOWN_getInstance(): any
        function toTrilean( bool: boolean ): any
    }
}
