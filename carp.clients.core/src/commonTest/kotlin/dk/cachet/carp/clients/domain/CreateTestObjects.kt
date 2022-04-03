package dk.cachet.carp.clients.domain

import dk.cachet.carp.clients.domain.data.DataListener
import dk.cachet.carp.clients.domain.data.DeviceDataCollector
import dk.cachet.carp.clients.domain.data.DeviceDataCollectorFactory
import dk.cachet.carp.clients.domain.data.StubDeviceDataCollector
import dk.cachet.carp.clients.domain.data.StubDeviceDataCollectorFactory
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataTypeMetaData
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.StudyProtocol


val smartphone: Smartphone = Smartphone( "User's phone" )
val connectedDevice = StubDeviceConfiguration( "Connected sensor" )
val deviceSmartphoneDependsOn: AnyPrimaryDeviceConfiguration = Smartphone( "Some other device" )

/**
 * Create a study protocol with [smartphone] as the single primary device, i.e., a typical 'smartphone study'.
 */
fun createSmartphoneStudy(): StudyProtocol
{
    val ownerId = UUID.randomUUID()
    val protocol = StudyProtocol( ownerId, "Smartphone study" )
    protocol.addPrimaryDevice( smartphone )
    return protocol
}

/**
 * Create a study protocol with [smartphone] as the single primary device and [connectedDevice] as a single connected device.
 */
fun createSmartphoneWithConnectedDeviceStudy(): StudyProtocol
{
    val ownerId = UUID.randomUUID()
    val protocol = StudyProtocol( ownerId, "Smartphone study" )
    protocol.addPrimaryDevice( smartphone )
    protocol.addConnectedDevice( connectedDevice, smartphone )
    return protocol
}

/**
 * Create a study protocol with [smartphone] as a primary device which also depends on another primary device for study execution ([deviceSmartphoneDependsOn]).
 */
fun createDependentSmartphoneStudy(): StudyProtocol
{
    val ownerId = UUID.randomUUID()
    val protocol = StudyProtocol( ownerId, "Smartphone study" )
    protocol.addPrimaryDevice( smartphone )
    // TODO: Right now simply adding another primary device is sufficient to create a dependency.
    //       However, when optimizing this to figure out dependencies based on triggers this might no longer be the case and tests might fail.
    protocol.addPrimaryDevice( deviceSmartphoneDependsOn )
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
        InMemoryDataStreamService(),
        eventBus.createApplicationServiceAdapter( DeploymentService::class ) )
    val invitation = createParticipantInvitation()
    val status = deploymentService.createStudyDeployment( UUID.randomUUID(), protocol.getSnapshot(), listOf( invitation ) )
    return Pair( deploymentService, status )
}

/**
 * Create a participant invitation for a specific [identity], or newly created identity when null,
 * which is assigned to all participant roles.
 */
fun createParticipantInvitation( identity: AccountIdentity? = null ): ParticipantInvitation =
    ParticipantInvitation(
        UUID.randomUUID(),
        AssignedTo.Anyone,
        identity ?: AccountIdentity.fromUsername( "Test" ),
        StudyInvitation( "Some study" )
    )

/**
 * Create a [DeviceDataCollectorFactory] which for all [DeviceDataCollector] instances
 * uses [StubDeviceDataCollector] with the specified [supportedDataTypes].
 */
fun createDataCollectorFactory( vararg supportedDataTypes: DataTypeMetaData ): DeviceDataCollectorFactory =
    supportedDataTypes
        .map { it.type }
        .toSet()
        .let { StubDeviceDataCollectorFactory( it, it ) }

/**
 * Create a data listener which supports the specified [supportedDataTypes].
 */
fun createDataListener( vararg supportedDataTypes: DataTypeMetaData ): DataListener =
    DataListener( createDataCollectorFactory( *supportedDataTypes ) )
