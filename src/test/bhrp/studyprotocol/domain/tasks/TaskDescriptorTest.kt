package bhrp.studyprotocol.domain.tasks

import bhrp.studyprotocol.domain.InvalidConfigurationError
import org.junit.jupiter.api.*
import kotlin.test.*


/**
 * Tests for [TaskDescriptor].
 */
class TaskDescriptorTest
{
    @Test
    fun `mutable implementation triggers exception`()
    {
        class NoDataClass( override val name: String = "Not a data class" ) : TaskDescriptor()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}