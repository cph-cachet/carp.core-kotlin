package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.serialization.MeasuresSerializer
import kotlinx.serialization.Serializable


@Serializable
data class StubTaskDescriptor(
    override val name: String = "Stub task",
    @Serializable( with = MeasuresSerializer::class )
    override val measures: List<Measure> = listOf() ) : TaskDescriptor()