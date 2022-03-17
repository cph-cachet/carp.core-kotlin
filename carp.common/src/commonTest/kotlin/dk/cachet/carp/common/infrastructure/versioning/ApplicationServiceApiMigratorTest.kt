package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*


/**
 * Tests for [ApplicationServiceApiMigrator].
 */
class ApplicationServiceApiMigratorTest
{
    interface TestService : ApplicationService<TestService, TestService.ServiceEvent>
    {
        companion object { val API_VERSION = ApiVersion( 1, 1 ) }

        @Serializable
        sealed class ServiceEvent( override val aggregateId: String? = null ) : IntegrationEvent<TestService>
        {
            @Required
            override val apiVersion: ApiVersion = API_VERSION
        }

        fun getAnswer( question: String ): Int
    }

    class TestServiceHost : TestService
    {
        override fun getAnswer( question: String ): Int = 42
    }

    @Serializable
    sealed class TestServiceRequest<out TReturn> : ApplicationServiceRequest<TestService, TReturn>
    {
        @Required
        override val apiVersion: ApiVersion = TestService.API_VERSION

        object Serializer : KSerializer<TestServiceRequest<*>> by ignoreTypeParameters( ::serializer )

        @Serializable
        data class GetAnswer( val question: String ) : TestServiceRequest<Int>()
        {
            override fun getResponseSerializer() = Int.serializer()
            override suspend fun invokeOn( service: TestService ) = service.getAnswer( question )
        }
    }


    private fun createTestApiMigrator( runtimeVersion: ApiVersion, migrations: List<ApiMigration> ) =
        ApplicationServiceApiMigrator(
            runtimeVersion,
            TestServiceRequest.Serializer,
            TestService.ServiceEvent.serializer(),
            migrations
        )

    @Test
    fun initialization_succeeds_when_no_migrations_are_needed()
    {
        val runtimeVersion = ApiVersion( 1, 0 )
        createTestApiMigrator( runtimeVersion, migrations = emptyList() )
    }

    @Test
    fun initialization_succeeds_when_required_migrations_are_present()
    {
        val runtimeVersion = ApiVersion( 1, 2 )

        val intermediateMigrations = listOf(
            UnchangedMigration( 0, 1 ),
            UnchangedMigration( 1, 2 )
        )
        createTestApiMigrator( runtimeVersion, intermediateMigrations )

        val rangeMigration = listOf( UnchangedMigration( 0, 2 ) )
        createTestApiMigrator( runtimeVersion, rangeMigration )
    }

    @Test
    fun initialization_fails_when_migration_is_missing()
    {
        val runtimeVersion = ApiVersion( 1, 3 )

        assertFailsWith<IllegalArgumentException> { createTestApiMigrator( runtimeVersion, migrations = emptyList() ) }

        val migrations = listOf(
            UnchangedMigration( 0, 1 ),
            UnchangedMigration( 2, 3 )
        )
        assertFailsWith<IllegalArgumentException> { createTestApiMigrator( runtimeVersion, migrations ) }
    }

    @Test
    fun initalization_fails_when_migrations_conflict()
    {
        val runtimeVersion = ApiVersion( 1, 2 )
        val migrations = listOf(
            UnchangedMigration( 0, 1 ),
            UnchangedMigration( 0, 2 )
        )
        assertFailsWith<IllegalArgumentException> { createTestApiMigrator( runtimeVersion, migrations ) }
    }

    @Test
    @OptIn( ExperimentalSerializationApi::class )
    fun migration_of_request_and_response_succeeds() = runTest {
        val runtimeVersion = ApiVersion( 1, 1 )

        // Change the "prompt" key to "question" and return "42" as a String rather than Int.
        val migration =
            object : ApiMigration( 0, 1 )
            {
                override fun migrateRequest( request: JsonObject ): JsonObject = request
                    .map {
                        if ( it.key == "prompt" ) "question" to it.value
                        else it.key to it.value
                    }
                    .let { fields -> JsonObject( fields.toMap() ) }
                override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ) =
                    ApiResponse( JsonPrimitive( "42" ), null )
                override fun migrateEvent( event: JsonObject ): JsonObject = throw UnsupportedOperationException()
            }
        val apiMigrator = createTestApiMigrator( runtimeVersion, listOf( migration ) )

        // Migrate request.
        val json = createTestJSON()
        val requestType = TestServiceRequest.GetAnswer.serializer().descriptor.serialName
        val oldRequest = mapOf(
            json.configuration.classDiscriminator to JsonPrimitive( requestType ),
            API_VERSION_FIELD to JsonPrimitive( "1.0" ),
            "prompt" to JsonPrimitive( "What is the answer to life, the universe, and everything?" )
        )
        val migratedRequest = apiMigrator.migrateRequest( json, JsonObject( oldRequest ) )

        // Invoke method on migrated request.
        val response = migratedRequest.invokeOn( TestServiceHost() )
        assertEquals( "42", response.jsonPrimitive.content )
    }
}
