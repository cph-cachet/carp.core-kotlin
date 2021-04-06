package dk.cachet.carp.common.application.data.input.elements

import kotlin.reflect.KClass


/**
 * Describes data and its constraints which may be input by a user.
 */
interface InputElement<TData : Any>
{
    /**
     * Describes the data which may be input.
     */
    val name: String

    /**
     * Validates whether the provided [input] matches the constraints of this input element.
     */
    fun isValid( input: TData ): Boolean

    /**
     * Return the class information of the expected input data ([TData]).
     */
    fun getDataClass(): KClass<TData>
}


typealias AnyInputElement = InputElement<*>
