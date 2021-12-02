package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*


/**
 * Tests for [OnlyOptionalDevicesWarning].
 */
class OnlyOptionalDevicesWarningTest
{
    @Test
    fun isIssuePresent_true_with_only_optional_master_devices()
    {
        val protocol = createEmptyProtocol().apply {
            addMasterDevice( StubMasterDeviceDescriptor( "Optional 1", isOptional = true ) )
            addMasterDevice( StubMasterDeviceDescriptor( "Optional 2", isOptional = true ) )
        }

        val warning = OnlyOptionalDevicesWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun isIssuePresent_false_with_at_least_one_required_master_devices()
    {
        val protocol = createEmptyProtocol().apply {
            addMasterDevice( StubMasterDeviceDescriptor( "Required", isOptional = false ) )
            addMasterDevice( StubMasterDeviceDescriptor( "Optional", isOptional = true ) )
        }

        val warning = OnlyOptionalDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
    }
}
