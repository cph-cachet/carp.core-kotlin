package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.services.ApplicationService
import kotlinx.serialization.Serializable
import org.reflections.Reflections


fun main( args: Array<String> )
{
    val reflections = Reflections( "dk.cachet.carp" )
    val serializableObjects = reflections.getTypesAnnotatedWith( Serializable::class.java )
    reflections
        .getSubTypesOf( ApplicationService::class.java )
        .filter { it.isInterface }
        .forEach { appService ->
            val requestObjectName = "${appService.simpleName}Request"
            val requestObject = serializableObjects.single { it.simpleName == requestObjectName }
            requireNotNull( requestObject )
                { "Could not find request object for ${appService.name}. Searched for: $requestObjectName" }

            generateExampleRequests( appService, requestObject )
        }
}
