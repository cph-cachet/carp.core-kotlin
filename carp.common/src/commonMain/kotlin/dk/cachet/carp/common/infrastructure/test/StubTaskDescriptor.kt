package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.tasks.Measure
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


@Serializable
data class StubTaskDescriptor(
    @Required override val name: String = "Stub task",
    override val measures: List<Measure> = emptyList(),
    override val description: String? = null
) : TaskDescriptor<NoData>
