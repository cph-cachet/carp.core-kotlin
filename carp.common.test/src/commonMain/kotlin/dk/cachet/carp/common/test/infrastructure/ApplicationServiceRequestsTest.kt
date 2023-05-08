package dk.cachet.carp.common.test.infrastructure

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.*
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*
import kotlin.test.*


/**
 * Base class to test whether application service request objects can be serialized.
 */
@ExperimentalCoroutinesApi
@Suppress( "FunctionName" )
abstract class ApplicationServiceRequestsTest<
    TService : ApplicationService<TService, *>,
    TRequest : ApplicationServiceRequest<TService, *>
>(
    val decoratedServiceConstructor: (TService, (Command<TRequest>) -> Command<TRequest>) -> TService,
    private val requestSerializer: KSerializer<TRequest>,
    val requests: List<TRequest>
)
{
    abstract fun createService(): TService


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
