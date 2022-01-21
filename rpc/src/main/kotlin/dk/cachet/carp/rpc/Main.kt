package dk.cachet.carp.rpc

import dk.cachet.carp.rpc.ApplicationServiceInfo.Companion.JSON_EXAMPLES_FOLDER
import org.apache.commons.io.FileUtils
import java.io.File


fun main( args: Array<String> )
{
    // Create example requests for all request objects and responses of application service methods.
    val exampleRequests = ApplicationServiceInfo.findInNamespace( "dk.cachet.carp" )
        .associateWith { generateExampleRequests( it.serviceKlass, it.requestObjectKlass ) }

    // Output example requests.
    FileUtils.deleteDirectory( JSON_EXAMPLES_FOLDER )
    exampleRequests.forEach { (appService, examples) ->
        val serviceFolder = appService.jsonExamplesFolder
        serviceFolder.mkdirs()

        examples.forEach {
            val requestFileName = it.method.name + "-example.json"
            File( serviceFolder, requestFileName ).writeText( it.requestObject.json )
            val responseFileName = it.method.name + "-response-example.json"
            File( serviceFolder, responseFileName ).writeText( it.response.json )
        }
    }
}
