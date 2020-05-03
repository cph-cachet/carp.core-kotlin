package dk.cachet.carp.common


/**
 * A helper class to construct iterable objects which hold [T] member definitions.
 * This is similar to an enum, but removes the need for an intermediate enum type and generic type parameters are retained per member.
 *
 * Extend from this class as an object and assign members as follows: `val MEMBER = add( SomeMember() )`.
 */
open class EnumObjectList<T> private constructor( private val list: MutableList<T> ) : List<T> by list
{
    constructor() : this( mutableListOf() )

    protected fun <TAdd : T> add( item: TAdd ): TAdd = item.also { list.add( it ) }
}
