package dk.cachet.carp.common.infrastructure.reflect


/**
 * Provide access to runtime internals which cannot be accessed at compile time.
 */
internal expect object AccessInternals
{
    /**
     * Set the value of the field with the given [fieldName] on a given object ([onObject]) to [value].
     */
    fun setField( onObject: Any, fieldName: String, value: Any? )
}
