package dk.cachet.carp.rpc

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import kotlin.test.*


class JsonSchemasTest
{
    @Test
    fun schema_validation_for_generated_examples_succeeds()
    {
        val mapper = ObjectMapper()

        for ( (appService, requests) in exampleApplicationServiceRequests )
        {
            // Find schema file, or skip in case no schema is defined yet.
            val requestSchemaFile = appService.requestSchemaFile
            if ( !requestSchemaFile.exists() ) break

            val requestSchema = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V201909 )
                .getSchema( requestSchemaFile.readText() )
            requests.forEach {
                val requestJson = mapper.readTree( it.requestObject.json )
                val errors = requestSchema.validate( requestJson )
                check( errors.isEmpty() )
                    { "JSON schema \"$requestSchemaFile\" does not match generated JSON example of \"${it.requestObject.klass}\": $errors" }
            }
        }
    }
}
