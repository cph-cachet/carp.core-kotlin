package dk.cachet.carp.common.application.data.input

import dk.cachet.carp.common.application.data.Data
import kotlin.reflect.KClass


/**
 * A helper class to access and list [InputDataType]s,
 * and register matching [InputElement]s and converters to convert input from and to [Data] objects.
 *
 * Extend from this class as an object and assign members as follows: `val MEMBER = add( type, dataConverter, inputElement )`.
 */
open class InputDataTypeList private constructor( val list: MutableList<InputDataType> ) : List<InputDataType> by list
{
    constructor() : this( mutableListOf() )

    private val _inputElements: MutableMap<InputDataType, AnyInputElement> = mutableMapOf()
    val inputElements: Map<InputDataType, AnyInputElement> = _inputElements

    private val _dataClasses: MutableMap<InputDataType, KClass<*>> = mutableMapOf()
    val dataClasses: Map<InputDataType, KClass<*>> = _dataClasses

    private val _inputToDataConverters: MutableMap<InputDataType, (Any) -> Data> = mutableMapOf()
    val inputToDataConverters: Map<InputDataType, (Any) -> Data> = _inputToDataConverters

    private val _dataToInputConverters: MutableMap<InputDataType, (Data) -> Any> = mutableMapOf()
    val dataToInputConverters: Map<InputDataType, (Data) -> Any> = _dataToInputConverters

    /**
     * Register an input [type], an associated [inputElement], and data converter to convert the data type of the [inputElement] to a [Data] object.
     */
    @Suppress( "UNCHECKED_CAST" )
    protected fun <TInput : Any, TData : Data> add(
        inputType: InputDataType,
        inputElement: InputElement<TInput>,
        dataClass: KClass<TData>,
        inputToData: (TInput) -> TData,
        dataToInput: (TData) -> TInput
    ): InputDataType =
        inputType.also{
            require( !_inputElements.containsKey( it ) ) { "The specified input data type is already registered in this list." }

            list.add( it )
            _inputElements[ it ] = inputElement
            _dataClasses[ it ] = dataClass
            _inputToDataConverters[ it ] = inputToData as (Any) -> TData
            _dataToInputConverters[ it ] = dataToInput as (Data) -> Any
        }
}
