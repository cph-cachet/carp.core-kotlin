package dk.cachet.carp.protocols.domain.serialization

import dk.cachet.carp.protocols.domain.tasks.Measure
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.serializer


/**
 * A serializer to serialize instances of [List] which contain [Measure] elements.
 * TODO: Defining an explicit serializer for these is a workaround.
 *       This can be removed once the following kotlinx.serialization bug is fixed:
 *       https://github.com/Kotlin/kotlinx.serialization/issues/153
 */
object MeasuresSerializer
    : KSerializer<List<Measure>> by ArrayListSerializer( Measure::class.serializer() )