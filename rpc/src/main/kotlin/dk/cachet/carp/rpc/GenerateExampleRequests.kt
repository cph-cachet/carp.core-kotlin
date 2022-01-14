package dk.cachet.carp.rpc

import dk.cachet.carp.common.application.services.ApplicationService


fun generateExampleRequests(
    applicationServiceInterface: Class<out ApplicationService<*, *>>,
    requestObjectSuperType: Class<*>
)
{
    val requests = applicationServiceInterface.methods
    val requestObjects = requestObjectSuperType.classes

    requests.associateWith { request ->
        val requestObjectName = request.name.replaceFirstChar { it.uppercase() }
        val requestObject = requestObjects.singleOrNull { it.simpleName == requestObjectName }
        requireNotNull( requestObject )
            {
                "Could not find request object for ${applicationServiceInterface.name}.${request.name}. " +
                "Searched for: ${requestObjectSuperType.name}.$requestObjectName"
            }

        // TODO: For each request object, generate example JSON request and response.
    }
}
