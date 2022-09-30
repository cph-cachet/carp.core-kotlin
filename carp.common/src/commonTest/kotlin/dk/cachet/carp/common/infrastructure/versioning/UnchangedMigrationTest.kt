package dk.cachet.carp.common.infrastructure.versioning

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*


/**
 * Tests for [UnchangedMigration].
 */
class UnchangedMigrationTest
{
    @Test
    fun migrateRequest_of_UnchangedMigration_changes_version_number()
    {
        val migration = UnchangedMigration( 0, 1 )

        val oldObject = mapOf(
            API_VERSION_FIELD to JsonPrimitive( "1.0" ),
            "someField" to JsonPrimitive( 42 )
        )
        val migratedObject = migration.migrateRequest( JsonObject( oldObject ) )

        assertEquals( "1.1", migratedObject[ API_VERSION_FIELD ]?.jsonPrimitive?.content )
        assertEquals( 42, migratedObject[ "someField" ]?.jsonPrimitive?.int )
    }
}
