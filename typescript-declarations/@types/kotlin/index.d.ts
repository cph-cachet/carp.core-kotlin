declare module 'kotlin'
{
    class Long
    {
        static fromNumber( value: number ): Long

        toNumber(): number
    }


    namespace kotlin.collections
    {
        class ArrayList<T>
        {
            constructor( array: T[] )

            get_za3lpa$( index: number ): T
        }
        function toList_us0mfu$<T>( array: T[] ): ArrayList<T>

        class HashSet<T>
        {
            contains_11rb$( element: T ): boolean
        }
        function toSet_us0mfu$<T>( array: T[] ): HashSet<T>
    }
}
