package dk.cachet.carp.common.ddd

import dk.cachet.carp.common.DateTime
import dk.cachet.carp.common.Immutable


/**
 * An immutable snapshot of an [AggregateRoot] at a given moment in time.
 */
abstract class Snapshot<TAggregateRoot> : Immutable()
{
    abstract val creationDate: DateTime


    /**
     * Load the aggregate root object from this snapshot.
     */
    abstract fun toObject(): TAggregateRoot
}
