package dk.cachet.carp.protocols.domain.deployment

import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*


/**
 * Tests for [OnlyOptionalDevicesWarning].
 */
class OnlyOptionalDevicesWarningTest
{
    @Test
    fun isIssuePresent_true_with_only_optional_primary_devices()
    {
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( StubPrimaryDeviceConfiguration( "Optional 1", isOptional = true ) )
            addPrimaryDevice( StubPrimaryDeviceConfiguration( "Optional 2", isOptional = true ) )
        }

        val warning = OnlyOptionalDevicesWarning()
        assertTrue( warning.isIssuePresent( protocol ) )
    }

    @Test
    fun isIssuePresent_false_with_at_least_one_required_primary_devices()
    {
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( StubPrimaryDeviceConfiguration( "Required", isOptional = false ) )
            addPrimaryDevice( StubPrimaryDeviceConfiguration( "Optional", isOptional = true ) )
        }

        val warning = OnlyOptionalDevicesWarning()
        assertFalse( warning.isIssuePresent( protocol ) )
    }
}
