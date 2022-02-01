package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceLog
import java.io.File
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInfo
import kotlin.test.*


val TEST_REQUESTS_FOLDER = File( "build/test-requests/" )


/**
 * Outputs all logged requests of a [loggedService] while running unit tests to the build folder.
 *
 * Extend from this base class along with a test interface for which to log requests.
 */
open class OutputTestRequests<TService : ApplicationService<TService, *>>(
    loggedService: ApplicationServiceLog<TService>
)
{
    @Suppress( "UNCHECKED_CAST" )
    val loggedService: TService = loggedService as TService

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
        val testName = info.testMethod.get().name

        // Output logged requests.
        val output = File( testFolder, "$testName.json" )
        output.createNewFile() // TODO: Output requests.
    }
}
