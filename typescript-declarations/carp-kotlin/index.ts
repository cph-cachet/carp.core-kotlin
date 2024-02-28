import extend from "@cachet/kotlin-kotlin-stdlib"


// Facade with better method names and type conversions for internal types.
export namespace kotlin
{
    export type Nullable<T> = T | null | undefined
    export interface Long
    {
        toNumber(): number
    }
    export const toLong: (number: number) => Long = extend.$_$.toLong_0
    export class Pair<K, V>
    {
        constructor( first: K, second: V ) {
            let kotlinPair = new extend.$_$.Pair( first, second );
            kotlinPair.first = kotlinPair.md_1;
            kotlinPair.second = kotlinPair.nd_1;
            return kotlinPair;
        }
        get first(): K { return this.first; }
        get second(): V { return this.second; }
    }
}
export namespace kotlin.collections
{
    export interface Collection<T>
    {
        contains( value: T ): boolean
        size(): number
        toArray(): Array<T>
    }
    export interface List<T> extends Collection<T> {}
    export interface Set<T> extends Collection<T> {}
    export interface Map<K, V>
    {
        get( key: K ): V
        keys: Set<K>
        values: Collection<V>
    }
    export const listOf: <T>(array: T[]) => List<T> = extend.$_$.listOf_0
    export const setOf: <T>(array: T[]) => Set<T> = extend.$_$.setOf_0
    export const mapOf =
        function<K, V>( pairs: kotlin.Pair<K, V>[] ): Map<K, V>
        {
            return extend.$_$.mapOf_0( pairs as any )
        }
}
export namespace kotlin.time
{
    export interface Duration
    {
        get inWholeMilliseconds(): number
        get inWholeMicroseconds(): number
    }
    export namespace Duration
    {
        export const Companion: any = extend.$_$.Companion_getInstance_13()
        export const parseIsoString: (isoDuration: string) => Duration = Companion.zf
        export const ZERO: Duration = Companion.wf_1
        export const INFINITE: Duration = Companion.xf_1
    }
}


// Augment internal types to implement facade.
declare module "@cachet/kotlin-kotlin-stdlib"
{
    namespace $_$
    {
        abstract class Long implements kotlin.Long
        {
            toNumber(): number
            inWholeMilliseconds(): number
            inWholeMicroseconds(): number
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
extend.$_$.Long.prototype.toNumber = function(): number { return this.da(); };
Object.defineProperty( extend.$_$.Long.prototype, "inWholeMilliseconds", {
    get: function inWholeMilliseconds()
    {
        return extend.$_$._Duration___get_inWholeMilliseconds__impl__msfiry(this).toNumber();
    }
} );
Object.defineProperty( extend.$_$.Long.prototype, "inWholeMicroseconds", {
    get: function inWholeMicroseconds()
    {
        return extend.$_$._Duration___get_inWholeMicroseconds__impl__8oe8vv(this).toNumber();
    }
} );
extend.$_$.EmptyList.prototype.contains = function<T>( value: T ): boolean { return false; }
extend.$_$.EmptyList.prototype.size = function<T>(): number { return 0; }
extend.$_$.EmptyList.prototype.toArray = function<T>(): T[] { return []; }
extend.$_$.AbstractMutableList.prototype.contains = function<T>( value: T ): boolean { return this.p( value ); }
extend.$_$.AbstractMutableList.prototype.size = function<T>(): number { return this.n(); }
extend.$_$.EmptySet.prototype.contains = function<T>( value: T ): boolean { return false; }
extend.$_$.EmptySet.prototype.size = function<T>(): number { return 0; }
extend.$_$.EmptySet.prototype.toArray = function<T>(): T[] { return []; }
extend.$_$.HashSet.prototype.contains = function<T>( value: T ): boolean { return this.p( value ); }
extend.$_$.HashSet.prototype.size = function<T>(): number { return this.n(); }
extend.$_$.HashMap.prototype.get = function<K, V>( key: K ): V { return this.x2( key ); }
Object.defineProperty( extend.$_$.HashMap.prototype, "keys", {
    get: function keys() { return this.l2(); }
} );
Object.defineProperty( extend.$_$.HashMap.prototype, "values", {
    get: function values() { return this.m2(); }
} );


// Export facade.
export default kotlin
