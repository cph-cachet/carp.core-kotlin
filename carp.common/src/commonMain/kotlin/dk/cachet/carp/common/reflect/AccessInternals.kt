package dk.cachet.carp.common.reflect


/**
 * Provide access to runtime internals which cannot be accessed at compile time.
 */
internal expect object AccessInternals
{
    /**
     * Set the value of the field with the given [fieldName] on a given object ([onObject]) to [value].
     */
    @Suppress( "UnusedPrivateMember" ) // TODO: Remove once detekt bug is fixed: https://github.com/detekt/detekt/issues/3415
    fun setField( onObject: Any, fieldName: String, value: Any )
}
