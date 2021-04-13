package dk.cachet.carp.deployment

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.tasks.ConcurrentTask
import dk.cachet.carp.common.domain.ProtocolOwner
import dk.cachet.carp.common.domain.StudyProtocol
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.application.DeviceDeploymentStatus
import dk.cachet.carp.deployment.application.MasterDeviceDeployment
import dk.cachet.carp.deployment.application.StudyDeploymentStatus
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


class DeploymentCodeSamples
{
    @Test
    @Suppress( "UnusedPrivateMember" )
    fun readme() = runSuspendTest {
        val deploymentService: DeploymentService = createDeploymentEndpoint()
        val trackPatientStudy: StudyProtocol = createExampleProtocol()
        val patientPhone: Smartphone = trackPatientStudy.masterDevices.first() as Smartphone // "Patient's phone"

        // This is called by `StudyService` when deploying a participant group.
        var status: StudyDeploymentStatus = deploymentService.createStudyDeployment( trackPatientStudy.getSnapshot() )
        val studyDeploymentId = status.studyDeploymentId

        // What comes after is similar to what is called by the client in `carp.client`:
        // - Register the device to be deployed.
        val registration = patientPhone.createRegistration()
        status = deploymentService.registerDevice( studyDeploymentId, patientPhone.roleName, registration )

        // - Retrieve information on what to run and indicate the device is ready to collect the requested data.
        val patientPhoneStatus: DeviceDeploymentStatus = status.getDeviceStatus( patientPhone )
        if ( patientPhoneStatus.canObtainDeviceDeployment ) // True since there are no dependent devices.
        {
            val deploymentInformation: MasterDeviceDeployment =
                deploymentService.getDeviceDeploymentFor( studyDeploymentId, patientPhone.roleName )
            val deploymentDate: DateTime = deploymentInformation.lastUpdateDate // To verify correct deployment.
            deploymentService.deploymentSuccessful( studyDeploymentId, patientPhone.roleName, deploymentDate )
        }

        // Now that all devices have been registered and deployed, the deployment is ready.
        status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val isReady = status is StudyDeploymentStatus.DeploymentReady // True.
    }


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

    private val eventBus: EventBus = SingleThreadedEventBus()
    private fun createDeploymentEndpoint(): DeploymentService = DeploymentServiceHost(
        InMemoryDeploymentRepository(),
        eventBus.createApplicationServiceAdapter( DeploymentService::class ) )
}
