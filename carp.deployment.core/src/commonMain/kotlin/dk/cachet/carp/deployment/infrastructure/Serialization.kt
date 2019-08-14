package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.deployment.domain.*
import dk.cachet.carp.protocols.infrastructure.JSON


/**
 * Create a [StudyDeploymentSnapshot] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDeploymentSnapshot.Companion.fromJson( json: String ): StudyDeploymentSnapshot
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDeploymentSnapshot.toJson(): String
    = JSON.stringify( StudyDeploymentSnapshot.serializer(), this )

/**
 * Create a [DeploymentStatus] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun DeploymentStatus.Companion.fromJson( json: String ): DeploymentStatus
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun DeploymentStatus.toJson(): String
    = JSON.stringify( DeploymentStatus.serializer(), this )

/**
 * Create a [DeviceDeployment] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun DeviceDeployment.Companion.fromJson( json: String ): DeviceDeployment
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun DeviceDeployment.toJson(): String
    = JSON.stringify( DeviceDeployment.serializer(), this )