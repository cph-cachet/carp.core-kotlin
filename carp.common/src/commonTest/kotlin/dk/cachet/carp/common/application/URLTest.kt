package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.tasks.WebTask
import dk.cachet.carp.common.application.tasks.WebTaskBuilder
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class URLTest
{

    @Test
    fun can_serialize_and_deserialize_URL_using_JSON()
    {
        val url = URL( "https://www.example.com" )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( URL.serializer(), url )
        val parsed = json.decodeFromString( URL.serializer(), serialized )

        assertEquals( url, parsed )
    }

    @Test
    fun cant_initialize_url()
    {
        // if string is empty
        assertFailsWith<IllegalArgumentException> { URL( "" ) }
        // with no scheme
        assertFailsWith<IllegalArgumentException> { URL ( "www.example.com" ) }
    }

    @Test
    fun can_initialize_url()
    {
        // with custom scheme
        URL( "app://welcome" )
        // with explicit port
        URL( "http://localhost:3000" )
        // with query parameters
        URL( "https://www.example.com?param1=value1&param2=value2" )
        // with redirect uri
        URL( "https://www.example.com/?url=https://www.example2.com" )
    }
}
