package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceMock
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [StudyServiceRequest]'s.
 */
class StudyServiceRequestsTest
{
    companion object
    {
        val requests: List<StudyServiceRequest> = listOf(
            StudyServiceRequest.CreateStudy( StudyOwner(), "Test", "Description", StudyInvitation.empty() ),
            StudyServiceRequest.UpdateInternalDescription( UUID.randomUUID(), "New name", "New description" ),
            StudyServiceRequest.GetStudyDetails( UUID.randomUUID() ),
            StudyServiceRequest.GetStudyStatus( UUID.randomUUID() ),
            StudyServiceRequest.GetStudiesOverview(StudyOwner()),
            StudyServiceRequest.AddParticipant( UUID.randomUUID(), EmailAddress( "test@test.com" ) ),
            StudyServiceRequest.GetParticipants( UUID.randomUUID() ),
            StudyServiceRequest.SetProtocol( UUID.randomUUID(), StudyProtocol( ProtocolOwner(), "Test" ).getSnapshot() ),
            StudyServiceRequest.GoLive( UUID.randomUUID() ),
            StudyServiceRequest.DeployParticipantGroup( UUID.randomUUID(), setOf() )
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
    fun invokeOn_requests_call_service() = runBlockingTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<StudyService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }
}
