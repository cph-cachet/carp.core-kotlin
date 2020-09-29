package dk.cachet.carp.common.data

import dk.cachet.carp.common.Immutable
import dk.cachet.carp.common.ImplementAsDataClass
import kotlinx.serialization.Polymorphic


/**
 * Holds data for a [DataType].
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
interface Data
