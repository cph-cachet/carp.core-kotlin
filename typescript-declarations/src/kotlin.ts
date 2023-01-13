import * as kotlinStdLib from "kotlin-kotlin-stdlib-js-ir"


declare module "kotlin-kotlin-stdlib-js-ir"
{
    // Base interfaces with better method names for internal types.
    namespace kotlin
    {
        interface Long
        {
            toNumber(): number
        }
        function toLong( number: number ): Long
        interface Pair<K, V>
        {
            first: K
            second: V
        }
    }
    namespace kotlin.collections
    {
        interface Collection<T>
        {
            contains( value: T ): boolean
            size(): number
            toArray(): Array<T>
        }
        interface List<T> extends Collection<T> {}
        interface Set<T> extends Collection<T> {}
        interface Map<K, V>
        {
            get( key: K ): V
            keys: Set<K>
            values: Collection<V>
        }
        function listOf<T>( array: T[] ): List<T>
        function setOf<T>( array: T[] ): Set<T>
        function mapOf<K, V>( pairs: kotlin.Pair<K, V>[] ): Map<K, V>
    }

    // Augment internal types to implement desired base interfaces.
    namespace $_$
    {
        abstract class Long implements kotlin.Long
        {
            toNumber(): number
        }
        interface Pair<K, V> extends kotlin.Pair<K, V>
        {
            first: K
            second: V
        }
        interface Collection<T> extends kotlin.collections.Collection<T> {}
        abstract class EmptyList<T> implements kotlin.collections.List<T> {}
        abstract class AbstractMutableList<T> implements kotlin.collections.List<T> {}
        interface Set<T> extends kotlin.collections.Set<T> {}
        abstract class EmptySet<T> implements kotlin.collections.Set<T> {}
        abstract class HashSet<T> implements kotlin.collections.Set<T> {}
        interface Map<K, V> extends kotlin.collections.Map<K, V> {}
        abstract class HashMap<K, V> implements kotlin.collections.Map<K, V>
        {
            get( key: K ): V
            keys: kotlin.collections.Set<K>
            values: kotlin.collections.Collection<V>
        }
    }
}

// Implement base interfaces in internal types.
kotlinStdLib.$_$.Long.prototype.toNumber = function(): number { return this.p4(); };
kotlinStdLib.$_$.EmptyList.prototype.contains = function<T>( value: T ): boolean { return false; }
kotlinStdLib.$_$.EmptyList.prototype.size = function<T>(): number { return 0; }
kotlinStdLib.$_$.EmptyList.prototype.toArray = function<T>(): T[] { return []; }
kotlinStdLib.$_$.AbstractMutableList.prototype.contains = function<T>( value: T ): boolean { return this.y( value ); }
kotlinStdLib.$_$.AbstractMutableList.prototype.size = function<T>(): number { return this.f(); }
kotlinStdLib.$_$.EmptySet.prototype.contains = function<T>( value: T ): boolean { return false; }
kotlinStdLib.$_$.EmptySet.prototype.size = function<T>(): number { return 0; }
kotlinStdLib.$_$.EmptySet.prototype.toArray = function<T>(): T[] { return []; }
kotlinStdLib.$_$.HashSet.prototype.contains = function<T>( value: T ): boolean { return this.y( value ); }
kotlinStdLib.$_$.HashSet.prototype.size = function<T>(): number { return this.f(); }
kotlinStdLib.$_$.HashMap.prototype.get = function<K, V>( key: K ): V { return this.x1( key ); }
Object.defineProperty( kotlinStdLib.$_$.HashMap.prototype, "keys", {
    get: function keys() { return this.y1(); }
} );
Object.defineProperty( kotlinStdLib.$_$.HashMap.prototype, "values", {
    get: function values() { return this.z1(); }
} );

// Export facade.
export * from "kotlin-kotlin-stdlib-js-ir"
export namespace kotlin
{
    export const Pair = class<K, V> implements kotlinStdLib.kotlin.Pair<K, V> {
        constructor( first: K, second: V ) {
            let kotlinPair = new kotlinStdLib.$_$.Pair( first, second );
            kotlinPair.first = kotlinPair.s2_1;
            kotlinPair.second = kotlinPair.t2_1;
            return kotlinPair;
        }
        get first(): K { return this.first; }
        get second(): V { return this.second; }
    }
    export const toLong: (number: number) => kotlinStdLib.kotlin.Long = kotlinStdLib.$_$.toLong_0
}
export namespace kotlin.collections
{
    export const listOf: <T>(array: T[]) => kotlinStdLib.kotlin.collections.List<T> = kotlinStdLib.$_$.listOf
    export const setOf: <T>(array: T[]) => kotlinStdLib.kotlin.collections.Set<T> = kotlinStdLib.$_$.setOf
    export const mapOf =
        function<K, V>( pairs: kotlinStdLib.kotlin.Pair<K, V>[] ): kotlinStdLib.kotlin.collections.Map<K, V> {
            return kotlinStdLib.$_$.mapOf( pairs as any )
        }
}
