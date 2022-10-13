package dk.cachet.carp.common.infrastructure.versioning

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive


/**
 * A new `version` field was added to all aggregate root snapshots.
 * The only snapshot which was exposed is `StudyProtocolSnapshot`.
 */
abstract class Major1Minor0To1Migration : ApiMigration( 0, 1 )
{
    private val newField = "version"

    protected fun ApiJsonObjectMigrationBuilder.addVersionField( fieldName: String ) =
        updateObject( fieldName ) { json[ newField ] = JsonPrimitive( 0 ) }

    protected fun ApiJsonObjectMigrationBuilder.removeVersionField()
    {
        json.remove( newField )
    }

    protected fun ApiJsonObjectMigrationBuilder.removeVersionField( fieldName: String )
    {
        if ( json[ fieldName ] != JsonNull )
        {
            updateObject( fieldName ) { json.remove( newField ) }
        }
    }
}
