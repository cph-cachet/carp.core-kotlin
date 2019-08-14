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
 * Create a [StudyDeploymentStatus] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDeploymentStatus.Companion.fromJson( json: String ): StudyDeploymentStatus
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun StudyDeploymentStatus.toJson(): String
    = JSON.stringify( StudyDeploymentStatus.serializer(), this )

/**
 * Create a [MasterDeviceDeployment] from JSON, serialized using the globally set infrastructure serializer ([JSON]).
 */
fun MasterDeviceDeployment.Companion.fromJson( json: String ): MasterDeviceDeployment
    = JSON.parse( serializer(), json )

/**
 * Serialize to JSON, using the globally set infrastructure serializer ([JSON]).
 */
fun MasterDeviceDeployment.toJson(): String
    = JSON.stringify( MasterDeviceDeployment.serializer(), this )