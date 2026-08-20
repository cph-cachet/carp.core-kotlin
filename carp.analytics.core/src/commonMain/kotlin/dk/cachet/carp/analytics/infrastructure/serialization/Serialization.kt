package dk.cachet.carp.analytics.infrastructure.serialization

import dk.cachet.carp.analytics.application.data.GenericArtifact
import dk.cachet.carp.analytics.application.data.WorkflowArtifact
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


/**
 * The [WorkflowArtifact] types which a [Json] serializer needs to know about.
 *
 * Compose it into core's: `createDefaultJSON( DSP_DATA_SERIAL_MODULE )`. See [createDefaultJSON].
 * Without it artefacts still travel, but arrive as `CustomData` holding the raw JSON.
 */
val DSP_DATA_SERIAL_MODULE: SerializersModule = SerializersModule {
    polymorphic( Data::class )
    {
        subclass( GenericArtifact::class )
    }
}
