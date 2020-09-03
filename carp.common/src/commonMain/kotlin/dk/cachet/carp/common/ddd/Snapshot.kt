package dk.cachet.carp.common.ddd

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.Immutable
import dk.cachet.carp.common.ImplementAsDataClass


/**
 * An immutable snapshot of an [AggregateRoot] at a given moment in time.
 */
@Immutable
@ImplementAsDataClass
interface Snapshot<TAggregateRoot>
{
    val creationDate: DateTime


    /**
     * Load the aggregate root object from this snapshot.
     */
    fun toObject(): TAggregateRoot
}
