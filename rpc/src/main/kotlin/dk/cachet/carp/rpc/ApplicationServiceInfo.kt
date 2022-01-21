package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.services.ApplicationService


/**
 * Determines associated names and file location of related code artifacts of
 * the application service identified by [klass].
 */
class ApplicationServiceInfo( val klass: Class<out ApplicationService<*, *>> )
{
    companion object
    {
        /**
         * The namespace under which subsystem namespaces are defined in which the application service resides.
         */
        const val NAMESPACE = "dk.cachet.carp"
    }


    val requestObjectName = "${klass.simpleName}Request"

    val subsystemName = checkNotNull(
        Regex( Regex.escape( NAMESPACE ) + "\\.([^.]+)" )
            .find( klass.`package`.name )
            ?.groupValues?.lastOrNull()
    ) { "${klass.name} is residing in an unexpected namespace." }

    val requestSchemaPath = "json-schemas/$subsystemName/${klass.simpleName}/$requestObjectName-schema.json"
}
