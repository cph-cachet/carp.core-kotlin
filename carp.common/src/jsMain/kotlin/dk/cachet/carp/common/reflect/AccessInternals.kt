package dk.cachet.carp.common.reflect


internal actual object AccessInternals
{
    actual fun setField( onObject: Any, fieldName: String, value: Any )
    {
        // Find the corresponding field name for the JavaScript runtime, which often attaches mangled suffixes.
        val fields = js( "Object.keys( onObject )" ) as Array<String>
        val mangledField = fields.singleOrNull { it.startsWith( fieldName ) }
        checkNotNull( mangledField ) { "Could not find a matching field for \"$fieldName\" on the JavaScript runtime." }

        val toModify = onObject.asDynamic()
        toModify[ mangledField ] = value
    }
}
