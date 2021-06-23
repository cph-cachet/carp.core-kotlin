package dk.cachet.carp.protocols.infrastructure.test

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor
import dk.cachet.carp.common.infrastructure.test.StubTrigger
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers),
 * and initializes the infrastructure serializer to be aware about polymorph stub testing classes.
 */
fun createEmptyProtocol( name: String = "Test protocol" ): StudyProtocol
{
    JSON = createDefaultJSON( STUBS_SERIAL_MODULE )

    val alwaysSameOwner = ProtocolOwner( UUID( "27879e75-ccc1-4866-9ab3-4ece1b735052" ) )
    return StudyProtocol( alwaysSameOwner, name, "Test description" )
}

/**
 * Creates a study protocol with a single master device.
 */
fun createSingleMasterDeviceProtocol( masterDeviceName: String = "Master" ): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val master = StubMasterDeviceDescriptor( masterDeviceName )
    protocol.addMasterDevice( master )
    return protocol
}

/**
 * Creates a study protocol with a single master device which has a single connected device.
 */
fun createSingleMasterWithConnectedDeviceProtocol(
    masterDeviceName: String = "Master",
    connectedDeviceName: String = "Connected"
): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val master = StubMasterDeviceDescriptor( masterDeviceName )
    protocol.addMasterDevice( master )
    protocol.addConnectedDevice( StubDeviceDescriptor( connectedDeviceName ), master )
    return protocol
}

/**
 * Creates a study protocol with a couple of devices and tasks added.
 */
fun createComplexProtocol(): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val masterDevice = StubMasterDeviceDescriptor()
    val connectedDevice = StubDeviceDescriptor()
    val chainedMasterDevice = StubMasterDeviceDescriptor( "Chained master" )
    val chainedConnectedDevice = StubDeviceDescriptor( "Chained connected" )
    val trigger = StubTrigger( connectedDevice )
    val measures = listOf( Measure.DataStream( STUB_DATA_TYPE ) )
    val task = StubTaskDescriptor( "Task", measures )
    val expectedParticipantData = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
    with ( protocol )
    {
        addMasterDevice( masterDevice )
        addConnectedDevice( connectedDevice, masterDevice )
        addConnectedDevice( chainedMasterDevice, masterDevice )
        addConnectedDevice( chainedConnectedDevice, chainedMasterDevice )
        addTaskControl( trigger, task, masterDevice, TaskControl.Control.Start )
        addExpectedParticipantData( expectedParticipantData )
    }

    return protocol
}
