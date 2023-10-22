declare module "@cachet/kotlin-kotlin-stdlib-js-ir"
{
    namespace $_$
    {
        interface Long
        {
            // toNumber
            c1(): number
        }
        function toLong_0( number: number ): Long

        class Pair<K, V>
        {
            constructor( first: K, second: V )

            // first
            b3_1: K

            // second
            c3_1: V
        }

        interface Collection<T>
        {
            // contains
            e1( value: T ): boolean

            // size
            i(): number

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
            f2( key: K ): V

            // keys
            g2(): Set<K>

            // values
            h2(): Collection<V>
        }
        interface HashMap<K, V> extends Map<K, V> {}
        function mapOf<K, V>( pairs: Pair<K, V>[] ): Map<K, V>

        interface Duration extends Long {}
        interface DurationCompanion
        {
            // parseIsoString
            n6(): Duration

            // ZERO
            k6_1: Duration

            // INFINITE
            l6_1: Duration
        }
        function Companion_getInstance_7(): DurationCompanion
        function _Duration___get_inWholeMilliseconds__impl__msfiry(duration: Duration): Long
        function _Duration___get_inWholeMicroseconds__impl__8oe8vv(duration: Duration): Long
    }
}