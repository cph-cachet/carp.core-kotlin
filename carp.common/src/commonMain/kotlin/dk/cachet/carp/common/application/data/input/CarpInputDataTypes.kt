package dk.cachet.carp.common.application.data.input

import dk.cachet.carp.common.application.data.input.elements.SelectOne


/**
 * All default CARP [InputDataType]s.
 */
object CarpInputDataTypes : InputDataTypeList()
{
    /**
     * The [InputDataType] namespace of all CARP input data type definitions.
     */
    const val CARP_NAMESPACE: String = "dk.cachet.carp.input"


    internal const val SEX_TYPE_NAME = "$CARP_NAMESPACE.sex"
    /**
     * Biological sex assigned at birth.
     */
    val SEX = add(
        inputType = InputDataType.fromString( SEX_TYPE_NAME ),
        inputElement = SelectOne( "Sex", Sex.values().map { it.toString() }.toSet() ),
        dataClass = Sex::class,
        inputToData = { Sex.valueOf( it ) },
        dataToInput = { it.name }
    )
}
