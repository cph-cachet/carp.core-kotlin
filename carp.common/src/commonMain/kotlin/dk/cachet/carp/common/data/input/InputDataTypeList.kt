package dk.cachet.carp.common.data.input

import dk.cachet.carp.common.data.Data


/**
 * A helper class to access and list [InputDataType]s, and register matching [InputElement]s and converters to convert input to [Data] objects.
 *
 * Extend from this class as an object and assign members as follows: `val MEMBER = add( type, dataConverter, inputElement )`.
 */
open class InputDataTypeList private constructor( val list: MutableList<InputDataType> ) : List<InputDataType> by list
{
    constructor() : this( mutableListOf() )

    private val _inputElements: MutableMap<InputDataType, AnyInputElement> = mutableMapOf()
    val inputElements: Map<InputDataType, AnyInputElement> = _inputElements

    private val _dataConverters: MutableMap<InputDataType, (Any) -> Data> = mutableMapOf()
    val dataConverters: Map<InputDataType, (Any) -> Data> = _dataConverters

    /**
     * Register an input [type], an associated [inputElement], and data converter to convert the data type of the [inputElement] to a [Data] object.
     */
    protected fun <TInput : Any, TData : Data> add(
        type: InputDataType,
        inputElement: InputElement<TInput>,
        dataConverter: (TInput) -> TData
    ): InputDataType =
        type.also{
            require( !_inputElements.containsKey( it ) ) { "The specified input data type is already registered in this list." }

            list.add( it )
            _inputElements[ it ] = inputElement
            @Suppress( "UNCHECKED_CAST" )
            _dataConverters[ it ] = dataConverter as (Any) -> TData
        }
}
