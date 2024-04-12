package dk.cachet.carp.common.infrastructure.reflect


/**
 * Provide access to runtime internals which cannot be accessed at compile time.
 *
 * Warning: on JavaScript targets, this only works for non-minified sources. Variables which aren't exported are mangled
 * completely. So this should not be used in production code! It may still be useful for test automation.
 */
internal expect object AccessInternals
{
    /**
     * Set the value of the field with the given [fieldName] on a given object ([onObject]) to [value].
     */
    fun setField( onObject: Any, fieldName: String, value: Any? )
}
