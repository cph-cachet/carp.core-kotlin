package bhrp.studyprotocol.domain.deployment

import bhrp.studyprotocol.domain.createEmptyProtocol
import bhrp.studyprotocol.domain.devices.*
import bhrp.studyprotocol.domain.tasks.StubTaskDescriptor
import bhrp.studyprotocol.domain.triggers.StubTrigger
import org.junit.jupiter.api.*
import kotlin.test.*


class UnusedDevicesWarningTest
{
    @Test
    fun `isIssuePresent true with unused master device`()
    {
        val protocol = createEmptyProtocol()
        val unusedDevice = StubMasterDeviceDescriptor()
        protocol.addMasterDevice( unusedDevice )

        val warning = UnusedDevicesWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
        val expectedUnused = listOf( unusedDevice )
        val unused = warning.getUnusedDevices( protocol )
        assertEquals( expectedUnused.count(), unused.intersect( expectedUnused ).count() )
    }

    @Test
    fun `isIssuePresent true with unused connected device`()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor()
        val unusedConnected = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( master )
            addConnectedDevice( unusedConnected, master )
            addTriggeredTask( StubTrigger( master ), StubTaskDescriptor(), master )
        }

        val warning = UnusedDevicesWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
        val expectedUnused = listOf( unusedConnected )
        val unused = warning.getUnusedDevices( protocol )
        assertEquals( expectedUnused.count(), unused.intersect( expectedUnused ).count() )
    }

    @Test
    fun `isIssuePresent false when master device not used in triggers, but relays data`()
    {
        val protocol = createEmptyProtocol()
        val master = StubMasterDeviceDescriptor()
        val connected = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( master )
            addConnectedDevice( connected, master )
            addTriggeredTask( StubTrigger( connected ), StubTaskDescriptor(), connected )
        }

        val warning = UnusedDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( 0, warning.getUnusedDevices( protocol ).count() )
    }

    @Test
    fun `isIssuePresent false when chained master devices are not used in triggers, but relay data`()
    {
        val protocol = createEmptyProtocol()
        val master1 = StubMasterDeviceDescriptor( "Master 1" )
        val master2 = StubMasterDeviceDescriptor( "Master 2" )
        val connected = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( master1 )
            addConnectedDevice( master2, master1 )
            addConnectedDevice( connected, master2 )
            addTriggeredTask( StubTrigger( connected ), StubTaskDescriptor(), connected )
        }

        val warning = UnusedDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( 0, warning.getUnusedDevices( protocol ).count() )
    }

    @Test
    fun `isIssuePresent false when all devices are used in triggers`()
    {
        val protocol = createEmptyProtocol()
        val device1 = StubMasterDeviceDescriptor()
        val device2 = StubDeviceDescriptor()
        with ( protocol )
        {
            addMasterDevice( device1 )
            addConnectedDevice( device2, device1 )
            addTriggeredTask( StubTrigger( device1 ), StubTaskDescriptor(), device2 )
        }

        val warning = UnusedDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
        assertEquals( 0, warning.getUnusedDevices( protocol ).count() )
    }
}