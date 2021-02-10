package dk.cachet.carp.client

import dk.cachet.carp.client.domain.SmartphoneClient
import dk.cachet.carp.client.domain.StudyRuntimeStatus
import dk.cachet.carp.client.domain.createDataCollectorFactory
import dk.cachet.carp.client.domain.data.DataListener
import dk.cachet.carp.client.infrastructure.InMemoryClientRepository
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.data.CarpDataTypes
import dk.cachet.carp.common.ddd.SingleThreadedEventBus
import dk.cachet.carp.common.ddd.createApplicationServiceAdapter
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.application.ParticipationService
import dk.cachet.carp.deployment.application.ParticipationServiceHost
import dk.cachet.carp.deployment.domain.users.ActiveParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployment.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.protocols.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


class ClientCodeSamples
{
    @Test
    @Suppress( "UnusedPrivateMember" )
    fun readme() = runSuspendTest {
        val (participationService, deploymentService) = createEndpoints()
        val dataCollectorFactory = createDataCollectorFactory()

        // Retrieve invitation to participate in the study using a specific device.
        val account: Account = getLoggedInUser()
        val invitation: ActiveParticipationInvitation =
            participationService.getActiveParticipationInvitations( account.id ).first()
        val studyDeploymentId: UUID = invitation.participation.studyDeploymentId
        val deviceToUse: String = invitation.devices.first().masterDevice.roleName // This matches "Patient's phone".

        // Create a study runtime for the study.
        val clientRepository = createRepository()
        val client = SmartphoneClient( clientRepository, deploymentService, dataCollectorFactory )
        client.configure {
            // Device-specific registration options can be accessed from here.
            // Depending on the device type, different options are available.
            // E.g., for a smartphone, a UUID deviceId is generated. To override this default:
            deviceId = "xxxxxxxxx"
        }
        var status: StudyRuntimeStatus = client.addStudy( studyDeploymentId, deviceToUse )

        // Register connected devices in case needed.
        if ( status is StudyRuntimeStatus.RegisteringDevices )
        {
            val connectedDevice = status.remainingDevicesToRegister.first()
            val connectedRegistration = connectedDevice.createRegistration()
            deploymentService.registerDevice( studyDeploymentId, connectedDevice.roleName, connectedRegistration )

            // Re-try deployment now that devices have been registered.
            status = client.tryDeployment( status.id )
            val isDeployed = status is StudyRuntimeStatus.Deployed // True.
        }
    }


    private suspend fun createEndpoints(): Pair<ParticipationService, DeploymentService>
    {
        val eventBus = SingleThreadedEventBus()

        val deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            eventBus.createApplicationServiceAdapter( DeploymentService::class ) )

        val participationService = ParticipationServiceHost(
            InMemoryParticipationRepository(),
            accountService,
            eventBus.createApplicationServiceAdapter( ParticipationService::class ) )

        // Create deployment for the example protocol.
        val protocol = createExampleProtocol()
        val status = deploymentService.createStudyDeployment( protocol.getSnapshot() )

        // Invite a participant.
        val phone = protocol.masterDevices.first()
        val invitation = StudyInvitation.empty()
        participationService.addParticipation( status.studyDeploymentId, setOf( phone.roleName ), accountIdentity, invitation )
        accountService.findAccount( accountIdentity )

        return Pair( participationService, deploymentService )
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

        val connected = StubDeviceDescriptor( "External sensor" )
        protocol.addConnectedDevice( connected, phone )

        val measures = listOf( Smartphone.Sensors.geolocation(), Smartphone.Sensors.stepCount() )
        val startMeasures = ConcurrentTask( "Start measures", measures )
        protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

        return protocol
    }

    /**
     * A stub [DataListener] which supports the expected data types in [createExampleProtocol].
     */
    private fun createDataCollectorFactory() = createDataCollectorFactory(
        CarpDataTypes.GEOLOCATION, CarpDataTypes.STEP_COUNT
    )

    private val accountService = InMemoryAccountService()
    private val accountIdentity: AccountIdentity = AccountIdentity.fromUsername( "Test user" )
    private suspend fun getLoggedInUser(): Account = accountService.findAccount( accountIdentity )!!
}
