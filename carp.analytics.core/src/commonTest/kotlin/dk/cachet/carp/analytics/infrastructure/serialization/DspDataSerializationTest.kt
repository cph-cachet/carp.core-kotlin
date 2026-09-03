package dk.cachet.carp.analytics.infrastructure.serialization

import dk.cachet.carp.analytics.application.data.ArtifactReference
import dk.cachet.carp.analytics.application.data.Classification
import dk.cachet.carp.analytics.application.data.ComputedFeature
import dk.cachet.carp.analytics.application.data.DocumentArtifact
import dk.cachet.carp.analytics.application.data.DspDataTypes
import dk.cachet.carp.analytics.application.data.FeatureTable
import dk.cachet.carp.analytics.application.data.GenericArtifact
import dk.cachet.carp.analytics.application.data.ImageArtifact
import dk.cachet.carp.analytics.application.data.PlotArtifact
import dk.cachet.carp.analytics.application.data.SummaryStatistic
import dk.cachet.carp.analytics.application.data.TriggerActivation
import dk.cachet.carp.analytics.application.data.WorkflowArtifact
import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.serialization.CLASS_DISCRIMINATOR
import dk.cachet.carp.common.infrastructure.serialization.CustomData
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant


/**
 * Tests for [DSP_DATA_SERIAL_MODULE].
 */
class DspDataSerializationTest
{
    private val json: Json = createDefaultJSON( DSP_DATA_SERIAL_MODULE )
    private val dataSerializer = PolymorphicSerializer( Data::class )

    /**
     * One instance of every [WorkflowArtifact] leaf, paired with the type it should be tagged with.
     */
    private val artifacts: List<Pair<WorkflowArtifact, String>> = listOf(
        GenericArtifact( ApplicationData( """{"hr_mean":72.4}""" ) ) to
            DspDataTypes.GENERIC_ARTIFACT.type.toString(),
        ComputedFeature( "hr_mean", 72.4, "bpm" ) to
            DspDataTypes.COMPUTED_FEATURE.type.toString(),
        SummaryStatistic( "mean", 72.4, 60, "bpm" ) to
            DspDataTypes.SUMMARY_STATISTIC.type.toString(),
        Classification( "sleep", 0.92, "sleepnet/v2" ) to
            DspDataTypes.CLASSIFICATION.type.toString(),
        FeatureTable( reference(), listOf( "hr_mean", "hr_std" ), 1440 ) to
            DspDataTypes.FEATURE_TABLE.type.toString(),
        ImageArtifact( reference(), 800, 600 ) to
            DspDataTypes.IMAGE_ARTIFACT.type.toString(),
        PlotArtifact( reference(), ApplicationData( """{"mark":"line"}""" ), "vega-lite/v5", "Heart rate" ) to
            DspDataTypes.PLOT_ARTIFACT.type.toString(),
        DocumentArtifact( reference(), "Weekly report") to
            DspDataTypes.DOCUMENT_ARTIFACT.type.toString(),
        TriggerActivation( 1, UUID.randomUUID(), Instant.fromEpochSeconds( 1_755_000_000 ) ) to
            DspDataTypes.TRIGGER_ACTIVATION.type.toString()
    )


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

