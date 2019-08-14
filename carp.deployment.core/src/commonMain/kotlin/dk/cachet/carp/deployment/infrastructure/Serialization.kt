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
 * Create a [MasterDeviceDeployment] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun MasterDeviceDeployment.Companion.fromJson(json: String ): MasterDeviceDeployment
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun MasterDeviceDeployment.toJson(): String
    = JSON.stringify( MasterDeviceDeployment.serializer(), this )