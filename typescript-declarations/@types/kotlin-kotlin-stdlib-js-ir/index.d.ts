declare module 'kotlin-kotlin-stdlib-js-ir'
{
    namespace $crossModule$
    {
        abstract class Long
        {
            readonly _low: number
            readonly _high: number
            toInt_0_k$(): number
        }
        function toInt( long: Long ): number
        function numberToLong( number: number ): Long

        interface Collection<T>
        {
            toArray(): Array<T>
            _get_size__0_k$(): number
        }
        function listOf<T>( elements: T[] ): List<T>
        interface List<T> extends Collection<T> { }
        interface Set<T> extends Collection<T> { }
        function setOf<T>( elements: T[] ): Set<T>
        function contains_0<T>( collection: Collection<T>, element: T ): boolean
    }
}
