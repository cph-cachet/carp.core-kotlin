package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.*
import dk.cachet.carp.common.ddd.ServiceInvoker
import dk.cachet.carp.studies.application.*
import dk.cachet.carp.studies.domain.users.Username
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


/**
 * Tests for [UserServiceRequest]'s which rely on reflection, and for now can only be executed on the JVM platform.
 */
class UserServiceRequestsReflectionTest
{
    @Suppress( "UNCHECKED_CAST" )
    @Test
    fun request_object_for_each_request_available()
    {
        val serviceFunctions = UserService::class.members
            .filterNot { it.name == "equals" || it.name == "hashCode" || it.name == "toString" }
        val testedRequests = UserServiceRequestsTest.requests.map {
            val serviceInvoker = it as ServiceInvoker<UserService, *>
            serviceInvoker.function
        }

        assertTrue( testedRequests.containsAll( serviceFunctions ) )
    }
}