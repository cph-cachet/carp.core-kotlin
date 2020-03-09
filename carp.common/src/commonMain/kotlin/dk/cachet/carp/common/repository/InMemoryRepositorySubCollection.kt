package dk.cachet.carp.common.repository

class InMemoryRepositorySubCollection<K, T>( private val parentCollection: MutableMap<K, *>? = null ) : RepositorySubCollection<K, T>
{
    private val collection: MutableMap<K, MutableList<T>> = mutableMapOf()

    override fun getAll( key: K ): List<T>
    {
        require( parentCollection == null || key in parentCollection )

        return collection[key]?.toList() ?: listOf()
    }

    override fun addRemove( key: K, addToCollection: Set<T>, removeFromCollection: Set<T> )
    {
        require( parentCollection == null || key in parentCollection )

        val list = collection.getOrPut( key ) { mutableListOf() }

        require( addToCollection.intersect( list ).isEmpty() )

        collection[key] = list
            .union( addToCollection )
            .minus( removeFromCollection )
            .toMutableList()
    }
}
