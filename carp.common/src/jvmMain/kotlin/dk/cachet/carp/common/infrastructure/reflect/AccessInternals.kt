package dk.cachet.carp.common.infrastructure.reflect


internal actual object AccessInternals
{
    actual fun setField( onObject: Any, fieldName: String, value: Any )
    {
        // Get field.
        val klass = onObject::class.java
        val field = klass.getDeclaredField( fieldName )
        field.isAccessible = true

        field.set( onObject, value )
    }
}
