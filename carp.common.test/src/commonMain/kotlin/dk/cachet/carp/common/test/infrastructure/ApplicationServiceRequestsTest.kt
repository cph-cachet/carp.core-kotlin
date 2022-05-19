package dk.cachet.carp.common.test.infrastructure

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLoggingProxy
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.test.*


/**
 * Base class to test whether application service request objects can be serialized,
 * and whether they correctly call the application service on invoke.
 */
@ExperimentalCoroutinesApi
@Suppress( "FunctionName" )
abstract class ApplicationServiceRequestsTest<
    TService : ApplicationService<TService, *>,
    TRequest : ApplicationServiceRequest<TService, *>
>(
    private val requestSerializer: KSerializer<TRequest>,
    private val requests: List<TRequest>
)
{
    abstract fun createServiceLoggingProxy(): ApplicationServiceLoggingProxy<TService, *>


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
    fun invokeOn_requests_call_service() = runTest {
        val serviceLog = createServiceLoggingProxy()

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

    @Test
    fun serialized_request_contains_api_version()
    {
        val json = createTestJSON()

        requests.forEach {
            val serialized = json.encodeToJsonElement( requestSerializer, it ) as JsonObject
            val versionKey = ApplicationServiceRequest<*, *>::apiVersion.name
            assertTrue( serialized[ versionKey ] is JsonElement )
        }
    }
}
