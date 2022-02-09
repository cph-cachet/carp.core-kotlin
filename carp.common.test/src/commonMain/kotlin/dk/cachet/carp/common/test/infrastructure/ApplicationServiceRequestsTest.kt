package dk.cachet.carp.common.test.infrastructure

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.elementDescriptors
import kotlin.test.*


/**
 * Base class to test whether application service request objects can be serialized,
 * and whether they correctly call the application service on invoke.
 */
@Suppress( "FunctionName" )
abstract class ApplicationServiceRequestsTest<
    TService : ApplicationService<TService, *>,
    TRequest : ApplicationServiceRequest<TService, *>
>(
    private val requestSerializer: KSerializer<TRequest>,
    private val requests: List<TRequest>
)
{
    abstract fun createServiceLog(): ApplicationServiceLog<TService, *>


    @ExperimentalSerializationApi
    @Test
    fun all_request_objects_tested()
    {
        val testedRequestObjects = requests.mapNotNull { it::class.simpleName }.toSet()

        val sealedClassSerializer = requestSerializer.descriptor
        val subclassSerializers = sealedClassSerializer.getElementDescriptor( 1 ).elementDescriptors.toList()
        val allRequestObjects = subclassSerializers.map { it.serialName.split( '.' ).last() }.toSet()

        assertEquals( allRequestObjects, testedRequestObjects )
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun invokeOn_requests_call_service() = runSuspendTest {
        val serviceLog = createServiceLog()

        requests.forEach { request ->
            try { request.invokeOn( serviceLog as TService ) }
            catch ( ignore: Exception ) { } // Requests do not have to succeed to verify request arrived.
            assertTrue( serviceLog.wasCalled( request ) )

            serviceLog.clear()
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
