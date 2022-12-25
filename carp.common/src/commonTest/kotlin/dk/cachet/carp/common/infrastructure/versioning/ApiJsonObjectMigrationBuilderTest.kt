package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import kotlin.test.*


/**
 * Tests for [ApiJsonObjectMigrationBuilder].
 */
class ApiJsonObjectMigrationBuilderTest
{
    interface ToMigrate

    @Serializable
    @SerialName( "Migrate" )
    class Migrate( val a: Int, val b: Int, val inner: MigrateInner ) : ToMigrate

    @Serializable
    class MigrateInner( val c: Int )

    private val toMigrateSerializer = PolymorphicSerializer( ToMigrate::class )

    private val json = createDefaultJSON(
        SerializersModule {
            polymorphic( ToMigrate::class )
            {
                subclass( Migrate::class )
            }
        }
    )


    @Test
    fun ifType_succeeds()
    {
        val toMigrate: ToMigrate = Migrate( 42, 42, MigrateInner( 42 ) )
        val toMigrateJson: JsonObject = json.encodeToJsonElement( toMigrateSerializer, toMigrate ).jsonObject

        val toSet = JsonPrimitive( 0 )
        val migrated = migrate( toMigrateJson ) {
            ifType( "Migrate" ) {
                json[ Migrate::a.name ] = toSet
            }
            ifType( "NoMatch" ) {
                json[ Migrate::b.name ] = toSet
            }
        }

        assertEquals( toSet, migrated[ Migrate::a.name ] )
        assertNotEquals( toSet, migrated[ Migrate::b.name ] )
    }

    @Test
    fun updateObject_succeeds()
    {
        val toMigrate = Migrate( 42, 42, MigrateInner( 42 ) )
        val toMigrateJson = json.encodeToJsonElement( toMigrateSerializer, toMigrate ).jsonObject

        val toSet = JsonPrimitive( 0 )
        val migrated = migrate( toMigrateJson ) {
            updateObject( Migrate::inner.name ) {
                json[ MigrateInner::c.name ] = toSet
            }
        }

        val inner = assertNotNull( migrated[ Migrate::inner.name ]?.jsonObject )
        assertEquals( toSet, inner[ MigrateInner::c.name ] )
    }

    @Test
    fun updateArray_succeeds()
    {
        val jsonArray = JsonArray( listOf( JsonPrimitive( 42 ) ) )
        val arrayField = "array"
        val jsonObject = JsonObject( mapOf( arrayField to jsonArray ) )

        val migrated = migrate( jsonObject ) {
            updateArray( arrayField ) { json.clear() }
        }

        val emptyArrayField = JsonObject( mapOf( arrayField to JsonArray( emptyList() ) ) )
        assertEquals( emptyArrayField, migrated )
    }

    @Test
    fun copyField_succeeds()
    {
        val toMigrate = Migrate( 42, 0, MigrateInner( 0 ) )
        val toMigrateJson = json.encodeToJsonElement( toMigrateSerializer, toMigrate ).jsonObject

        val migrated = migrate( toMigrateJson ) {
            copyField( "a", "b" )
        }

        assertEquals( migrated[ Migrate::a.name ], migrated[ Migrate::b.name ] )
    }

    private fun migrate( json: JsonObject, migration: ApiJsonObjectMigrationBuilder.() -> Unit ): JsonObject =
        ApiJsonObjectMigrationBuilder( json, 0, 1 ).apply( migration ).build()
}
