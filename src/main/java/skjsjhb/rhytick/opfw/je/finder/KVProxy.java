package skjsjhb.rhytick.opfw.je.finder;

import skjsjhb.rhytick.opfw.je.dce.DCEModule;
import skjsjhb.rhytick.opfw.je.dce.Expose;

import javax.annotation.Nullable;

/**
 * Proxy class for KV static interfaces.
 */
@DCEModule("kv")
public class KVProxy {
    @Nullable
    @Expose
    public Object get(String k) {
        return KV.get(k);
    }

    @Expose
    public void set(String k, Object v) {
        KV.set(k, v);
    }
}
