package dk.cachet.carp.common.users

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
        override val inputType: InputDataType = InputDataType( CUSTOM_INPUT_TYPE_NAME, UUID.randomUUID().toString() )
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
     * Determines whether [input] is valid and can be converted to a matching [Data] object associated to this attribute
     * as registered in [registeredInputDataTypes] or [CustomInput] in case this is a [CustomParticipantAttribute].
     *
     * @throws UnsupportedOperationException when no input element is registered for this attribute.
     */
    fun <TInput> isValidInput( registeredInputDataTypes: InputDataTypeList, input: TInput ): Boolean
    {
        @Suppress( "UNCHECKED_CAST" )
        val inputElement = getInputElement( registeredInputDataTypes ) as InputElement<Any>

        // TODO: For now, consider null always a valid option.
        if ( input == null ) return true

        // TODO: `getDataClass` is a trivial implementation in extending classes, but could this be enforced by using the type system instead?
        //       On the JVM runtime, `isValidConfiguration` throws a `ClassCastException` when the wrong type were to be passed, but not on JS runtime.
        val isExpectedDataType = inputElement.getDataClass().isInstance( input )
        return isExpectedDataType && inputElement.isValid( input )
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
        require( isValidInput( registeredInputDataTypes, input ) )
            { "Input value does not match constraints for the specified input type." }

        // TODO: Add 'isRequired' to `InputElement` and validate whether 'null' input (not set) is a valid option.
        if ( input == null ) return null

        // Custom input which is simply wrapped.
        if ( this is CustomParticipantAttribute<*> ) return CustomInput( input )

        // Convert to concrete Data object.
        val converter = registeredInputDataTypes.inputToDataConverters[ inputType ]
            ?: throw UnsupportedOperationException( "No data converter for '$inputType' registered." )
        return converter( input )
    }

    /**
     * Determines whether [data] is valid input for this attribute.
     *
     * @throws UnsupportedOperationException when no input element is registered for this attribute.
     */
    fun <TData : Data?> isValidData( registeredInputDataTypes: InputDataTypeList, data: TData ): Boolean
    {
        val inputElement = getInputElement( registeredInputDataTypes )

        // TODO: For now, consider null always a valid option.
        if ( data == null ) return true

        // Early out in case data is of the wrong type.
        val isCorrectDataType = when ( this )
        {
            is CustomParticipantAttribute<*> -> data is CustomInput<*> && isValidCustomData( inputElement, data )
            is DefaultParticipantAttribute -> registeredInputDataTypes.dataClasses[ inputType ]!!.isInstance( data )
        }
        if ( !isCorrectDataType ) return false

        val input: Any? = dataToInput( registeredInputDataTypes, data )
        return isValidInput( registeredInputDataTypes, input )
    }

    /**
     * Convert [data] to the corresponding input representation.
     * The returned input is not necessarily valid as it may be constrained further by the registered input element.
     *
     * @throws UnsupportedOperationException when no input element is registered for this attribute.
     */
    fun <TData : Data?> dataToInput( registeredInputDataTypes: InputDataTypeList, data: TData ): Any?
    {
        @Suppress( "UNCHECKED_CAST" )
        val inputElement = getInputElement( registeredInputDataTypes ) as InputElement<Any>

        // TODO: For now, consider null always a valid option.
        if ( data == null ) return null

        // Custom input should be wrapped by `CustomInput` and contain an object of the expected input type.
        if ( this is CustomParticipantAttribute<*> )
        {
            require( data is CustomInput<*> && isValidCustomData( inputElement, data ) )
                { "Data is not of expected type for this attribute." }
            return data.input
        }

        // Convert to input data.
        val converter = registeredInputDataTypes.dataToInputConverters[ inputType ]
            ?: throw UnsupportedOperationException( "No data converter for '$inputType' registered." )
        require( registeredInputDataTypes.dataClasses[ inputType ]!!.isInstance( data ) )
            { "Data is not of expected type for this attribute." }
        return converter( data )
    }

    private fun isValidCustomData( inputElement: InputElement<*>, data: CustomInput<*> ) =
        inputElement.getDataClass().isInstance( data.input )
}
