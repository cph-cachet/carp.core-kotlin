package dk.cachet.carp.common.infrastructure.reflect


internal actual object AccessInternals
{
    actual fun setField( onObject: Any, fieldName: String, value: Any? )
    {
        // Find the corresponding field name for the JavaScript runtime:
        // - seemingly includes 1 to 2 underscores on the JS IR runtime, likely to "hide" internals
        // - often attaches mangled suffixes, maybe related to how suffixes are added to overloaded methods
        val fields = js( "Object.keys( onObject )" ) as Array<String>
        val mangledField = fields.singleOrNull {
            it.startsWith( "_$fieldName" ) || it.startsWith( "__$fieldName" ) ||
            it.startsWith( fieldName )
        }
        checkNotNull( mangledField ) { "Could not find a matching field for \"$fieldName\" on the JavaScript runtime." }

        val toModify = onObject.asDynamic()
        toModify[ mangledField ] = value
    }
}
