package bhrp.studyprotocols.domain.data

import bhrp.studyprotocols.domain.InvalidConfigurationError
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test


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