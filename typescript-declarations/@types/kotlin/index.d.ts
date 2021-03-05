declare module 'kotlin'
{
    class Long
    {
        static fromNumber( value: number ): Long

        toNumber(): number
    }


    namespace kotlin
    {
        class Pair<TFirst, TSecond>
        {
            constructor( first: TFirst, second: TSecond )

            readonly first: TFirst
            readonly second: TSecond
        }
    }


    namespace kotlin.collections
    {
        import Pair = kotlin.Pair


        class AbstractCollection<T>
        {
            toArray(): T[]
        }

        class ArrayList<T> extends AbstractCollection<T>
        {
            constructor( array: T[] )

            readonly size: number

            get_za3lpa$( index: number ): T
        }
        function toList_us0mfu$<T>( array: T[] ): ArrayList<T>

        class HashSet<T> extends AbstractCollection<T>
        {
            contains_11rb$( element: T ): boolean
        }
        function toSet_us0mfu$<T>( array: T[] ): HashSet<T>

        class HashMap<TKey, TValue> {}
        function toMap_v2dak7$<TKey, TValue>( pairs: Pair<TKey, TValue>[] ): HashMap<TKey, TValue>
    }
}
