package bhrp.studyprotocols.domain.tasks

import bhrp.studyprotocols.domain.InvalidConfigurationError
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