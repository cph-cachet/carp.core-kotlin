package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import kotlinx.serialization.Polymorphic


/**
 * Holds data for a [DataType].
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
interface Data
