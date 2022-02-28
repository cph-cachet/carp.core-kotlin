package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTrigger
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*


class UnusedDevicesWarningTest
{
    @Test
    fun isIssuePresent_true_with_unused_primary_device()
    {
        val protocol = createEmptyProtocol()
        val unusedDevice = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( unusedDevice )

        val warning = UnusedDevicesWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
        val expectedUnused = listOf( unusedDevice )
        val unused = warning.getUnusedDevices( protocol )
        assertEquals( expectedUnused.count(), unused.intersect( expectedUnused ).count() )
    }

    @Test
    fun isIssuePresent_true_with_unused_connected_device()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration()
        val unusedConnected = StubDeviceConfiguration()
        with ( protocol )
        {
            addPrimaryDevice( primary )
            addConnectedDevice( unusedConnected, primary )
            addTaskControl( StubTrigger( primary ).start( StubTaskConfiguration(), primary ) )
        }

        val warning = UnusedDevicesWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
        val expectedUnused = listOf( unusedConnected )
        val unused = warning.getUnusedDevices( protocol )
        assertEquals( expectedUnused.count(), unused.intersect( expectedUnused ).count() )
    }

    @Test
    fun isIssuePresent_false_when_primary_device_not_used_in_triggers_but_relays_data()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration()
        val connected = StubDeviceConfiguration()
        with ( protocol )
        {
            addPrimaryDevice( primary )
            addConnectedDevice( connected, primary )
            addTaskControl( StubTrigger( connected ).start( StubTaskConfiguration(), connected ) )
        }

        val warning = UnusedDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( 0, warning.getUnusedDevices( protocol ).count() )
    }

    @Test
    fun isIssuePresent_false_when_chained_primary_devices_are_not_used_in_triggers_but_relay_data()
    {
        val protocol = createEmptyProtocol()
        val primary1 = StubPrimaryDeviceConfiguration( "Primary 1" )
        val primary2 = StubPrimaryDeviceConfiguration( "Primary 2" )
        val connected = StubDeviceConfiguration()
        with ( protocol )
        {
            addPrimaryDevice( primary1 )
            addConnectedDevice( primary2, primary1 )
            addConnectedDevice( connected, primary2 )
            addTaskControl( StubTrigger( connected ).start( StubTaskConfiguration(), connected ) )
        }

        val warning = UnusedDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( 0, warning.getUnusedDevices( protocol ).count() )
    }

    @Test
    fun isIssuePresent_false_when_all_devices_are_used_in_triggers()
    {
        val protocol = createEmptyProtocol()
        val device1 = StubPrimaryDeviceConfiguration()
        val device2 = StubDeviceConfiguration()
        with ( protocol )
        {
            addPrimaryDevice( device1 )
            addConnectedDevice( device2, device1 )
            addTaskControl( StubTrigger( device1 ).start( StubTaskConfiguration(), device2 ) )
        }

        val warning = UnusedDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( 0, warning.getUnusedDevices( protocol ).count() )
    }
}
