@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_TYPE
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [CustomTaskConfiguration].
 */
class CustomTaskConfigurationTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON( STUBS_SERIAL_MODULE )
    }


    @Test
    fun initialization_from_json_extracts_base_TaskConfiguration_properties()
    {
        val measures: List<Measure> = listOf( Measure.DataStream( STUB_DATA_TYPE ) )
        val task = StubTaskConfiguration( "Unknown", measures )
        val serialized: String = JSON.encodeToString( StubTaskConfiguration.serializer(), task )

        val custom = CustomTaskConfiguration( "Irrelevant", serialized, JSON )
        assertEquals( task.name, custom.name )
        assertEquals( task.measures.count(), task.measures.intersect( custom.measures ).count() )
    }

    @Serializable
    internal data class IncorrectTask( val incorrect: String = "Not a task." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectTask()
        val serialized: String = JSON.encodeToString( IncorrectTask.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomTaskConfiguration( "Irrelevant", serialized, JSON )
        }
    }
}
