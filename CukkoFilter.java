import java.util.Random;

/**
 * A Java implemetion of Cuckoo Filter
 */
public class CukkoFilter {

    static final int MAXIMUM_CAPACITY = 1 << 30;

    private final int MAX_NUM_KICKS = 500;
    private int capacity;
    private int size = 0;
    private Bucket[] buckets;
    private Random random;

    public CukkoFilter(int capacity) {
        this.capacity = tableSizeFor(capacity);
        buckets = new Bucket[capacity];
        random = new Random();
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new Bucket();
        }
    }


    /**
     * insert an object into cuckoo filter
     *
     * @param o
     * @return false when filter consideredly full or  insert an null object
     */
    public boolean insert(Object o) {
        if (o == null)
            return false;
        byte f = fingerprint(o);
        int i1 = hash(o);
        int i2 = i1 ^ hash(f);

        if (buckets[i1].insert(f) || buckets[i2].insert(f)) {
            size++;
            return true;
        }
        // must relocate existing items
        return relocateAndInsert(i1, i2, f);
    }

    public boolean insertUnique(Object o) {
        if (contains(o))
            return false;
        return insert(o);
    }


    private boolean relocateAndInsert(int i1, int i2, byte f) {
        boolean flag = random.nextBoolean();
        int itemp = flag ? i1 : i2;
        for (int i = 0; i < MAX_NUM_KICKS; i++) {
            int position = random.nextInt(Bucket.BUCKET_SIZE);
            f = buckets[itemp].swap(position, f);
            itemp = itemp ^ hash(f);
            if (buckets[itemp].insert(f)) {
                size++;
                return true;
            }
        }
        return false;
    }


    public boolean contains(Object o) {
        byte f = fingerprint(o);
        int i1 = hash(o);
        int i2 = i1 ^ hash(f);
        return buckets[i1].contains(f) || buckets[i2].contains(f);
    }

    /**
     * delete object from cuckoo filter.Note that, to delete an item x safely, it must have been
     * previously inserted.
     *
     * @param o
     * @return
     */
    public boolean delete(Object o) {
        byte f = fingerprint(o);
        int i1 = hash(o);
        int i2 = i1 ^ hash(f);
        return buckets[i1].delete(f) || buckets[i2].delete(f);
    }

    public int size() {
        return size;
    }

    private byte fingerprint(Object o) {
        int h = o.hashCode();
        h = (h + 0x7ed55d16) + (h << 12);
        h = (h ^ 0xc761c23c) ^ (h >> 19);
        h = (h + 0x165667b1) + (h << 5);
        h = (h + 0xd3a2646c) ^ (h << 9);
        h = (h + 0xfd7046c5) + (h << 3);
        h = (h ^ 0xb55a4f09) ^ (h >> 16);
        if (h == Bucket.NULL_FINGERPRINT)
            h = 40;
        return (byte) h;
    }

    public int hash(Object key) {
        int h = hashint(key.hashCode());
        h -= (h << 6);
        h ^= (h >> 17);
        h -= (h << 9);
        h ^= (h << 4);
        h -= (h << 3);
        h ^= (h << 10);
        h ^= (h >> 15);
        return h & (capacity - 1);
    }

    public static int hashint(int a) {

        return a;
    }

    public boolean isEmpty() {
        return size == 0;
    }


    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    static class Bucket {
        public static final int FINGERPINT_SIZE = 1;
        public static final int BUCKET_SIZE = 4;
        public static final byte NULL_FINGERPRINT = 0;

        private final byte[] fps = new byte[BUCKET_SIZE];

        public boolean insert(byte fingerprint) {
            for (int i = 0; i < fps.length; i++) {
                if (fps[i] == NULL_FINGERPRINT) {
                    fps[i] = fingerprint;
                    return true;
                }
            }
            return false;
        }


        public boolean delete(byte fingerprint) {
            for (int i = 0; i < fps.length; i++) {
                if (fps[i] == fingerprint) {
                    fps[i] = NULL_FINGERPRINT;
                    return true;
                }
            }
            return false;
        }

        public boolean contains(byte fingerprint) {
            for (int i = 0; i < fps.length; i++) {
                if (fps[i] == fingerprint)
                    return true;
            }
            return false;
        }

        public byte swap(int position, byte fingerprint) {
            byte tmpfg = fps[position];
            fps[position] = fingerprint;
            return tmpfg;
        }
    }

}
