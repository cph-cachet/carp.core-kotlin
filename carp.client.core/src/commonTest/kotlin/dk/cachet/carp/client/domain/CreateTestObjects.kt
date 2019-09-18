package dk.cachet.carp.client.domain

import dk.cachet.carp.deployment.application.DeploymentManager
import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.protocols.domain.*
import dk.cachet.carp.protocols.domain.devices.Smartphone


val smartphone: Smartphone = Smartphone( "User's phone" )

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
 * Create a deployment manager which contains a study deployment for the specified [protocol].
 */
fun createStudyDeployment( protocol: StudyProtocol ): Pair<DeploymentManager, StudyDeploymentStatus>
{
    val deploymentManager = DeploymentManager( InMemoryDeploymentRepository() )
    val status = deploymentManager.createStudyDeployment( protocol.getSnapshot() )
    return Pair( deploymentManager, status )
}