package dk.cachet.carp.common.application

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.DependentServices
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.common.infrastructure.services.LoggedRequestSerializer
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import kotlinx.serialization.*
import java.net.URI
import kotlin.reflect.KClass


typealias ServiceClass = Class<out ApplicationService<*, *>>

/**
 * Determines associated classes, names, and file locations of code artifacts
 * related to the application service identified by [serviceKlass].
 */
@OptIn( InternalSerializationApi::class )
@Suppress( "MagicNumber" )
class ApplicationServiceInfo private constructor( val serviceKlass: ServiceClass )
{
    companion object
    {
        private val applicationServiceInfo: MutableMap<ServiceClass, ApplicationServiceInfo> = mutableMapOf()

        /**
         * Retrieve [ApplicationServiceInfo] for [serviceKlass].
         */
        fun of( serviceKlass: ServiceClass ): ApplicationServiceInfo =
            applicationServiceInfo.getOrPut( serviceKlass ) { ApplicationServiceInfo( serviceKlass ) }

        /**
         * Returns the [IntegrationEvent] class for [serviceKlass].
         *
         * @throws IllegalStateException when the `Event` class is not present or not defined at the expected location.
         */
        fun getEventClass( serviceKlass: ServiceClass ): Class<IntegrationEvent<*>>
        {
            val eventKlassLookup = serviceKlass
                .declaredClasses.singleOrNull { it.simpleName == "Event" }

            @Suppress( "UNCHECKED_CAST" )
            return checkNotNull( eventKlassLookup as? Class<IntegrationEvent<*>> )
                {
                    "Could not find event serializer for \"${serviceKlass.name}\". " +
                    "Expected it to be defined as an inner class named \"Event\"."
                }
        }

        private fun <T> getInnerObject( klass: Class<*>, name: String ): T?
        {
            val retrievedObject = klass
                .declaredClasses.singleOrNull { it.simpleName == name }
                ?.declaredFields?.singleOrNull {
                    // May be prepended with $ signs.
                    it.name.endsWith( "INSTANCE" )
                }
                ?.let {
                    it.isAccessible = true
                    it.get( null )
                }

            @Suppress( "UNCHECKED_CAST" )
            return retrievedObject as? T
        }

        private fun getEventSerializer( serviceKlass: ServiceClass ): SealedClassSerializer<IntegrationEvent<*>>
        {
            val eventClass = getEventClass( serviceKlass )

            // HACK: Seemingly a serializer can't be retrieved for sealed classes with no subtypes. A likely bug.
            //  This can be safely ignored, since there are no events to be serialized.
            if ( eventClass.declaredClasses.isEmpty() )
            {
                return SealedClassSerializer(
                    IntegrationEvent::class.qualifiedName!!,
                    IntegrationEvent::class,
                    emptyArray(),
                    emptyArray()
                )
            }

            @Suppress( "UNCHECKED_CAST" )
            return eventClass.kotlin.serializer() as SealedClassSerializer<IntegrationEvent<*>>
        }
    }


    val serviceName: String = serviceKlass.simpleName
    val apiVersion: ApiVersion
    val dependentServices: List<ServiceClass> = serviceKlass.getAnnotation( DependentServices::class.java )
        ?.service?.map { it.java }.orEmpty()

    val subsystemName: String
    val subsystemNamespace: String

    val requestObjectName: String = "${serviceName}Request"
    val requestObjectClass: Class<*>
    val eventClass: Class<IntegrationEvent<*>> = getEventClass( serviceKlass )

    val requestObjectSerializer: KSerializer<out ApplicationServiceRequest<*, *>>
    val eventSerializer: KSerializer<IntegrationEvent<*>>
    val loggedRequestSerializer: KSerializer<LoggedRequest<*>>
    val apiMigrator: ApplicationServiceApiMigrator<*, *>

    val requestSchemaUri: URI

    init
    {
        // Get API version.
        val apiVersionField = "API_VERSION"
        val apiVersionLookup: ApiVersion? = getInnerObject<Any>( serviceKlass, "Companion" )
            ?.let { inner ->
                val versionField = inner::class.java.declaredFields.firstOrNull { it.name == apiVersionField }
                versionField?.let {
                    it.isAccessible = true
                    it.get( inner ) as? ApiVersion
                }
            }
        apiVersion = checkNotNull( apiVersionLookup )
            {
                "Could not find `ApiVersion` for \"${serviceKlass.name}\". " +
                "Expected it to be defined as a field named `$apiVersionField` on an unnamed companion object."
            }

        // Get subsystem information.
        val unexpectedNamespace = IllegalStateException(
            "Application services should be in a namespace matching the following pattern: " +
            "<organization-namespace>.<subsystem>.application.<service-name>"
        )
        val splitNamespace = serviceKlass.name.split( '.' )
        val (subsystem, application, service) =
            try { splitNamespace.takeLast( 3 ) }
            catch ( _: IndexOutOfBoundsException ) { throw unexpectedNamespace }
        assert( serviceName == service )
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
        requestObjectSerializer = checkNotNull( getInnerObject( requestObjectClass, "Serializer" ) )
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

        @Suppress( "UNCHECKED_CAST" )
        loggedRequestSerializer = LoggedRequestSerializer(
            requestObjectSerializer as KSerializer<out ApplicationServiceRequest<Nothing, *>>,
            eventSerializer as KSerializer<out IntegrationEvent<Nothing>>
        )

        // Get application service API migrator.
        val apiMigratorName = "${serviceName}ApiMigrator"
        val apiMigratorFullName = "$subsystemNamespace.infrastructure.versioning.${apiMigratorName}Kt"
        val apiMigratorClass: Class<*>? =
            try { Class.forName( apiMigratorFullName ) }
            catch ( _: ClassNotFoundException ) { null }
        val apiMigratorLookup = apiMigratorClass?.declaredMethods
            ?.firstOrNull { it.name == "get$apiMigratorName" }?.invoke( null ) as? ApplicationServiceApiMigrator<*, *>
        apiMigrator = checkNotNull( apiMigratorLookup )
            {
                "Could not find API migrator for \"${serviceKlass.name}\". " +
                "Expected it to be defined as a property named \"$apiMigratorName\" in \"$apiMigratorFullName\"."
            }

        requestSchemaUri = URI( "https://carp.cachet.dk/schemas/$subsystemName/$serviceName/$requestObjectName.json" )
    }


    /**
     * Returns info for the service which is responsible for publishing the event identified by [classDiscriminator],
     * but only in case the event is published by this or one of the [dependentServices]; null otherwise.
     *
     * @throws IllegalArgumentException if [classDiscriminator] does not end in
     *  "... <subsystem>.application.<service>.Event.<event-name>".
     */
    fun getEventPublisher( classDiscriminator: String ): ApplicationServiceInfo? =
        dependentServices.plus( serviceKlass )
            .map { of( it ) }
            .firstOrNull {
                val splitNamespace = classDiscriminator.split( '.' )
                val subsystem =
                    try { splitNamespace.takeLast( 5 ).first() }
                    catch ( _: IndexOutOfBoundsException )
                    {
                        throw IllegalArgumentException( "Unexpected class discriminator." )
                    }
                subsystem == it.subsystemName
            }
}
