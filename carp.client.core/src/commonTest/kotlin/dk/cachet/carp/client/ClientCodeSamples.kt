package dk.cachet.carp.client

import dk.cachet.carp.client.domain.SmartphoneClient
import dk.cachet.carp.client.domain.StudyRuntimeStatus
import dk.cachet.carp.client.domain.createDataCollectorFactory
import dk.cachet.carp.client.domain.data.DataListener
import dk.cachet.carp.client.infrastructure.InMemoryClientRepository
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.CarpDataTypes
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.domain.users.ActiveParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


class ClientCodeSamples
{
    @Test
    fun readme() = runBlockingTest {
        val deploymentService = createDeploymentEndpoint()
        val dataCollectorFactory = createDataCollectorFactory()

        // Retrieve invitation to participate in the study using a specific device.
        val account: Account = getLoggedInUser()
        val invitation: ActiveParticipationInvitation =
            deploymentService.getActiveParticipationInvitations( account.id ).first()
        val studyDeploymentId: UUID = invitation.participation.studyDeploymentId
        val deviceToUse: String = invitation.devices.first().deviceRoleName // This matches "Patient's phone".

        // Create a study runtime for the study.
        val clientRepository = createRepository()
        val client = SmartphoneClient( clientRepository, deploymentService, dataCollectorFactory )
        client.configure {
            // Device-specific registration options can be accessed from here.
            // Depending on the device type, different options are available.
            // E.g., for a smartphone, a UUID deviceId is generated. To override this default:
            deviceId = "xxxxxxxxx"
        }
        val runtime: StudyRuntimeStatus = client.addStudy( studyDeploymentId, deviceToUse )
        var isDeployed = runtime.isDeployed // True, because there are no dependent devices.

        // Suppose a deployment also depends on a "Clinician's phone" to be registered; deployment cannot complete yet.
        // After the clinician's phone has been registered, attempt deployment again.
        isDeployed = client.tryDeployment( runtime.id ) // True once dependent clients have been registered.
    }


    private suspend fun createDeploymentEndpoint(): DeploymentService
    {
        val service = DeploymentServiceHost( InMemoryDeploymentRepository(), accountService )

        // Create deployment for the example protocol.
        val protocol = createExampleProtocol()
        val status = service.createStudyDeployment( protocol.getSnapshot() )

        // Invite a participant.
        val phone = protocol.masterDevices.first()
        val invitation = StudyInvitation.empty()
        service.addParticipation( status.studyDeploymentId, setOf( phone.roleName ), accountIdentity, invitation )
        val account = accountService.findAccount( accountIdentity )

        return service
    }

    private fun createRepository() = InMemoryClientRepository()

    /**
     * This is the protocol created in ProtocolsCodeSamples.readme().
     */
    private fun createExampleProtocol(): StudyProtocol
    {
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Example study" )

        val phone = Smartphone( "Patient's phone" )
        protocol.addMasterDevice( phone )

        val measures = listOf( Smartphone.Sensors.geolocation(), Smartphone.Sensors.stepCount() )
        val startMeasures = ConcurrentTask( "Start measures", measures )
        protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

        return protocol
    }

    /**
     * A stub [DataListener] which supports the expected data types in [createExampleProtocol].
     */
    private fun createDataCollectorFactory() = createDataCollectorFactory(
        CarpDataTypes.GEOLOCATION, CarpDataTypes.STEPCOUNT
    )

    private val accountService = InMemoryAccountService()
    private val accountIdentity: AccountIdentity = AccountIdentity.fromUsername( "Test user" )
    private suspend fun getLoggedInUser(): Account = accountService.findAccount( accountIdentity )!!
}
