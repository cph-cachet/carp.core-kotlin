package bhrp.studyprotocols.domain.data

import bhrp.studyprotocols.domain.InvalidConfigurationError
import org.junit.jupiter.api.*
import kotlin.test.*


/**
 * Tests for [DataType].
 */
class DataTypeTest
{
    @Test
    fun `mutable implementation triggers exception`()
    {
        class NoDataClass : DataType()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}