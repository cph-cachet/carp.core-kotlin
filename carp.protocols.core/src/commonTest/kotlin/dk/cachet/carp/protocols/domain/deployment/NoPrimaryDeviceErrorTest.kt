package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*


/**
 * Tests for [NoPrimaryDeviceError].
 */
class NoPrimaryDeviceErrorTest
{
    @Test
    fun isIssuePresent_true_with_no_primary_device()
    {
        val emptyProtocol = createEmptyProtocol()
        val error = NoPrimaryDeviceError()

        assertTrue( error.isIssuePresent( emptyProtocol ) )
    }

    @Test
    fun isIssuePresent_false_with_primary_device()
    {
        val protocol = createEmptyProtocol()
        protocol.addPrimaryDevice( StubPrimaryDeviceConfiguration() )
        val error = NoPrimaryDeviceError()

        assertFalse( error.isIssuePresent( protocol ) )
    }
}
