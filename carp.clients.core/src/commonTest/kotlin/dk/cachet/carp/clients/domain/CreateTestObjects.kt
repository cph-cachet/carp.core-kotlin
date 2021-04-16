package dk.cachet.carp.clients.domain

import dk.cachet.carp.clients.domain.data.DataListener
import dk.cachet.carp.clients.domain.data.DeviceDataCollector
import dk.cachet.carp.clients.domain.data.DeviceDataCollectorFactory
import dk.cachet.carp.clients.domain.data.StubDeviceDataCollector
import dk.cachet.carp.clients.domain.data.StubDeviceDataCollectorFactory
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol


val smartphone: Smartphone = Smartphone( "User's phone" )
val connectedDevice = StubDeviceDescriptor( "Connected sensor" )
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
 * Create a study protocol with [smartphone] as the single master device and [connectedDevice] as a single connected device.
 */
fun createSmartphoneWithConnectedDeviceStudy(): StudyProtocol
{
    val protocol = StudyProtocol( ProtocolOwner(), "Smartphone study" )
    protocol.addMasterDevice( smartphone )
    protocol.addConnectedDevice( connectedDevice, smartphone )
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
    val eventBus = SingleThreadedEventBus()

    val deploymentService = DeploymentServiceHost(
        InMemoryDeploymentRepository(),
        eventBus.createApplicationServiceAdapter( DeploymentService::class ) )
    val status = deploymentService.createStudyDeployment( protocol.getSnapshot() )
    return Pair( deploymentService, status )
}

/**
 * Create a [DeviceDataCollectorFactory] which for all [DeviceDataCollector] instances
 * uses [StubDeviceDataCollector] with the specified [supportedDataTypes].
 */
fun createDataCollectorFactory( vararg supportedDataTypes: DataType ): DeviceDataCollectorFactory =
    supportedDataTypes.toSet().let { StubDeviceDataCollectorFactory( it, it ) }

/**
 * Create a data listener which supports the specified [supportedDataTypes].
 */
fun createDataListener( vararg supportedDataTypes: DataType ): DataListener =
    DataListener( createDataCollectorFactory( *supportedDataTypes ) )