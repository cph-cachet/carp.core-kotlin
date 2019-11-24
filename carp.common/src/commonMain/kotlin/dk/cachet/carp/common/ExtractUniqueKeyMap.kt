package dk.cachet.carp.common


/**
 * A [Map] which automatically extracts the key of added values and throws an error when attempting to add an element with a key which is already present.
 *
 * However, elements which have already been added (using referential equality checks) are ignored.
 * Adding them again won't change the collection and does not throw an error.
 */
class ExtractUniqueKeyMap<K, V>(
    /**
     * Specifies how to retrieve the key for the specified element.
     */
    private val keyOf: (V) -> K,
    /**
     * The error which should be thrown when attempting to add an element with a key which is already present.
     */
    private val elementPresentError: Throwable ) : Map<K, V>
{
    private val map: MutableMap<K, V> = mutableMapOf()

    /**
     * Associates the specified [element] with the key extracted from the [element] and adds the key/value pair in case the key does not yet exist.
     * Throws the exception as specified during the initialization of this map when the key already exists.
     *
     * @param element The element for which to extract the key and add as a key/value pair.
     * @return True if the element has been added; false if the specified element is already included in the map.
     */
    fun tryAddIfKeyIsNew( element: V ): Boolean
    {
        val storedElement: V? = map[ keyOf( element ) ]
        if ( storedElement != null )
        {
            if ( element === storedElement )
            {
                return false
            }

            throw elementPresentError
        }

        map[ keyOf( element ) ] = element
        return true
    }

    /**
     * Removes the specified [element] from this collection.
     *
     * @param element The element to remove.
     * @return True if the element has been removed; false if the specified element is not included in this collection.
     */
    fun remove( element: V ): Boolean
    {
        return map.remove( keyOf( element ) ) != null
    }

    override val entries: Set<Map.Entry<K, V>> get() = map.entries
    override val keys: Set<K> get() = map.keys
    override val size: Int get() = map.size
    override val values: Collection<V> get() = map.values
    override fun containsKey( key: K ): Boolean = map.containsKey( key )
    override fun containsValue( value: V ): Boolean = map.containsValue( value )
    override fun get( key: K ): V? = map[ key ]
    override fun isEmpty(): Boolean = map.isEmpty()
}