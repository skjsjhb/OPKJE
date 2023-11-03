console.log("[OPKJE Preload Compatibility Layer Started]");

(() => {
    const version = VM.require("version");
    globalThis.Sys = {
        about: () => {
            return `${version.getProdString()}, ${VM.getVMInfo()}`
        }
    };
})();

console.log(Sys.about());