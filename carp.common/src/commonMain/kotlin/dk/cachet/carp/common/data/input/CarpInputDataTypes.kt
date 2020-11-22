package dk.cachet.carp.common.data.input

import dk.cachet.carp.common.data.input.element.SelectOne


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
        type = InputDataType.fromString( SEX_TYPE_NAME ),
        inputElement = SelectOne( "Sex", Sex.values().map { it.toString() }.toSet() ),
        dataConverter = { Sex.valueOf( it ) }
    )
}
