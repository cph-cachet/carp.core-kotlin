package dk.cachet.carp.common.domain

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.DateTime


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
