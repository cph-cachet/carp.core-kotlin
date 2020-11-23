package dk.cachet.carp.common.users

import dk.cachet.carp.common.NamespacedId
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.Data
import dk.cachet.carp.common.data.input.AnyInputElement
import dk.cachet.carp.common.data.input.CUSTOM_INPUT_TYPE_NAME
import dk.cachet.carp.common.data.input.CustomInput
import dk.cachet.carp.common.data.input.InputElement
import dk.cachet.carp.common.data.input.InputDataType
import dk.cachet.carp.common.data.input.InputDataTypeList
import kotlinx.serialization.Serializable


/**
 * Describes expected data related to one or multiple participants in a study.
 */
@Serializable
sealed class ParticipantAttribute
{
    /**
     * Uniquely identifies the type of data represented by this participant attribute.
     */
    abstract val inputType: InputDataType


    /**
     * A default participant attribute which is identified by the data [inputType] to be input.
     */
    @Serializable
    data class DefaultParticipantAttribute( override val inputType: InputDataType ) : ParticipantAttribute()


    /**
     * A custom participant attribute, for which an [input] element is specified, and for which an [inputType] is generated.
     */
    @Serializable
    data class CustomParticipantAttribute<T : Any>( val input: InputElement<T> ) : ParticipantAttribute()
    {
        override val inputType: NamespacedId = NamespacedId( CUSTOM_INPUT_TYPE_NAME, UUID.randomUUID().toString() )
    }


    /**
     * Get an [InputElement] which describes the data to be input by the user for this attribute.
     * In case this is a default attribute, the input element is retrieved through [registeredInputDataTypes].
     *
     * @throws UnsupportedOperationException when no input element is registered for this attribute.
     */
    fun getInputElement( registeredInputDataTypes: InputDataTypeList ): AnyInputElement =
        when ( this )
        {
            is DefaultParticipantAttribute -> registeredInputDataTypes.inputElements[ inputType ]
                ?: throw UnsupportedOperationException( "No input element for '$inputType' registered." )
            is CustomParticipantAttribute<*> -> input
        }

    /**
     * Convert [input] to the matching [Data] object associated to this attribute as registered in [registeredInputDataTypes],
     * or [CustomInput] in case this is a [CustomParticipantAttribute].
     *
     * @throws IllegalArgumentException when the [input] does not match the constraints determined by the [InputElement] associated to this attribute.
     * @throws UnsupportedOperationException when no input element or data converter is registered for the input type of this attribute.
     */
    fun <TInput> inputToData( registeredInputDataTypes: InputDataTypeList, input: TInput ): Data?
    {
        @Suppress( "UNCHECKED_CAST" )
        val inputElement = getInputElement( registeredInputDataTypes ) as InputElement<Any>

        // TODO: Add 'isRequired' to `InputElement` and validate whether 'null' input (not set) is a valid option.
        if ( input == null ) return null

        // Early out for custom input which is simply wrapped.
        if ( this is CustomParticipantAttribute<*> ) return CustomInput( input )

        // Verify whether input is valid.
        // TODO: `getDataClass` is a trivial implementation in extending classes, but could this be enforced by using the type system instead?
        //       On the JVM runtime, `isValidConfiguration` throws a `ClassCastException` when the wrong type were to be passed, but not on JS runtime.
        require( inputElement.getDataClass().isInstance( input ) ) { "Input data type does not match expected data type." }
        require( inputElement.isValid( input ) ) { "Input value does not match constraints for the specified type." }

        // Convert to concrete Data object.
        val converter = registeredInputDataTypes.dataConverters[ inputType ]
            ?: throw UnsupportedOperationException( "No data converter for '$inputType' registered." )
        return converter( input )
    }
}
