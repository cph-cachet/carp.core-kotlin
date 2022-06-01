package dk.cachet.carp.common.application


/**
 * A helper class to construct iterable objects which hold [V] member definitions indexed on [K].
 * This is similar to an enum, but removes the need for an intermediate enum type and generic type parameters are retained per member.
 *
 * Extend from this class as an object and assign members as follows: `val MEMBER = add( SomeMember() )`.
 */
open class EnumObjectMap<K, V> private constructor(
    private val map: MutableMap<K, V>,
    val keyOf: (V) -> K
) : Map<K, V> by map
{
    constructor(
        /**
         * Specifies how to retrieve the key for the specified element.
         */
        keyOf: (V) -> K
    ) : this( mutableMapOf(), keyOf )

    /**
     * Add an element using the key which is extracted from [item] using [keyOf].
     *
     * @throws IllegalArgumentException in case the extracted from [item] is already present in this map.
     */
    protected fun <TAdd : V> add( item: TAdd ): TAdd = item.also {
        val key = keyOf( it )
        require( !map.contains( key ) ) { "An item with the same key is already present." }

        map[ key ] = it
    }
}
