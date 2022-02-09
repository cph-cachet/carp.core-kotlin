package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.common.infrastructure.services.LoggedRequestSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer
import java.net.URI


/**
 * Determines associated classes, names, and file locations of code artifacts
 * related to the application service identified by [serviceKlass].
 */
@OptIn( InternalSerializationApi::class )
@Suppress( "MagicNumber" )
class ApplicationServiceInfo( val serviceKlass: Class<out ApplicationService<*, *>> )
{
    val subsystemName: String
    val subsystemNamespace: String
    val serviceName: String = serviceKlass.simpleName

    val requestObjectName: String = "${serviceName}Request"
    val requestObjectClass: Class<*>

    val requestObjectSerializer: KSerializer<out ApplicationServiceRequest<*, *>>
    val eventSerializer: KSerializer<IntegrationEvent<*>>
    val loggedRequestSerializer: LoggedRequestSerializer<*>

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

        // Get request object serializer.
        @Suppress( "UNCHECKED_CAST" )
        val requestObjectSerializerLookup = requestObjectClass
            .declaredClasses.single { it.simpleName == "Serializer" }
            .getField( "INSTANCE" ).get( null ) as? KSerializer<ApplicationServiceRequest<*, *>>
        requestObjectSerializer = checkNotNull( requestObjectSerializerLookup )
            {
                "Could not find request object serializer for \"${requestObjectName}\". " +
                "Expected it to be defined as an inner object named \"Serializer\"."
            }

        // Get event serializer.
        val eventKlassLookup = serviceKlass
            .declaredClasses.singleOrNull { it.simpleName == "Event" }
            ?.kotlin
        val eventKlass = checkNotNull( eventKlassLookup )
            {
                "Could not find event serializer for \"${serviceKlass.name}\". " +
                "Expected it to be defined as an inner class named \"Event\"."
            }
        @Suppress( "UNCHECKED_CAST" )
        eventSerializer =
            try { eventKlass.serializer() as KSerializer<IntegrationEvent<*>> }
            // HACK: Seemingly a serializer can't be retrieved for sealed classes with no subtypes. A likely bug.
            //  This can be safely ignored, since there are no events to be serialized.
            catch ( _: SerializationException ) { NotSerializable as KSerializer<IntegrationEvent<*>> }

        loggedRequestSerializer = LoggedRequestSerializer( requestObjectSerializer, eventSerializer )

        requestSchemaUri = URI( "https://carp.cachet.dk/schemas/$subsystemName/$serviceName/$requestObjectName.json" )
    }
}
