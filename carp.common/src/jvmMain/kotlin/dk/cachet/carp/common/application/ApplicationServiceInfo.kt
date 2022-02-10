package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.DependentServices
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.common.infrastructure.services.LoggedRequestSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.serializer
import java.net.URI
import kotlin.reflect.KClass


/**
 * Determines associated classes, names, and file locations of code artifacts
 * related to the application service identified by [serviceKlass].
 */
@OptIn( InternalSerializationApi::class )
@Suppress( "MagicNumber" )
class ApplicationServiceInfo( val serviceKlass: Class<out ApplicationService<*, *>> )
{
    companion object
    {
        private fun getEventSerializer(
            serviceKlass: Class<out ApplicationService<*, *>>
        ): SealedClassSerializer<IntegrationEvent<*>>
        {
            val eventKlassLookup = serviceKlass
                .declaredClasses.singleOrNull { it.simpleName == "Event" }
                ?.kotlin
            val eventKlass = checkNotNull( eventKlassLookup )
            {
                "Could not find event serializer for \"${serviceKlass.name}\". " +
                "Expected it to be defined as an inner class named \"Event\"."
            }

            // HACK: Seemingly a serializer can't be retrieved for sealed classes with no subtypes. A likely bug.
            //  This can be safely ignored, since there are no events to be serialized.
            if ( eventKlass.java.declaredClasses.isEmpty() )
            {
                return SealedClassSerializer(
                    IntegrationEvent::class.qualifiedName!!,
                    IntegrationEvent::class,
                    emptyArray(),
                    emptyArray()
                )
            }

            @Suppress( "UNCHECKED_CAST" )
            return eventKlass.serializer() as SealedClassSerializer<IntegrationEvent<*>>
        }
    }


    val serviceName: String = serviceKlass.simpleName
    val apiVersion: ApiVersion = checkNotNull( serviceKlass.getAnnotation( ApiVersion::class.java ) )
        { "Application service \"${serviceKlass.name}\" is missing an \"${ApiVersion::class.simpleName}\" annotation." }
    val dependentServices: List<Class<out ApplicationService<*, *>>> =
        serviceKlass.getAnnotation( DependentServices::class.java )
            ?.service?.map { it.java } ?: emptyList()

    val subsystemName: String
    val subsystemNamespace: String

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

        // Get event serializer capable of serializing published events, as well as events the service subscribes to.
        // TODO: Merging sealed serializers can potentially be made multiplatform and moved to `carp.common`.
        val eventSerializers = dependentServices.plus( serviceKlass ).map { getEventSerializer( it ) }
        // HACK: There is no public accessor to get the subclass serializers.
        //  https://github.com/Kotlin/kotlinx.serialization/issues/1865
        val subclassesField = SealedClassSerializer::class.java
            .getDeclaredField( "class2Serializer" )
            .apply { isAccessible = true }
        val allSubclassSerializers = eventSerializers.flatMap {
            @Suppress( "UNCHECKED_CAST" )
            val serializers = subclassesField.get( it )
                as Map<KClass<IntegrationEvent<*>>, KSerializer<IntegrationEvent<*>>>
            serializers.toList()
        }.toMap()
        eventSerializer = SealedClassSerializer(
            IntegrationEvent::class.qualifiedName!!,
            IntegrationEvent::class,
            allSubclassSerializers.keys.toTypedArray(),
            allSubclassSerializers.values.toTypedArray()
        )

        loggedRequestSerializer = LoggedRequestSerializer( requestObjectSerializer, eventSerializer )

        requestSchemaUri = URI( "https://carp.cachet.dk/schemas/$subsystemName/$serviceName/$requestObjectName.json" )
    }
}
