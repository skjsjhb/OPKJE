/**
 * This is the type definition of OPKJE implementation of OPFW.
 *
 * Note that this is NOT the variation, subset or superset of the OPFW specification. This declaration
 * should only be used by the preload script as a reference of how to construct the exported APIs.
 *
 * Under very a few circumstances, native extensions might utilitize the native interfaces to provide
 * features which are not possible to implement using standard APIs. However, this behavior is unstable, not supported,
 * and should be limited to a small scope only. We'll also continually to try to improve the API to cover more
 * requirements.
 */
type KnownRequireTypeMap<T> =
    T extends "version" ? Version :
        T extends "cfg" ? Cfg :
            T extends "kv" ? KV :
                T extends "timer" ? TimerFactory :
                    T extends "finder" ? Finder :
                        T extends "transformer" ? Transformer :
                            T extends "util" ? Util :
                                never;

type KnownRequireNames = "version" | "cfg" | "kv" | "timer" | "finder" | "transformer" | "util";

declare interface VM {
    getVMInfo(): string;

    library(name: string): void;

    requestLoop(f: () => any): void;

    require<T extends KnownRequireNames>(name: T): KnownRequireTypeMap<T>;

    stop(): void;
}

declare interface Version {
    getAPIVersion(): number;

    getProdString(): string;
}

declare interface Cfg {
    getBoolean(k: string, dv: boolean): boolean;

    getBoolean(k: string): boolean;

    getDouble(k: string, dv: number): number;

    getDouble(k: string): number;

    getInt(k: string, dv: number): number;

    getInt(k: string): number;

    getValue(k: string, dv: string): string;

    getValue(k: string): string;
}

declare interface KV {
    get(key: string): string | null;

    set(key: string, value: string): void;
}


declare interface Timer {
    getApproxTime(): number;

    getHighResTime(): number;
}

declare interface TimerFactory {
    newTimer(): Timer;
}

declare interface Finder {
    readFile(vpt: string): any; /* byte[] */

    writeFile(vpt: string, dat: Uint8Array | string): void;
}

declare interface Util {
    decodeString(a: Uint8Array): string;

    encodeString(a: string): Uint8Array;

    toArray(a: any /* T[] */): any[];
}

// @ts-ignore
declare global {
    const VM: VM;
}
