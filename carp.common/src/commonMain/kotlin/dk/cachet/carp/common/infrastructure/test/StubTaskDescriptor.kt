package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.tasks.measures.Measure
import kotlinx.serialization.Serializable


@Serializable
data class StubTaskDescriptor(
    override val name: String = "Stub task",
    override val measures: List<Measure> = listOf()
) : TaskDescriptor
