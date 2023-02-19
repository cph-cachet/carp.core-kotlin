package dk.cachet.carp.common.test.infrastructure.versioning

import dk.cachet.carp.common.application.ApplicationServiceInfo
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLogger
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInfo
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.*


val TEST_REQUESTS_FOLDER = File( "build/test-requests/" )


/**
 * Outputs all logged requests of a [serviceLogger] while running unit tests to the build folder.
 *
 * Extend from this base class along with a test interface for which to log requests,
 * and implement `createService` by returning a logging service proxy and setting [serviceLogger].
 */
open class OutputTestRequests<TService : ApplicationService<TService, *>>( applicationServiceKlass: KClass<TService> )
{
    @Suppress( "UNCHECKED_CAST" )
    protected var serviceLogger: ApplicationServiceLogger<TService, *>? = null
    private val applicationServiceInfo = ApplicationServiceInfo.of( applicationServiceKlass.java )

    companion object
    {
        private lateinit var testFolder: File

        @JvmStatic
        @BeforeAll
        fun setup( info: TestInfo )
        {
            val testInterface = checkNotNull( info.testClass.get().interfaces.singleOrNull() )
                { "Outputting test results expects a single test interface to be applied to the extending class." }
            testFolder = File( TEST_REQUESTS_FOLDER, testInterface.simpleName )

            // Clean previous output.
            FileUtils.deleteDirectory( testFolder )
            testFolder.mkdirs()
        }
    }

    @AfterTest
    fun outputLoggedRequests( info: TestInfo )
    {
        val service = checkNotNull( serviceLogger )
            { "`loggedService` needs to be set in `createService`." }

        // Serialize requests as json.
        val requests = service.loggedRequests
        val json = Json( createTestJSON() ) { prettyPrint = true }
        val serializer = ListSerializer( applicationServiceInfo.loggedRequestSerializer )
        val loggedRequestsJson = json.encodeToString( serializer, requests )

        // Output to file.
        val testName = info.testMethod.get().name
        val output = File( testFolder, "$testName.json" )
        output.writeText( loggedRequestsJson )
    }
}
