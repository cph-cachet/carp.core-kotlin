package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.services.ApplicationService
import kotlinx.serialization.Serializable
import org.reflections.Reflections
import java.io.File


private const val NAMESPACE = "dk.cachet.carp"


fun main( args: Array<String> )
{
    // Create example requests for all request objects and responses of application service methods.
    val reflections = Reflections( NAMESPACE )
    val serializableObjects = reflections.getTypesAnnotatedWith( Serializable::class.java )
    val exampleRequests = reflections
        .getSubTypesOf( ApplicationService::class.java )
        .filter { it.isInterface }
        .associateWith { appService ->
            val requestObjectName = "${appService.simpleName}Request"
            val requestObject = serializableObjects.single { it.simpleName == requestObjectName }
            requireNotNull( requestObject )
                { "Could not find request object for ${appService.name}. Searched for: $requestObjectName" }

            generateExampleRequests( appService, requestObject )
        }

    // Output example requests to "build/rpc".
    val outputFolder = File( "build/rpc" )
    outputFolder.mkdirs()
    exampleRequests.forEach { (appService, examples) ->
        val subsystem = Regex( Regex.escape( NAMESPACE ) + "\\.([^.]+)" )
            .find( appService.`package`.name )
            ?.groupValues?.lastOrNull()
        requireNotNull( subsystem ) { "${appService.name} is residing in an unexpected namespace." }

        val subsystemFolder = File( outputFolder, subsystem )
        val serviceFolder = File( subsystemFolder, appService.simpleName )
        serviceFolder.mkdirs()

        examples.forEach {
            val requestFileName = "example-" + it.method.name + "-request.json"
            File( serviceFolder, requestFileName ).writeText( it.requestObject.json )
            val responseFileName = "example-" + it.method.name + "-response.json"
            File( serviceFolder, responseFileName ).writeText( it.response.json )
        }
    }
}
