package dk.cachet.carp.client.domain

import dk.cachet.carp.client.domain.data.DataListener
import dk.cachet.carp.client.domain.data.DeviceDataCollector
import dk.cachet.carp.client.domain.data.DeviceDataCollectorFactory
import dk.cachet.carp.client.domain.data.StubDeviceDataCollector
import dk.cachet.carp.client.domain.data.StubDeviceDataCollectorFactory
import dk.cachet.carp.common.data.DataType
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

/**
 * Create a [DeviceDataCollectorFactory] which for all [DeviceDataCollector] instances
 * uses [StubDeviceDataCollector] with the specified [supportedDataTypes].
 */
fun createDataCollectorFactory( vararg supportedDataTypes: DataType ): DeviceDataCollectorFactory =
    StubDeviceDataCollectorFactory( supportedDataTypes.toSet() )

/**
 * Create a data listener which supports the specified [supportedDataTypes].
 */
fun createDataListener( vararg supportedDataTypes: DataType ): DataListener =
    DataListener( createDataCollectorFactory( *supportedDataTypes ) )
