package dk.cachet.carp.common.test.infrastructure

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.reflect.reflectIfAvailable
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.test.JsIgnore
import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.test.*


/**
 * Base class to test whether a request object exists per method defined in an application service,
 * whether it can be serialized, and whether the service invoker works.
 */
@Suppress( "FunctionName" )
abstract class ApplicationServiceRequestsTest<TService : ApplicationService<TService, *>, TRequest>(
    private val serviceKlass: KClass<TService>,
    private val requestSerializer: KSerializer<TRequest>,
    private val requests: List<TRequest>
)
{
    abstract fun createServiceLog( log: (LoggedRequest<TService>) -> Unit ): ApplicationServiceLog<TService>


    @Suppress( "UNCHECKED_CAST" )
    @Test
    @JsIgnore // Reflection is not available on JS runtime.
    fun request_object_for_each_request_available()
    {
        val reflect = reflectIfAvailable()
        assertNotNull( reflect )

        val serviceFunctions = reflect.members( serviceKlass )
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = requests.map {
            val serviceInvoker = it as ServiceInvoker<TService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun invokeOk_requests_call_service() = runSuspendTest {
        val loggedRequests: MutableList<LoggedRequest<TService>> = mutableListOf()
        val serviceLog = createServiceLog { log -> loggedRequests.add( log ) }

        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<TService, *>
            try { serviceInvoker.invokeOn( serviceLog as TService ) }
            catch ( ignore: Exception ) { } // Requests do not have to succeed to verify request arrived.
            assertEquals( request, loggedRequests.single().request )

            loggedRequests.clear()
        }
    }

    @Test
    fun can_serialize_and_deserialize_requests()
    {
        val json = createTestJSON()

        requests.forEach { request ->
            val serialized = json.encodeToString( requestSerializer, request )
            val parsed = json.decodeFromString( requestSerializer, serialized )
            assertEquals( request, parsed )
        }
    }
}
