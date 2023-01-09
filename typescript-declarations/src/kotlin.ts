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
    }

    // Augment internal types to implement desired base interfaces.
    namespace $_$
    {
        abstract class Long implements kotlin.Long
        {
            toNumber(): number
        }
        interface Collection<T> extends kotlin.collections.Collection<T> {}
        interface List<T> extends kotlin.collections.List<T> {}
        abstract class EmptyList<T> implements kotlin.collections.List<T>
        {
            contains( value: T ): boolean
            size(): number
        }
        abstract class AbstractMutableList<T> implements kotlin.collections.List<T>
        {
            contains( value: T ): boolean
            size(): number
        }
        interface Set<T> extends kotlin.collections.Set<T> {}
        abstract class EmptySet<T> implements kotlin.collections.Set<T>
        {
            contains( value: T ): boolean
            size(): number
        }
        abstract class HashSet<T> implements kotlin.collections.Set<T>
        {
            contains( value: T ): boolean
            size(): number
        }
    }
}

// Implement base interfaces in internal types.
kotlinStdLib.$_$.Long.prototype.toNumber = function(): number { return this.y4(); };
kotlinStdLib.$_$.EmptyList.prototype.contains = function<T>( value: T ): boolean { return false; }
kotlinStdLib.$_$.EmptyList.prototype.size = function<T>(): number { return 0; }
kotlinStdLib.$_$.EmptyList.prototype.toArray = function<T>(): T[] { return []; }
kotlinStdLib.$_$.AbstractMutableList.prototype.contains = function<T>( value: T ): boolean { return this.g( value ); }
kotlinStdLib.$_$.AbstractMutableList.prototype.size = function<T>(): number { return this.f(); }
kotlinStdLib.$_$.EmptySet.prototype.contains = function<T>( value: T ): boolean { return false; }
kotlinStdLib.$_$.EmptySet.prototype.size = function<T>(): number { return 0; }
kotlinStdLib.$_$.EmptySet.prototype.toArray = function<T>(): T[] { return []; }
kotlinStdLib.$_$.HashSet.prototype.contains = function<T>( value: T ): boolean { return this.g( value ); }
kotlinStdLib.$_$.HashSet.prototype.size = function<T>(): number { return this.f(); }

// Export facade.
export namespace kotlin
{
    export type Long = kotlinStdLib.kotlin.Long
    export const toLong: (number: number) => kotlinStdLib.kotlin.Long = kotlinStdLib.$_$.toLong
}
export namespace kotlin.collections
{
    export type Collection<T> = kotlinStdLib.kotlin.collections.Collection<T>
    export type List<T> = kotlinStdLib.kotlin.collections.List<T>
    export const listOf: <T>(array: T[]) => List<T> = kotlinStdLib.$_$.listOf
    export type Set<T> = kotlinStdLib.kotlin.collections.Set<T>
    export const setOf: <T>(array: T[]) => Set<T> = kotlinStdLib.$_$.setOf
}
