package carp.protocols.domain.tasks

import carp.protocols.domain.InvalidConfigurationError
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test


/**
 * Tests for [TaskDescriptor].
 */
class TaskDescriptorTest
{
    @Test
    fun `mutable implementation triggers exception`()
    {
        class NoDataClass(
            override val name: String = "Not a data class",
            override val measures: List<Measure> = listOf() ) : TaskDescriptor()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}