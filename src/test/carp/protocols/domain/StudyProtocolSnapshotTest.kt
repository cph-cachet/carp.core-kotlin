package carp.protocols.domain

import kotlinx.serialization.json.JSON
import org.junit.jupiter.api.*
import org.junit.Assert.*


/**
 * Tests for [StudyProtocolSnapshot].
 */
class StudyProtocolSnapshotTest
{
    @Test
    fun `can (de)serialize snapshot using JSON`()
    {
        val protocol = createEmptyProtocol()
        val snapshot = protocol.getSnapshot()

        val serialized: String = JSON.stringify( snapshot )
        val parsed: StudyProtocolSnapshot = JSON.parse( serialized )

        assertEquals( snapshot, parsed )
    }
}