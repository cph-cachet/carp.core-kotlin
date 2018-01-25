package bhrp.studyprotocols.domain.devices

import bhrp.studyprotocols.domain.*
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test


/**
 * Tests for [DeviceDescriptor].
 */
class DeviceDescriptorTest
{
    @Test
    fun `mutable implementation triggers exception`()
    {
        class NoDataClass( override val roleName: String = "Not a data class" ) : DeviceDescriptor()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}