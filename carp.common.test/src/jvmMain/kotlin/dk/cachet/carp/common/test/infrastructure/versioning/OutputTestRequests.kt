package dk.cachet.carp.common.test.infrastructure.versioning

import dk.cachet.carp.common.application.ApplicationServiceInfo
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.services.*
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInfo
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.*


val TEST_REQUESTS_FOLDER = File( "build/test-requests/" )


/**
 * Outputs all logged requests of a [serviceLogger] while running unit tests to the build folder.
 *
 * Extend from this base class along with a test interface for which to log requests,
 * and implement `createService` by returning a wrapped service using [createLoggedApplicationService].
 */
open class OutputTestRequests<
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>,
    TRequest : ApplicationServiceRequest<TService, *>,
>(
    applicationServiceKlass: KClass<TService>,
    private val decoratedServiceConstructor: (TService, (Command<TRequest>) -> Command<TRequest>) -> TService
)
{
    private var serviceLogger: ApplicationServiceLogger<TService, TEvent>? = null
    private val applicationServiceInfo = ApplicationServiceInfo.of( applicationServiceKlass.java )

    companion object
    {
        private lateinit var testFolder: File

        @JvmStatic
        @BeforeAll
        fun setup( info: TestInfo )
        {
            val testInterface = checkNotNull( info.testClass.get().interfaces.singleOrNull() )
                { "Outputting test results expects a single test interface to be applied to the extending class." }
            testFolder = File( TEST_REQUESTS_FOLDER, testInterface.simpleName )

            // Clean previous output.
            FileUtils.deleteDirectory( testFolder )
            testFolder.mkdirs()
        }
    }

    protected fun createLoggedApplicationService(
        service: TService,
        eventBus: EventBus
    ): TService
    {
        // Create event bus log which subscribes to all events of this service and its dependent services.
        val subscribeToServices = applicationServiceInfo.dependentServices + applicationServiceInfo.serviceKlass
        @Suppress( "UNCHECKED_CAST" )
        val toObserve = subscribeToServices.map {
            val serviceKlass = it.kotlin as KClass<Nothing>
            val eventKlass = ApplicationServiceInfo.of( it ).eventClass.kotlin as KClass<Nothing>
            EventBusLog.Subscription( serviceKlass, eventKlass )
        }.toTypedArray()
        val eventBusLog = EventBusLog( eventBus, *toObserve )

        // Decorate service by applying a logger from which requests are retrieved after each test.
        val logger = ApplicationServiceLogger<TService, TEvent>()
        val loggedService = decoratedServiceConstructor( service )
            { ApplicationServiceRequestLogger( eventBusLog, logger::addLog, it ) }
        serviceLogger = logger

        return loggedService
    }

    @AfterTest
    fun outputLoggedRequests( info: TestInfo )
    {
        val service = checkNotNull( serviceLogger )
            { "`createLoggedApplicationService` needs to be called in `createService`." }

        // Serialize requests as json.
        val requests = service.loggedRequests
        val json = Json( createTestJSON() ) { prettyPrint = true }
        val serializer = ListSerializer( applicationServiceInfo.loggedRequestSerializer )
        val loggedRequestsJson = json.encodeToString( serializer, requests )

        // Output to file.
        val testName = info.testMethod.get().name
        val output = File( testFolder, "$testName.json" )
        output.writeText( loggedRequestsJson )
    }
}
