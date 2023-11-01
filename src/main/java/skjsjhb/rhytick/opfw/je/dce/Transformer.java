package skjsjhb.rhytick.opfw.je.dce;

/**
 * Compatibility layer for elementary DTs between JS and JVM.
 */
@DCEModule("transformer")
public class Transformer {
    /**
     * Convert a byte array to an enumerable type.
     */
    @Expose
    public TByteArray from(byte[] source) {
        return new TByteArray(source);
    }

    public static class TByteArray {
        protected byte[] array;

        int counter = 0;

        public TByteArray(byte[] aArray) {
            array = aArray;
        }

        @Expose
        public int getLength() {
            return array.length;
        }

        @Expose
        public byte nextByte() {
            return array[counter++];
        }
    }
}
