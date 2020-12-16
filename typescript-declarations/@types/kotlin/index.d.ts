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


        class ArrayList<T>
        {
            constructor( array: T[] )

            // HACK: This internal array is exposed to simplify iterating over Kotlin arrays.
            //       It is unclear to me how to expose Kotlin's higher-order collection functions.
            readonly array_hd7ov6$_0: T[]
            readonly size: number

            get_za3lpa$( index: number ): T
        }
        function toList_us0mfu$<T>( array: T[] ): ArrayList<T>

        class HashSet<T>
        {
            contains_11rb$( element: T ): boolean
        }
        function toSet_us0mfu$<T>( array: T[] ): HashSet<T>

        class HashMap<TKey, TValue> {}
        function toMap_v2dak7$<TKey, TValue>( pairs: Pair<TKey, TValue>[] ): HashMap<TKey, TValue>
    }
}
