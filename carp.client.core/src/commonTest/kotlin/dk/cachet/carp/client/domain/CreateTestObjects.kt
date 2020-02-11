package dk.cachet.carp.client.domain

import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.Smartphone


val smartphone: Smartphone = Smartphone( "User's phone" )
val deviceSmartphoneDependsOn: AnyMasterDeviceDescriptor = Smartphone( "Some other device" )

/**
 * Create a study protocol with [smartphone] as the single master device, i.e., a typical 'smartphone study'.
 */
fun createSmartphoneStudy(): StudyProtocol
{
    val protocol = StudyProtocol( ProtocolOwner(), "Smartphone study" )
    protocol.addMasterDevice( smartphone )
    return protocol
}

/**
 * Create a study protocol with [smartphone] as a master device which also depends on another master device for study execution ([deviceSmartphoneDependsOn]).
 */
fun createDependentSmartphoneStudy(): StudyProtocol
{
    val protocol = StudyProtocol( ProtocolOwner(), "Smartphone study" )
    protocol.addMasterDevice( smartphone )
    // TODO: Right now simply adding another master device is sufficient to create a dependency.
    //       However, when optimizing this to figure out dependencies based on triggers this might no longer be the case and tests might fail.
    protocol.addMasterDevice( deviceSmartphoneDependsOn )
    return protocol
}

/**
 * Create a deployment service which contains a study deployment for the specified [protocol].
 */
suspend fun createStudyDeployment( protocol: StudyProtocol ): Pair<DeploymentService, StudyDeploymentStatus>
{
    val deploymentService = DeploymentServiceHost( InMemoryDeploymentRepository(), InMemoryAccountService() )
    val status = deploymentService.createStudyDeployment( protocol.getSnapshot() )
    return Pair( deploymentService, status )
}
