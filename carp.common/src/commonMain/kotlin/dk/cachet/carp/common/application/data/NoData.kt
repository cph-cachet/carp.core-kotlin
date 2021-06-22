package dk.cachet.carp.common.application.data

import kotlinx.serialization.Serializable


/**
 * Placeholder for generic `Data` types to indicate there is no associated data.
 * This should not be serialized; instead, nullable `Data` should be used.
 */
@Serializable
object NoData : Data
