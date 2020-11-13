package dk.cachet.carp.common.ddd

import dk.cachet.carp.common.DateTime


/**
 * A root objects which ensures the integrity of underlying state as a whole, tracks events raised from within,
 * and for which an immutable 'snapshot' at any given moment in time can be obtained.
 */
abstract class AggregateRoot<TRoot, TSnapshot : Snapshot<TRoot>, TEvent : DomainEvent>
{
    /**
     * The date when this object was created.
     */
    var creationDate: DateTime = DateTime.now()
        protected set

    private val events: MutableList<TEvent> = mutableListOf()

    /**
     * Add an event triggered by this aggregate root to a queue.
     */
    protected fun event( event: TEvent ) = events.add( event )

    /**
     * In case the instance this is called on is true, [createEvent] and add it to the event queue of this aggregate root.
     */
    protected fun Boolean.eventOnSuccess( createEvent: () -> TEvent ): Boolean =
        this.also { if ( this ) event( createEvent() ) }

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