    /**
     * The completeness guard, in the direction the registry cannot check itself.
     *
     * [serializer_registered_for_each_dsp_data_type] goes type string -> class: it catches a
     * declared type with nothing to serialize it. This goes the other way, class -> type string,
     * and catches the leaf that was written and registered but never given a [DspDataTypes] entry.
     * That leaf would serialize happily and then be rejected at runtime by `appendToDataStreams`,
     * because the study protocol's `dsp` task can only declare what this registry lists.
     *
     * Derived from [DSP_DATA_SERIAL_MODULE] rather than a list maintained here, so the check cannot
     * drift from what is actually registered.
     */
    @ExperimentalSerializationApi
    @Test
    fun every_registered_artifact_has_a_dsp_data_type()
    {
        val declared = DspDataTypes.map { it.key.toString() }.toSet()
        val registered = registeredTypeNames()

        // Without this the loop below would pass on an empty set, and the guard
        // would be silently useless.
        assertTrue( registered.isNotEmpty(), "No artefact serializers were found to check." )

        registered.forEach { serialName ->
            assertTrue(
                serialName in declared,
                "\"$serialName\" is registered for serialization but has no DspDataTypes entry, " +
                    "so no study protocol can declare it and its measurements will be rejected."
            )
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun every_leaf_has_a_sample_in_this_test()
    {
        assertEquals( registeredTypeNames().size, artifacts.size, "A leaf has no sample artefact to test with." )
        assertEquals( DspDataTypes.size, artifacts.size, "A data type has no sample artefact to test with." )
    }

    @Test
    fun can_round_trip_every_leaf_as_data()
    {
        artifacts.forEach { ( artifact, _ ) ->
            val serialized = json.encodeToString( dataSerializer, artifact )
            assertEquals( artifact, json.decodeFromString( dataSerializer, serialized ), "Failed on $artifact." )
        }
    }

    @Test
    fun every_leaf_is_tagged_with_its_dsp_data_type()
    {
        artifacts.forEach { ( artifact, expectedType ) ->
            val serialized = json.encodeToString( dataSerializer, artifact )
            val type = json.parseToJsonElement( serialized ).jsonObject[ CLASS_DISCRIMINATOR ]?.jsonPrimitive?.content
            assertEquals( expectedType, type )
        }
    }

    @Test
    fun unknown_artifacts_round_trip_through_a_core_only_serializer()
    {
        val coreOnly = createDefaultJSON()

        artifacts.forEach { ( artifact, expectedType ) ->
            val serialized = json.encodeToString( dataSerializer, artifact )

            // A consumer which doesn't know DSP types keeps the artefact as its raw JSON, ...
            val unknown = coreOnly.decodeFromString( dataSerializer, serialized )
            assertIs<CustomData>( unknown )
            assertEquals( expectedType, unknown.className )

            // ... and hands it back unchanged.
            assertEquals( serialized, coreOnly.encodeToString( dataSerializer, unknown ) )
        }
    }


    /**
     * The serial name of every subclass registered in [DSP_DATA_SERIAL_MODULE] - which, because the
     * `DataType` is derived from the serial name, is the type string each leaf travels under.
     *
     * `dumpTo` is how a module is enumerated without reflection, so this works on every target.
     */
    @ExperimentalSerializationApi
    private fun registeredTypeNames(): Set<String>
    {
        val names = mutableSetOf<String>()

        DSP_DATA_SERIAL_MODULE.dumpTo(
            object : SerializersModuleCollector
            {
                override fun <T : Any> contextual(
                    kClass: KClass<T>,
                    provider: (List<KSerializer<*>>) -> KSerializer<*>
                ) = Unit

                override fun <Base : Any, Sub : Base> polymorphic(
                    baseClass: KClass<Base>,
                    actualClass: KClass<Sub>,
                    actualSerializer: KSerializer<Sub>
                )
                {
                    names += actualSerializer.descriptor.serialName
                }

                override fun <Base : Any> polymorphicDefaultSerializer(
                    baseClass: KClass<Base>,
                    defaultSerializerProvider: (Base) -> SerializationStrategy<Base>?
                ) = Unit

                override fun <Base : Any> polymorphicDefaultDeserializer(
                    baseClass: KClass<Base>,
                    defaultDeserializerProvider: (String?) -> DeserializationStrategy<Base>?
                ) = Unit
            }
        )

        return names
    }


    private fun reference() = ArtifactReference(
        "carp://artefacts/run-1/step-1/features.parquet",
        "application/vnd.apache.parquet",
        1024,
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    )
}
