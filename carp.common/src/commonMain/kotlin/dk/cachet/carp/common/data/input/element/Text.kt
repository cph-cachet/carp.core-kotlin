package dk.cachet.carp.common.data.input.element

import dk.cachet.carp.common.data.input.InputElement
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


/**
 * Text entry by the user.
 */
@Serializable
data class Text( override val name: String ) : InputElement<String>
{
    override fun isValid( input: String ): Boolean = true

    override fun getDataClass(): KClass<String> = String::class
}
