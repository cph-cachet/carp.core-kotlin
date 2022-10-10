package dk.cachet.carp.protocols.infrastructure.test

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.devices.PrimaryDeviceConfiguration
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration
import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers),
 * and initializes the infrastructure serializer to be aware about polymorph stub testing classes.
 */
fun createEmptyProtocol( name: String = "Test protocol" ): StudyProtocol
{
    JSON = createDefaultJSON( STUBS_SERIAL_MODULE )

    val alwaysSameOwnerId = UUID( "27879e75-ccc1-4866-9ab3-4ece1b735052" )
    return StudyProtocol( alwaysSameOwnerId, name, "Test description" )
}

/**
 * Creates a study protocol with a single primary device.
 */
fun createSinglePrimaryDeviceProtocol( primaryDeviceName: String = "Primary" ): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val primary = StubPrimaryDeviceConfiguration( primaryDeviceName )
    protocol.addPrimaryDevice( primary )
    return protocol
}

/**
 * Creates a study protocol with a single primary device which has a single connected device.
 */
fun createSinglePrimaryWithConnectedDeviceProtocol(
    primaryDeviceName: String = "Primary",
    connectedDeviceName: String = "Connected"
): SinglePrimaryWithConnectedTestProtocol
{
    val protocol = createEmptyProtocol()
    val primary = StubPrimaryDeviceConfiguration( primaryDeviceName )
    protocol.addPrimaryDevice( primary )
    val connected = StubDeviceConfiguration( connectedDeviceName )
    protocol.addConnectedDevice( connected, primary )

    return SinglePrimaryWithConnectedTestProtocol( protocol, primary, connected )
}

data class SinglePrimaryWithConnectedTestProtocol(
    val protocoL: StudyProtocol,
    val primary: AnyPrimaryDeviceConfiguration,
    val connected: AnyDeviceConfiguration
)

/**
 * Creates a study protocol with a couple of devices and tasks added.
 */
fun createComplexProtocol(): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val primaryDevice = StubPrimaryDeviceConfiguration()
    val connectedDevice = StubDeviceConfiguration()
    val chainedPrimaryDevice = StubPrimaryDeviceConfiguration( "Chained primary" )
    val chainedConnectedDevice = StubDeviceConfiguration( "Chained connected" )
    val trigger = StubTriggerConfiguration( connectedDevice )
    val measures = listOf( Measure.DataStream( STUB_DATA_POINT_TYPE ) )
    val task = StubTaskConfiguration( "Task", measures )
    val mainRole = ParticipantRole( "Role", false )
    val optionalRole = ParticipantRole( "Optional role", true )
    val commonExpectedData =
        ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) ),
            AssignedTo.All
        )
    val mainRoleData =
        ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ),
            AssignedTo.Roles( setOf( mainRole.role ) )
        )
    with ( protocol )
    {
        addPrimaryDevice( primaryDevice )
        addConnectedDevice( connectedDevice, primaryDevice )
        addConnectedDevice( chainedPrimaryDevice, primaryDevice )
        addConnectedDevice( chainedConnectedDevice, chainedPrimaryDevice )
        addTaskControl( trigger, task, primaryDevice, TaskControl.Control.Start )
        addParticipantRole( mainRole )
        addParticipantRole( optionalRole )
        changeDeviceAssignment( primaryDevice, AssignedTo.Roles( setOf( mainRole.role ) ) )
        addExpectedParticipantData( commonExpectedData )
        addExpectedParticipantData( mainRoleData )
    }

    return protocol
}
