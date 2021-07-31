package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.StubData
import dk.cachet.carp.data.application.DataStreamSequence
import dk.cachet.carp.data.application.DataStreamSequenceSerializer
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.application.SyncPoint
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [DataStreamSequence] relying on core infrastructure.
 */
class DataStreamSequenceTest
{
    private val json = createDefaultJSON( STUBS_SERIAL_MODULE )
    private val testDataStreamSequence =
        with(
            MutableDataStreamSequence(
                dataStreamId<StubData>( UUID.randomUUID(), "Device" ),
                0,
                listOf( 1 ),
                SyncPoint( Clock.System.now() )
            )
        )
        {
            appendMeasurements(
                measurement( StubData(), 0 ),
                measurement( StubData(), 1 )
            )
            this
        }

    @Test
    fun can_serialize_and_deserialize_DataStreamSequence()
    {
        val serialized = json.encodeToString( DataStreamSequenceSerializer, testDataStreamSequence )
        val parsed: DataStreamSequence = json.decodeFromString( DataStreamSequenceSerializer, serialized )

        assertEquals( testDataStreamSequence, parsed )
    }

    @Test
    fun deserialization_incorrect_DataStreamSequence_fails()
    {
        val incorrectFirstSequenceId =
            """
            {
               "dataStream":{
                  "studyDeploymentId":"48a0312b-7536-43c2-af8b-8507b75642aa",
                  "deviceRoleName":"Device",
                  "dataType":"dk.cachet.carp.stub"
               },
               "firstSequenceId":-1,
               "measurements":[],
               "triggerIds":[1],
               "syncPoint":{
                  "utcTime":"2021-07-28T09:45:28.603825300Z",
                  "utcOffset":0,
                  "relativeClockSpeed":1.0
               }
            }
            """

        val json = createDefaultJSON( STUBS_SERIAL_MODULE )
        assertFailsWith<IllegalArgumentException>
        {
            json.decodeFromString( DataStreamSequenceSerializer, incorrectFirstSequenceId )
        }
    }
}
