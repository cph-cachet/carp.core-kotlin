package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.infrastructure.services.ServiceInvoker
import dk.cachet.carp.studies.application.StudyService
import kotlin.test.*


/**
 * Tests for [StudyServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class StudyServiceRequestsReflectionTest
{
    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun request_object_for_each_request_available()
    {
        val serviceFunctions = StudyService::class.members
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = StudyServiceRequestsTest.requests.map {
            val serviceInvoker = it as ServiceInvoker<StudyService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }
}
