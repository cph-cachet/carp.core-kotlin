declare module "kotlin-kotlin-stdlib-js-ir"
{
    namespace $_$
    {
        interface Long
        {
            // toNumber
            p4(): number
        }
        function toLong_0( number: number ): Long

        class Pair<K, V>
        {
            constructor( first: K, second: V )

            // first
            s2_1: K

            // second
            t2_1: V
        }

        interface Collection<T>
        {
            // contains
            y( value: T ): boolean

            // size
            f(): number

            toArray(): Array<T>
        }

        interface List<T> extends Collection<T> {}
        interface EmptyList<T> extends List<T> {}
        interface AbstractMutableList<T> extends List<T> {}
        function listOf<T>( elements: T[] ): List<T>

        interface Set<T> extends Collection<T> {}
        interface EmptySet<T> extends Set<T> {}
        interface HashSet<T> extends Set<T> {}
        function setOf<T>( elements: T[] ): Set<T>

        interface Map<K, V>
        {
            // get
            x1( key: K ): V

            // keys
            y1(): Set<K>

            // values
            z1(): Collection<V>
        }
        interface HashMap<K, V> extends Map<K, V> {}
        function mapOf<K, V>( pairs: Pair<K, V>[] ): Map<K, V>
    }
}
