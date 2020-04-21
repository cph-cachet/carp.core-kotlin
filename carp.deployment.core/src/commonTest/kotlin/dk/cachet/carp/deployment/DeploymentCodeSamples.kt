package dk.cachet.carp.deployment

import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


class DeploymentCodeSamples
{
    @Test
    fun readme() = runBlockingTest {
        val deploymentService: DeploymentService = createDeploymentEndpoint()
        val trackPatientStudy: StudyProtocol = createExampleProtocol()
        val patientPhone: Smartphone = trackPatientStudy.masterDevices.first() as Smartphone // "Patient's phone"

        // This is called by `StudyService` when deploying a participant group.
        var status: StudyDeploymentStatus = deploymentService.createStudyDeployment( trackPatientStudy.getSnapshot() )
        val studyDeploymentId = status.studyDeploymentId

        // What comes after is called by `ClientManager` in `carp.client`:
        // - Register the device to be deployed.
        val registration = patientPhone.createRegistration {
            // Device-specific registration options can be accessed from here.
            // Depending on the device type, different options are available.
            // E.g., for a smartphone, a UUID deviceId is generated. To override this default:
            deviceId = "xxxxxxxxx"
        }
        status = deploymentService.registerDevice( studyDeploymentId, patientPhone.roleName, registration )

        // - Retrieve information on what to run and indicate the device is ready to collect the requested data.
        val patientPhoneStatus: DeviceDeploymentStatus = status.getDeviceStatus( patientPhone )
        if ( patientPhoneStatus.canObtainDeviceDeployment ) // True since there are no dependent devices.
        {
            val deploymentInformation: MasterDeviceDeployment =
                deploymentService.getDeviceDeploymentFor( studyDeploymentId, patientPhone.roleName )
            val deploymentChecksum: Int = deploymentInformation.getChecksum() // To verify correct deployment.
            deploymentService.deploymentSuccessful( studyDeploymentId, patientPhone.roleName, deploymentChecksum )
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

        val measures = listOf( Smartphone.geolocation(), Smartphone.stepcount() )
        val startMeasures = ConcurrentTask( "Start measures", measures )
        protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

        return protocol
    }

    private fun createDeploymentEndpoint(): DeploymentService =
        DeploymentServiceHost( InMemoryDeploymentRepository(), InMemoryAccountService() )
}
