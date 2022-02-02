package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.services.ApplicationService
import java.net.URI


/**
 * Determines associated classes, names, and file locations of code artifacts
 * related to the application service identified by [serviceKlass].
 */
@Suppress( "MagicNumber" )
class ApplicationServiceInfo( val serviceKlass: Class<out ApplicationService<*, *>> )
{
    val subsystemName: String
    val subsystemNamespace: String
    val serviceName: String = serviceKlass.simpleName

    val requestObjectName: String = "${serviceName}Request"
    val requestObjectClass: Class<*>

    val requestSchemaUri: URI

    init
    {
        // Get subsystem information.
        val unexpectedNamespace = IllegalStateException(
            "Application services should be in a namespace matching the following pattern: " +
            "<organization-namespace>.<subsystem>.application.<service-name>"
        )
        val splitNamespace = serviceKlass.name.split( '.' )
        val (subsystem, application, service) =
            try { splitNamespace.takeLast( 3 ) }
            catch ( _: IndexOutOfBoundsException ) { throw unexpectedNamespace }
        if ( application != "application" ) throw unexpectedNamespace
        subsystemName = subsystem
        subsystemNamespace = splitNamespace.dropLast( 2 ).joinToString( "." )

        // Get request object.
        val requestObjectFullName = "$subsystemNamespace.infrastructure.$requestObjectName"
        val requestObject: Class<*>? =
            try { Class.forName( requestObjectFullName ) }
            catch ( _: ClassNotFoundException ) { null }
        requestObjectClass = checkNotNull( requestObject )
            { "Could not find request object for \"${serviceKlass.name}\". Expected at: $requestObjectFullName" }

        requestSchemaUri = URI( "https://carp.cachet.dk/schemas/$subsystemName/$serviceName/$requestObjectName.json" )
    }
}
