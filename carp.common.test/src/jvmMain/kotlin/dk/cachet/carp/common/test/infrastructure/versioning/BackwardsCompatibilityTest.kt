package dk.cachet.carp.common.test.infrastructure.versioning

import dk.cachet.carp.common.application.ApplicationServiceInfo
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.*


/**
 * Tests whether old API requests are handled correctly by migrating them to the current API version,
 * and transforming the response to be compatible with the old request.
 */
@ExperimentalSerializationApi
@Suppress( "FunctionName" )
abstract class BackwardsCompatibilityTest<TService : ApplicationService<TService, *>>(
    applicationServiceKlass: KClass<TService>
)
{
    private val serviceInfo = ApplicationServiceInfo( applicationServiceKlass.java )
    private val currentVersion = serviceInfo.apiVersion

    private val testRequestsFolder: File =
        File( "src/commonTest/resources/test-requests/${serviceInfo.serviceName}" )
    private lateinit var availableTestVersions: List<ApiVersion>

    @BeforeTest
    fun setup()
    {
        // Get available test versions.
        val directories = testRequestsFolder.listFiles()?.filter { it.isDirectory } ?: emptyList()
        availableTestVersions = directories.map {
            val versionMatch = assertNotNull(
                Regex( """(\d)\.(\d)""" ).find( it.name ),
                "Unexpected version folder in \"$testRequestsFolder\": ${it.name}"
            )
            val version = ApiVersion(
                major = versionMatch.groups[ 1 ]!!.value.toInt(),
                minor = versionMatch.groups[ 2 ]!!.value.toInt()
            )
            assertFalse(
                version.isMoreRecent( currentVersion ),
                "Impossible to have test sources for version \"$version\" " +
                "which is more recent than the current API version \"$currentVersion\"."
            )
            version
        }
    }


    abstract fun createService(): Pair<TService, EventBus>

    private val json = createTestJSON()

    /**
     * Before releasing a new version, you need to copy the output of `OutputTestRequests`
     * to test resources under a matching version folder to make this test pass.
     */
    @Test
    @Ignore
    fun test_requests_for_current_api_version_available()
    {
        val testRequests = File( testRequestsFolder, currentVersion.toString() )

        assertTrue( testRequests.exists() )
    }

    @Test
    @Ignore
    fun can_replay_backwards_compatible_test_requests() = runSuspendTest {
        val compatibleTests = availableTestVersions.filter { it.major == currentVersion.major }
        val loggedRequestsSerializer = ListSerializer( serializer<LoggedJsonRequest>() )

        val testFiles = compatibleTests.flatMap { version ->
            val testDirectory = File( testRequestsFolder, version.toString() )
            FileUtils.listFiles( testDirectory, arrayOf( "json" ), true )
        }

        testFiles
            .associateWith { json.decodeFromString( loggedRequestsSerializer, it.readText() ) }
            .forEach { replayLoggedRequests( it.key.absolutePath, it.value ) }
    }


    @Suppress( "UNCHECKED_CAST" )
    private suspend fun replayLoggedRequests( fileName: String, loggedRequests: List<LoggedJsonRequest> )
    {
        val (service, eventBus) = createService()

        loggedRequests.forEachIndexed { index, logged ->
            val replayErrorBase = "Couldn't replay requests in: $fileName. Request #${index + 1}"

            // TODO: Migrate request and preceding events to latest version.
            val migratedRequest = logged.request
            val migratedPrecedingEvents = logged.precedingEvents

            // Publish preceding events.
            migratedPrecedingEvents.forEach {
                val event = json.decodeFromJsonElement( serviceInfo.eventSerializer, it )
                val eventSource = checkNotNull( serviceInfo.getEventPublisher( event )?.kotlin )
                    { "The event \"${event::class}\" isn't an expected event processed by \"${serviceInfo.serviceKlass}\"." }

                // Cast to bypass generic constraint checking. We know the types line up.
                eventBus.publish( eventSource as KClass<Nothing>, event as IntegrationEvent<Nothing>)
            }

            // Validate whether request outcome corresponds to log.
            val request = json.decodeFromJsonElement( serviceInfo.requestObjectSerializer, migratedRequest )
                as ApplicationServiceRequest<TService, *>
            val response =
                try { request.invokeOn( service ) }
                catch ( ex: Exception )
                {
                    if ( logged !is LoggedJsonRequest.Failed ) throw ex
                    else assertEquals(
                        logged.exceptionType,
                        ex::class.simpleName,
                        "$replayErrorBase failed with the wrong exception type."
                    )
                }

            // Validate response in case request should succeed.
            if ( logged is LoggedJsonRequest.Succeeded )
            {
                // TODO: Migrate response to requested version.
                val responseSerializer = request.getResponseSerializer() as KSerializer<Any?>
                val migratedResponse = json.encodeToJsonElement( responseSerializer, response )

                assertEquals(
                    logged.response,
                    migratedResponse,
                    "$replayErrorBase returned the wrong response."
                )
            }
        }
    }
}
