package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.domain.createEmptyProtocol
import dk.cachet.carp.protocols.domain.devices.StubMasterDeviceDescriptor
import org.junit.jupiter.api.Test
import org.junit.Assert.*


/**
 * Tests for [NoMasterDeviceError].
 */
class NoMasterDeviceErrorTest
{
    @Test
    fun `isIssuePresent true with no master device`()
    {
        val emptyProtocol = createEmptyProtocol()
        val error = NoMasterDeviceError()

        assertTrue( error.isIssuePresent( emptyProtocol ) )
    }

    @Test
    fun `isIssuePresent false with master device`()
    {
        val protocol = createEmptyProtocol()
        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        val error = NoMasterDeviceError()

        assertFalse( error.isIssuePresent( protocol ) )
    }
}