package dk.cachet.carp.studies

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.studies.application.ParticipantService
import dk.cachet.carp.studies.application.ParticipantServiceHost
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHost
import dk.cachet.carp.studies.domain.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.StudyOwner
import dk.cachet.carp.studies.infrastructure.InMemoryParticipantRepository
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


class StudiesCodeSamples
{
    @Test
    fun readme() = runBlockingTest {
        val (studyService, participantService) = createEndpoints()

        // Create a new study.
        val studyOwner = StudyOwner()
        var studyStatus: StudyStatus = studyService.createStudy( studyOwner, "Example study" )
        val studyId: UUID = studyStatus.studyId

        // Let the study use the protocol from the 'carp.protocols' example above.
        val trackPatientStudy: StudyProtocol = createExampleProtocol()
        val patientPhone: AnyMasterDeviceDescriptor = trackPatientStudy.masterDevices.first() // "Patient's phone"
        val protocolSnapshot: StudyProtocolSnapshot = trackPatientStudy.getSnapshot()
        studyStatus = studyService.setProtocol( studyId, protocolSnapshot )

        // Add a participant.
        val email = EmailAddress( "participant@email.com" )
        val participant: Participant = participantService.addParticipant( studyId, email )

        // Once all necessary study options have been configured, the study can go live.
        if ( studyStatus is StudyStatus.Configuring && studyStatus.canGoLive )
        {
            studyStatus = studyService.goLive( studyId )
        }

        // Once the study is live, you can 'deploy' it to participant's devices. They will be invited.
        if ( studyStatus.canDeployToParticipants )
        {
            // Create a 'participant group' with a single participant, using the "Patient's phone".
            val participation = AssignParticipantDevices( participant.id, setOf( patientPhone.roleName ) )
            val participantGroup = setOf( participation )

            val groupStatus: ParticipantGroupStatus = participantService.deployParticipantGroup( studyId, participantGroup )
            val isInvited = groupStatus.studyDeploymentStatus is StudyDeploymentStatus.Invited // True.
        }
    }


    private fun createEndpoints(): Pair<StudyService, ParticipantService>
    {
        val studyRepo = InMemoryStudyRepository()
        val studyService = StudyServiceHost( studyRepo )

        val deploymentService = DeploymentServiceHost( InMemoryDeploymentRepository(), InMemoryAccountService() )
        val participantService = ParticipantServiceHost( studyRepo, InMemoryParticipantRepository(), deploymentService )

        return Pair( studyService, participantService )
    }

    /**
     * This is the protocol created in ProtocolsCodeSamples.readme().
     */
    private fun createExampleProtocol(): StudyProtocol
    {
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Track patient movement" )

        val phone = Smartphone( "Patient's phone" )
        protocol.addMasterDevice( phone )

        val measures = listOf( Smartphone.Sensors.geolocation(), Smartphone.Sensors.stepcount() )
        val startMeasures = ConcurrentTask( "Start measures", measures )
        protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

        return protocol
    }
}
