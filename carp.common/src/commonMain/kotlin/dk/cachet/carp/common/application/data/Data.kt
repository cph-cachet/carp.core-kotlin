package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Holds data for a [DataType].
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
interface Data


/**
 * Placeholder for generic `Data` types to indicate there is no associated data.
 * This should not be serialized; instead, nullable `Data` should be used.
 */
@Serializable
@JsExport
object NoData : Data
