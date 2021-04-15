package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.studies.application.ParticipantService
import dk.cachet.carp.studies.application.ParticipantServiceMock
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


/**
 * Tests for [ParticipantServiceRequest]'s.
 */
class ParticipantServiceRequestsTest
{
    companion object
    {
        private val studyId = UUID.randomUUID()

        val requests: List<ParticipantServiceRequest> = listOf(
            ParticipantServiceRequest.AddParticipant( studyId, EmailAddress( "test@test.com" ) ),
            ParticipantServiceRequest.GetParticipant( studyId, UUID.randomUUID() ),
            ParticipantServiceRequest.GetParticipants( studyId ),
            ParticipantServiceRequest.DeployParticipantGroup( studyId, setOf() ),
            ParticipantServiceRequest.GetParticipantGroupStatusList( studyId ),
            ParticipantServiceRequest.StopParticipantGroup( studyId, UUID.randomUUID() ),
            ParticipantServiceRequest.SetParticipantGroupData( studyId, UUID.randomUUID(), InputDataType( "some", "type" ), CustomInput( "Test" ) )
        )
    }

    private val mock = ParticipantServiceMock()


    @Test
    fun can_serialize_and_deserialize_requests()
    {
        requests.forEach { request ->
            val serializer = ParticipantServiceRequest.serializer()
            val serialized = JSON.encodeToString( serializer, request )
            val parsed = JSON.decodeFromString( serializer, serialized )
            assertEquals( request, parsed )
        }
    }

    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun invokeOn_requests_call_service() = runSuspendTest {
        requests.forEach { request ->
            val serviceInvoker = request as ServiceInvoker<ParticipantService, *>
            val function = serviceInvoker.function
            serviceInvoker.invokeOn( mock )
            assertTrue( mock.wasCalled( function, serviceInvoker.overloadIdentifier ) )
            mock.reset()
        }
    }
}
