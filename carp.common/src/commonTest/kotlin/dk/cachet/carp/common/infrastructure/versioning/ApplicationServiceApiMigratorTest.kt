package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [ApplicationServiceApiMigrator].
 */
class ApplicationServiceApiMigratorTest
{
    interface TestService : ApplicationService<TestService, TestService.ServiceEvent>
    {
        companion object { val API_VERSION = ApiVersion( 1, 0 ) }

        @Serializable
        sealed class ServiceEvent( override val aggregateId: String? = null ) : IntegrationEvent<TestService>
        {
            @Required
            override val apiVersion: ApiVersion = API_VERSION
        }
    }

    @Serializable
    sealed class TestServiceRequest<out TReturn> : ApplicationServiceRequest<TestService, TReturn>
    {
        @Required
        override val apiVersion: ApiVersion = TestService.API_VERSION

        object Serializer : KSerializer<TestServiceRequest<*>> by ignoreTypeParameters( ::serializer )
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
}
