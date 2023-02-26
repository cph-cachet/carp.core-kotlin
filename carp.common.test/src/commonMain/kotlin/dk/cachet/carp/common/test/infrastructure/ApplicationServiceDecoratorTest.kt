package dk.cachet.carp.common.test.infrastructure

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.services.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue


/**
 * Base class to test whether an application service decorator correctly invokes the decorated service.
 */
@ExperimentalCoroutinesApi
@Suppress(
    "FunctionName",
    "UnnecessaryAbstractClass" // Prevent test being picked up by test runner.
)
abstract class ApplicationServiceDecoratorTest<
    TService : ApplicationService<TService, TEvent>,
    TEvent : IntegrationEvent<TService>,
    TRequest : ApplicationServiceRequest<TService, *>
>(
    private val requestsTest: ApplicationServiceRequestsTest<TService, TRequest>,
    private val serviceInvoker: ApplicationServiceInvoker<TService, TRequest>
)
{
    @Test
    fun request_invoker_calls_service() = runTest {
        // Create logged service.
        val service = requestsTest.createService()
        val logger = ApplicationServiceLogger<TService, TEvent>()
        val eventBusLog = EventBusLog( SingleThreadedEventBus() ) // Ignore events.
        val ignoreServiceInvocation =
            object : Command<TRequest>
            {
                // The returned result goes unused in tests, so this cast never fails.
                @Suppress( "UNCHECKED_CAST" )
                override suspend fun <TReturn> invoke( request: TRequest ): TReturn = null as TReturn
            }
        val loggedService = requestsTest.decoratedServiceConstructor( service )
            { ApplicationServiceRequestLogger( eventBusLog, logger::addLog, ignoreServiceInvocation ) }

        // Test whether each invoked method on the decorated service is converted back into the same request object.
        // `requestTest` guarantees a request for each call is available.
        requestsTest.requests.forEach {
            serviceInvoker.invokeOnService( it, loggedService )
            assertTrue(
                logger.wasCalled( it ),
                "Service wasn't called or parameters of called request don't match: $it"
            )
            logger.clear()
        }
    }
}
