package dk.cachet.carp.common.infrastructure

interface RepositoryKeyCollection<K, T>
{
    fun getAll( key: K ): List<T>
    fun addRemove( key: K, addToCollection: Set<T>, removeFromCollection: Set<T> )
    fun addSingle( key: K, item: T ) = addRemove( key, setOf(item), emptySet() )
}
