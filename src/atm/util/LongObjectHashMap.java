package atm.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class LongObjectHashMap<V> {
    private Entry<V>[] data = new Entry[0];
    private int size = 0;

    public LongObjectHashMap() {}

    public LongObjectHashMap(int size) {
        this.data = new Entry[size + size / 3];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(long key) {
        return get(key) != null;
    }
    
    public V get(long key) {
        int i = index(key);
        Entry<V> e = data[i];
        while (e != null) {
            if (e.key == key) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }
    
    public V put(long key, V value) {
        int i = index(key);
        if (data[i] == null) {
            return insert(i, new Entry<>(key, value));
        }
        Entry<V> root = data[i];
        if (root.key == key) {
            return insert(root, value);
        }
        return insertIntoEntryChain(key, value, root);
    }

    private V insertIntoEntryChain(long key, V value, Entry<V> oldEntry) {
        // avoiding needless recursive method calls
        while (oldEntry.next != null) {
            oldEntry = oldEntry.next;
            if (oldEntry.key == key) {
                return insert(oldEntry, value);
            }
        }
        return insert(oldEntry, new Entry<>(key, value));
    }

    public V remove(long key) {
        int i = index(key);
        Entry<V> rootEntry = data[i];
        if (rootEntry != null) {
            if (rootEntry.key == key) {
                return remove(i, rootEntry);
            }
            return removeFromChain(key, rootEntry);
        }
        return null;
    }

    private V removeFromChain(long key, Entry<V> e) {
        while (e.next != null) {
            Entry<V> prev = e;
            e = e.next;
            if (e.key == key) {
                return remove(prev, e);
            }
        }
        return null;
    }

    private V remove(Entry<V> prev, Entry<V> e) {
        V value = e.value;
        prev.next = e.next;
        --size;
        resize();
        return value;
    }

    private V remove(int i, Entry<V> rootEntry) {
        V value = rootEntry.value;
        if (rootEntry.next != null) {
            data[i] = rootEntry.next;
        }
        --size;
        resize();
        return value;
    }

    public void clear() {
        for (int i = 0; i < data.length; i++) {
            data[i] = null;
        }
        size = 0;
        resize();
    }
    
    public Set<Long> keySet() {
        return collect(Entry::getKey);
    }

    public Collection<V> values() {
        return collect(Entry::getValue);
    }

    
    public Set<Entry<V>> entrySet() {
        return collect((e) -> e);
    }

    private <T> Set<T> collect(Function<Entry<V>, T> f) {
        Set<T> result = new HashSet<>(size);
        for (Entry<V> rootEntry : data) {
            if (rootEntry != null) {
                collectFromChain(f, result, rootEntry);
            }
        }
        return result;
    }

    private <T> void collectFromChain(Function<Entry<V>, T> f, Set<T> result, Entry<V> e) {
        result.add(f.apply(e));
        while (e.next != null) {
            e = e.next;
            result.add(f.apply(e));
        }
    }

    private int index(long key) {
        return (int) (key ^ (key >>> 32)) % data.length;
    }

    private V insert(int i, Entry<V> newEntry) {
        Entry<V> oldEntry = data[i];
        data[i] = newEntry;
//        if (oldEntry != null) {
//            newEntry.next = oldEntry.next;
//            return oldEntry.value;
//        }
        assert oldEntry == null;
        ++size;
        resize();
        return null;
    }

    private V insert(Entry<V> oldEntry, Entry<V> newEntry) {
        oldEntry.next = newEntry;
        ++size;
        resize();
        return null;
    }

    private V insert(Entry<V> entry, V value) {
        V oldValue = entry.value;
        entry.value = value;
        return oldValue;
    }

    private void resize() {
        if (size > data.length * 0.75) {
            Entry<V>[] oldData = data;
            data = new Entry[data.length * 2];
            refillWithValues(oldData);
        }
        // may be truncate?
    }

    private void refillWithValues(Entry<V>[] values) {
        for (Entry<V> root : values) {
            while (root != null) {
                put(root.key, root.value);
                root = root.next;
            }
        }
    }

    public static class Entry<V> {
        private final long key;
        private V value;
        private Entry<V> next;

        private Entry(long key, V value) {
            this.key = key;
            this.value = value;
        }

        public long getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
