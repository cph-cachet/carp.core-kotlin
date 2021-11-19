package dk.cachet.carp.protocols.domain

import dk.cachet.carp.protocols.domain.deployment.DeploymentIssue
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.test.findConcreteTypes
import kotlin.reflect.KClass
import kotlin.reflect.jvm.isAccessible
import kotlin.test.*


class StudyProtocolReflectionTest
{
    @Test
    fun all_deployment_issues_are_registered()
    {
        val protocol = createEmptyProtocol()

        // Get all registered deployment issues.
        val member =
            StudyProtocol::class.members.first { it.name == "possibleDeploymentIssues" }
        member.isAccessible = true
        @Suppress( "UNCHECKED_CAST" )
        val registeredDeploymentIssues = member.call( protocol ) as? List<DeploymentIssue>
        assertNotNull( registeredDeploymentIssues )
        val registeredDeploymentIssueTypes = registeredDeploymentIssues.map { it::class }

        val definedDeploymentIssues: List<KClass<out DeploymentIssue>> = findConcreteTypes()

        // For each defined deployment issue, verify whether it is registered in `StudyProtocol`.
        definedDeploymentIssues.forEach {
            assertTrue(
                it in registeredDeploymentIssueTypes,
                "`$it` is not registered as a possible deployment issue in `StudyProtocol`."
            )
        }
    }
}
