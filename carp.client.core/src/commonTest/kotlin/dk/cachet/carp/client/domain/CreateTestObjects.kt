package dk.cachet.carp.client.domain

import dk.cachet.carp.deployment.application.DeploymentManager
import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.*


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
 * Create a deployment manager which contains a study deployment for the specified [protocol].
 */
fun createStudyDeployment( protocol: StudyProtocol ): Pair<DeploymentManager, StudyDeploymentStatus>
{
    val deploymentManager = DeploymentManager( InMemoryDeploymentRepository() )
    val status = deploymentManager.createStudyDeployment( protocol.getSnapshot() )
    return Pair( deploymentManager, status )
}