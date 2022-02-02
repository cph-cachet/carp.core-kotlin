package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.ApplicationServiceInfo
import dk.cachet.carp.common.application.services.ApplicationService
import org.apache.commons.io.FileUtils
import org.reflections.Reflections
import java.io.File


/**
 * Application service info for all CARP application services which are loaded on the current classpath.
 */
val applicationServices = Reflections( "dk.cachet.carp" )
    .getSubTypesOf( ApplicationService::class.java )
    .filter { it.isInterface }
    .map { ApplicationServiceInfo( it ) }


fun main( args: Array<String> )
{
    // Create example requests for all request objects and responses of application service methods.
    val exampleRequests = applicationServices
        .associateWith { generateExampleRequests( it.serviceKlass, it.requestObjectClass ) }

    // Output example requests.
    val jsonExamplesFolder = File( "build/rpc-examples/" )
    FileUtils.deleteDirectory( jsonExamplesFolder )
    exampleRequests.forEach { (appService, examples) ->
        val serviceFolder =
            File( jsonExamplesFolder, "${appService.subsystemName}/${appService.serviceName}" )
        serviceFolder.mkdirs()

        examples.forEach {
            val requestFileName = it.method.name + ".json"
            File( serviceFolder, requestFileName ).writeText( it.requestObject.json )
            val responseFileName = it.method.name + "-response.json"
            File( serviceFolder, responseFileName ).writeText( it.response.json )
        }
    }
}
