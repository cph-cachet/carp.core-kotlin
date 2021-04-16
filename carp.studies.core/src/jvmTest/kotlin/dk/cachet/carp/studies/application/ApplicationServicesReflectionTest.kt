package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.test.services.verifyNoDomainTypesUsedIn
import org.reflections.Reflections
import kotlin.test.*


class ApplicationServicesReflectionTest
{
    @Test
    fun no_domain_objects_used_in_application_service_interfaces()
    {
        // Find all application services and integration events in this subsystem.
        val namespace = this::class.java.`package`.name
        val reflections = Reflections( namespace )
        val typesToCheck = reflections.getSubTypesOf( ApplicationService::class.java )
            .plus( reflections.getSubTypesOf( IntegrationEvent::class.java ) )

        verifyNoDomainTypesUsedIn( typesToCheck )
    }
}
