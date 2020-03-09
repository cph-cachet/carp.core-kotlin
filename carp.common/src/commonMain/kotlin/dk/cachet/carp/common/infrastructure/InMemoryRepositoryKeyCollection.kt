package dk.cachet.carp.common.infrastructure

class InMemoryRepositoryKeyCollection<K, T>( private val keyCheck: (key: K ) -> Boolean = { true } ) : RepositoryKeyCollection<K, T>
{
    private val collection: MutableMap<K, List<T>> = mutableMapOf()

    override fun getAll( key: K ): List<T>
    {
        require( keyCheck( key ) )

        return collection[key] ?: emptyList()
    }

    override fun addRemove( key: K, addToCollection: Set<T>, removeFromCollection: Set<T> )
    {
        require( keyCheck( key ) )

        val list = collection.getOrPut( key ) { mutableListOf() }

        require( addToCollection.intersect( list ).isEmpty() )

        collection[key] = list
            .union( addToCollection )
            .minus( removeFromCollection )
            .toList()
    }
}
