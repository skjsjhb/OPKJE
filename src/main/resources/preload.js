console.log("[OPKJE Preload Compatibility Layer]");

(() => {
    console.log("Deploying OPFW modules.");

    const version = VM.require("version");
    const ikv = VM.require("kv");
    const finder = VM.require("finder");
    const util = VM.require("util");

    // Sys
    globalThis.Sys = {
        about: () => {
            return `${version.getProdString()}, ${VM.getVMInfo()}`
        },
        stop: () => {
            VM.stop();
        }
    };

    // KV
    globalThis.KV = {
        set: (k, v) => {
            ikv.set(k, JSON.stringify(v));
        },
        get: (k) => {
            const val = ikv.get(k);
            return val == null ? null : JSON.parse(val);
        }
    }

    // Finder
    globalThis.Finder = {
        readFile: (pt) => {
            return new Uint8Array(util.toArray(finder.readFile(pt)));
        },
        readString: (pt) => {
            return util.decodeString(finder.readFile(pt)); // Decoder can handle native bytes
        },
        writeFile: (pt, dat) => {
            finder.writeFile(pt, dat); // Strings are already handled
        },
        decodeString: (a) => {
            return util.decodeString(a);
        },
        getStringBytes: (a) => {
            return new Uint8Array(util.toArray(util.encodeString(a)));
        }
    };
})();