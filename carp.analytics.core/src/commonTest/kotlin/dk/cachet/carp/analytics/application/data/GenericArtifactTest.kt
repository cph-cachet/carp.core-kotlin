package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


/**
 * Tests for [GenericArtifact].
 */
class GenericArtifactTest
{
    private val json: Json = createDefaultJSON()


    @Test
    fun can_round_trip_an_inline_payload()
    {
        assertRoundTrips(
            GenericArtifact( ApplicationData( """{"hr_mean":72.4}""" ), schemaRef = "https://example.org/hr.json" )
        )
    }

    @Test
    fun can_round_trip_a_reference()
    {
        assertRoundTrips( GenericArtifact( reference = reference() ) )
    }

    @Test
    fun cant_initialize_without_a_payload_or_a_reference()
    {
        assertFailsWith<IllegalArgumentException> { GenericArtifact() }
    }

    @Test
    fun serial_name_matches_the_registered_data_type()
    {
        assertEquals(
            DspDataTypes.GENERIC_ARTIFACT.type.toString(),
            GenericArtifact.serializer().descriptor.serialName
        )
    }


    private fun assertRoundTrips( artifact: GenericArtifact )
    {
        val serialized = json.encodeToString( GenericArtifact.serializer(), artifact )
        assertEquals( artifact, json.decodeFromString( GenericArtifact.serializer(), serialized ) )
    }

    private fun reference() = ArtifactReference(
        "carp://artefacts/run-1/step-1/features.parquet",
        "application/vnd.apache.parquet",
        1024,
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    )
}
