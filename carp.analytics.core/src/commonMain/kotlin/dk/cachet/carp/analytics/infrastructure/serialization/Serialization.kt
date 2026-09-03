package dk.cachet.carp.analytics.infrastructure.serialization

import dk.cachet.carp.analytics.application.data.Classification
import dk.cachet.carp.analytics.application.data.ComputedFeature
import dk.cachet.carp.analytics.application.data.DocumentArtifact
import dk.cachet.carp.analytics.application.data.FeatureTable
import dk.cachet.carp.analytics.application.data.GenericArtifact
import dk.cachet.carp.analytics.application.data.ImageArtifact
import dk.cachet.carp.analytics.application.data.PlotArtifact
import dk.cachet.carp.analytics.application.data.SummaryStatistic
import dk.cachet.carp.analytics.application.data.TriggerActivation
import dk.cachet.carp.analytics.application.data.WorkflowArtifact
import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


/**
 * The [WorkflowArtifact] types which a [Json] serializer needs to know about.
 *
 */
val DSP_DATA_SERIAL_MODULE: SerializersModule = SerializersModule {
    polymorphic( Data::class )
    {
        subclass( GenericArtifact::class )

        // AnalyticResult.
        subclass( ComputedFeature::class )
        subclass( SummaryStatistic::class )
        subclass( Classification::class )
        subclass( FeatureTable::class )

        // MediaArtifact.
        subclass( ImageArtifact::class )
        subclass( PlotArtifact::class )
        subclass( DocumentArtifact::class )

        // ControlSignal.
        subclass( TriggerActivation::class )
    }
}
