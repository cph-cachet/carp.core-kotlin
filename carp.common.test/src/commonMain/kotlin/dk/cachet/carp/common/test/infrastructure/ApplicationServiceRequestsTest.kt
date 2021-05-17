package dk.cachet.carp.common.test.infrastructure

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.reflect.reflectIfAvailable
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.test.JsIgnore
import dk.cachet.carp.test.Mock
import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.test.*


/**
 * Base class to test whether a request object exists per method defined in an application service,
 * whether it can be serialized, and whether the service invoker works.
 */
@Suppress( "FunctionName", "UnnecessaryAbstractClass" )
abstract class ApplicationServiceRequestsTest<TService : ApplicationService<*, *>, TRequest>(
    private val serviceKlass: KClass<TService>,
    protected val serviceMock: Mock<TService>,
    private val requestSerializer: KSerializer<TRequest>,
    private val requests: List<TRequest>
)
{
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
    fun invokeOn_requests_call_service() = runSuspendTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<TService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( serviceMock as TService )
            assertTrue( serviceMock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            serviceMock.reset()
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
