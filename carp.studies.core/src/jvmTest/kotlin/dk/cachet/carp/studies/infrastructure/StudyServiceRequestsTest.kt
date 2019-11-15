package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.studies.application.*
import dk.cachet.carp.studies.domain.*
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [StudyServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class StudyServiceRequestsTest
{
    companion object {
        val requests: List<StudyServiceRequest> = listOf(
            StudyServiceRequest.CreateStudy( StudyOwner(), "Test", StudyDescription.empty() ),
            StudyServiceRequest.GetStudyStatus( UUID.randomUUID() )
        )
    }

    private val mock = StudyServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = StudyServiceRequest.serializer()
            val serialized = JSON.stringify( serializer, request )
            val parsed = JSON.parse( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun executeOn_requests_call_service() = runBlockingTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<StudyService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun request_object_for_each_request_available()
    {
        val serviceFunctions = StudyService::class.members
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = requests.map {
            val serviceInvoker = it as ServiceInvoker<StudyService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }
}