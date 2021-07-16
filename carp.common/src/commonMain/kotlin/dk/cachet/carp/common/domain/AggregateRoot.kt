package dk.cachet.carp.common.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


/**
 * A root objects which ensures the integrity of underlying state as a whole, tracks events raised from within,
 * and for which an immutable 'snapshot' at any given moment in time can be obtained.
 */
abstract class AggregateRoot<TRoot, TSnapshot : Snapshot<TRoot>, TEvent : DomainEvent>
{
    /**
     * The date when this object was created.
     */
    var creationDate: Instant = Clock.System.now()
        protected set

    private val events: MutableList<TEvent> = mutableListOf()

    /**
     * Add an event triggered by this aggregate root to a queue.
     */
    protected fun event( event: TEvent ) = events.add( event )

    /**
     * In case the instance equals [value], [createEvent] and add it to the event queue of this aggregate root.
     */
    protected fun <TPredicate> TPredicate.eventIf( value: TPredicate, createEvent: () -> TEvent ): TPredicate =
        this.also { if ( this == value ) event( createEvent() ) }

    /**
     * Returns all tracked events on this aggregate root in the order they were triggered and clears the queue.
     */
    fun consumeEvents(): List<TEvent>
    {
        val toProcess = events.toList()
        events.clear()
        return toProcess
    }

    /**
     * Get an immutable snapshot representing the state of this aggregate at this moment in time.
     */
    abstract fun getSnapshot(): TSnapshot
}
