package dk.cachet.carp.rpc

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import dk.cachet.carp.common.application.services.ApplicationService
import kotlinx.serialization.Serializable
import org.reflections.Reflections
import java.io.File


fun main( args: Array<String> )
{
    // Find all application services.
    val reflections = Reflections( ApplicationServiceInfo.NAMESPACE )
    val appServices = reflections
        .getSubTypesOf( ApplicationService::class.java )
        .filter { it.isInterface }
        .map { ApplicationServiceInfo( it ) }

    // Create example requests for all request objects and responses of application service methods.
    val serializableObjects = reflections.getTypesAnnotatedWith( Serializable::class.java )
    val exampleRequests = appServices
        .associateWith { appService ->
            val requestObjectName = appService.requestObjectName
            val requestObject = serializableObjects.single { it.simpleName == requestObjectName }
            checkNotNull( requestObject )
                { "Could not find request object for ${appService.klass.name}. Searched for: $requestObjectName" }

            generateExampleRequests( appService.klass, requestObject )
        }

    // Validate available JSON schemas using the example requests.
    val mapper = ObjectMapper()
    for ( appService in appServices )
    {
        val requests = exampleRequests[ appService ]!!
        val requestSchemaFile = File( appService.requestSchemaPath )
        if ( !requestSchemaFile.exists() ) break

        val requestSchema = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V201909 )
            .getSchema( requestSchemaFile.readText() )
        requests.forEach {
            val requestJson = mapper.readTree( it.requestObject.json )
            val errors = requestSchema.validate( requestJson )
            check( errors.isEmpty() )
                { "JSON schema \"$requestSchemaFile\" does not match generated JSON example of \"${it.requestObject.klass.name}\": $errors" }
        }
    }

    // Output example requests to "build/rpc".
    val outputFolder = File( "build/rpc" )
    outputFolder.mkdirs()
    exampleRequests.forEach { (appService, examples) ->
        val subsystemFolder = File( outputFolder, appService.subsystemName )
        val serviceFolder = File( subsystemFolder, appService.klass.simpleName )
        serviceFolder.mkdirs()

        examples.forEach {
            val requestFileName = it.method.name + "-request-example.json"
            File( serviceFolder, requestFileName ).writeText( it.requestObject.json )
            val responseFileName = it.method.name + "-response-example.json"
            File( serviceFolder, responseFileName ).writeText( it.response.json )
        }
    }
}
