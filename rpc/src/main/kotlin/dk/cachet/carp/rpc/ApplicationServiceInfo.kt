package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.services.ApplicationService
import kotlinx.serialization.Serializable
import org.reflections.Reflections
import java.io.File


/**
 * Determines associated classes, names, and file locations of code artifacts
 * related to the application service identified by [serviceKlass].
 */
class ApplicationServiceInfo private constructor(
    val subsystemName: String,
    val serviceKlass: Class<out ApplicationService<*, *>>,
    val requestObjectKlass: Class<*>
)
{
    companion object
    {
        val JSON_EXAMPLES_FOLDER = File( "build/rpc-examples/" )

        /**
         * Find application service info for all application services which are defined in
         * underlying subsystems of the specified [namespace].
         */
        fun findInNamespace( namespace: String ): List<ApplicationServiceInfo>
        {
            val reflections = Reflections( namespace )
            val appServices = reflections
                .getSubTypesOf( ApplicationService::class.java )
                .filter { it.isInterface }
            val serializableObjects = reflections.getTypesAnnotatedWith( Serializable::class.java )

            return appServices.map { serviceKlass ->
                val requestObjectName = "${serviceKlass.simpleName}Request"
                val requestKlass =
                    serializableObjects.singleOrNull { it.simpleName == requestObjectName }
                checkNotNull( requestKlass )
                    { "Could not find request object for \"${serviceKlass.name}\". Searched for: $requestObjectName" }

                val subsystemName: String = checkNotNull(
                    Regex( Regex.escape( namespace ) + "\\.([^.]+)" )
                        .find( serviceKlass.`package`.name )
                        ?.groupValues?.lastOrNull()
                ) { "${serviceKlass.name} is residing in an unexpected namespace." }

                ApplicationServiceInfo( subsystemName, serviceKlass, requestKlass )
            }
        }
    }

    val requestObjectName: String = requestObjectKlass.simpleName

    val jsonExamplesFolder = File( JSON_EXAMPLES_FOLDER, "$subsystemName/$requestObjectName" )

    val requestSchemaFile =
        File( "json-schemas/$subsystemName/$requestObjectName/$requestObjectName-schema.json" )
}
