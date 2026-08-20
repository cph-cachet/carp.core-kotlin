package dk.cachet.carp.analytics.infrastructure.serialization

import dk.cachet.carp.analytics.application.data.ArtifactReference
import dk.cachet.carp.analytics.application.data.DspDataTypes
import dk.cachet.carp.analytics.application.data.GenericArtifact
import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.serialization.CLASS_DISCRIMINATOR
import dk.cachet.carp.common.infrastructure.serialization.CustomData
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull


/**
 * Tests for [DSP_DATA_SERIAL_MODULE].
 */
class DspDataSerializationTest
{
    private val json: Json = createDefaultJSON( DSP_DATA_SERIAL_MODULE )
    private val dataSerializer = PolymorphicSerializer( Data::class )


    @ExperimentalSerializationApi
    @Test
    fun serializer_registered_for_each_dsp_data_type()
    {
        DspDataTypes.forEach {
            assertNotNull(
                DSP_DATA_SERIAL_MODULE.getPolymorphic( Data::class, it.key.toString() ),
                "No serializer is registered for \"${it.key}\"."
            )
        }
    }

    @Test
    fun can_round_trip_an_artifact_as_data()
    {
        val artifact = GenericArtifact( ApplicationData( """{"hr_mean":72.4}""" ) )

        val serialized = json.encodeToString( dataSerializer, artifact )
        assertEquals( artifact, json.decodeFromString( dataSerializer, serialized ) )
    }

    @Test
    fun artifact_is_typed_with_its_dsp_data_type()
    {
        val serialized = json.encodeToString( dataSerializer, GenericArtifact( reference = reference() ) )

        val type = json.parseToJsonElement( serialized ).jsonObject[ CLASS_DISCRIMINATOR ]?.jsonPrimitive?.content
        assertEquals( DspDataTypes.GENERIC_ARTIFACT.type.toString(), type )
    }

    @Test
    fun unknown_artifact_round_trips_through_a_core_only_serializer()
    {
        val coreOnly = createDefaultJSON()
        val serialized = json.encodeToString( dataSerializer, GenericArtifact( reference = reference() ) )

        // A consumer which doesn't know DSP types keeps the artefact as its raw JSON, ...
        val unknown = coreOnly.decodeFromString( dataSerializer, serialized )
        assertIs<CustomData>( unknown )
        assertEquals( DspDataTypes.GENERIC_ARTIFACT.type.toString(), unknown.className )

        // ... and hands it back unchanged.
        assertEquals( serialized, coreOnly.encodeToString( dataSerializer, unknown ) )
    }


    private fun reference() = ArtifactReference(
        "carp://artefacts/run-1/step-1/features.parquet",
        "application/vnd.apache.parquet",
        1024,
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    )
}
