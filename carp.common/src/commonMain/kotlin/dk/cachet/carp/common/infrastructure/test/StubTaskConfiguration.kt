package dk.cachet.carp.common.infrastructure.test

import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.tasks.TaskConfiguration
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


@Serializable
data class StubTaskConfiguration(
    @Required override val name: String = "Stub task",
    override val measures: List<Measure> = emptyList(),
    override val description: String? = null
) : TaskConfiguration<NoData>
