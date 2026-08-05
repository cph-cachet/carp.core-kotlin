package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ExecutorState(
    val executionId: UUID,
    val status: ExecutionStatus,
    val startedAt: Instant,
    val completedAt: Instant? = null,
    val workflowId: UUID,
    val studyId: UUID
)
