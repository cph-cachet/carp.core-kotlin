package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.protocols.domain.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.StubMasterDeviceDescriptor
import kotlin.test.*


/**
 * Tests for [NoMasterDeviceError].
 */
class NoMasterDeviceErrorTest
{
    @Test
    fun isIssuePresent_true_with_no_master_device()
    {
        val emptyProtocol = createEmptyProtocol()
        val error = NoMasterDeviceError()

        assertTrue( error.isIssuePresent( emptyProtocol ) )
    }

    @Test
    fun isIssuePresent_false_with_master_device()
    {
        val protocol = createEmptyProtocol()
        protocol.addMasterDevice( StubMasterDeviceDescriptor() )
        val error = NoMasterDeviceError()

        assertFalse( error.isIssuePresent( protocol ) )
    }
}
