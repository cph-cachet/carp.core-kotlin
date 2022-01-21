package dk.cachet.carp.rpc

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaException
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import java.net.URI
import kotlin.test.*


class JsonSchemasTest
{
    @Test
    fun schema_validation_for_generated_examples_succeeds()
    {
        val schemaFactory = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V201909 )
        val mapper = ObjectMapper()

        for ( (appService, requests) in exampleApplicationServiceRequests )
        {
            // Find request schema, or skip in case no schema is defined yet.
            val requestSchemaURI = URI( "file:${appService.requestSchemaPath}" )
            val requestSchema =
                try { schemaFactory.getSchema( requestSchemaURI ) }
                catch ( _: JsonSchemaException ) { break }

            @Suppress( "UnreachableCode" ) // Seemingly a bug in detekt 1.18.1
            requests.forEach {
                val requestJson = mapper.readTree( it.requestObject.json )
                val errors = requestSchema.validate( requestJson )
                check( errors.isEmpty() )
                    { "JSON schema \"$requestSchema\" doesn't match generated JSON example of \"${it.requestObject.klass}\": $errors" }
            }
        }
    }
}
