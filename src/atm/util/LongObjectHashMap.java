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
        return e.find(key);
    }
    
    public V put(long key, V value) {
        int i = index(key);
        if (data[i] == null) {
            return insertRootEntry(i, getNewEntry(key, value));
        }
        Entry<V> e = data[i];
        return e.insert(this, key, value);
    }

    private Entry<V> getNewEntry(long key, V value) {
        return new Entry<>(key, value);
    }

    private V insertRootEntry(int i, Entry<V> newEntry) {
        Entry<V> oldEntry = data[i];
        assert oldEntry == null;
        data[i] = newEntry;
        incSize();
        return null;
    }

    public V remove(long key) {
        int i = index(key);
        Entry<V> e = data[i];
        if (e != null) {
            return e.remove(this, i, key);
        }
        return null;
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
                rootEntry.collect(f, result);
            }
        }
        return result;
    }

    private int index(long key) {
        return (int) (key ^ (key >>> 32)) % data.length;
    }

    private void incSize() {
        ++size;
        resize();
    }

    private void decSize() {
        --size;
        resize();
    }

    private void resize() {
        if (size > data.length * 0.75) {
            Entry<V>[] oldData = data;
            data = new Entry[data.length * 2];
            fill(oldData);
        }
        // may be truncate?
    }

    private void fill(Entry<V>[] data) {
        for (Entry<V> e : data) {
            e.putRecursively(this);
        }
    }

    // static: ugly, but memory saving
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

        protected V find(long key) {
            Entry<V> e = this;
            while (e != null) {
                if (e.key == key) {
                    return e.value;
                }
                e = e.next;
            }
            return null;
        }

        protected V insert(LongObjectHashMap<V> table, long key, V value) {
            Entry<V> e = this;
            Entry<V> oldE = this;
            while (e != null) {
                oldE = e;
                if (e.key == key) {
                    return insert(e, value);
                }
                e = e.next;
            }
            return insert(table, oldE, new Entry<>(key, value));
        }

        private V insert(Entry<V> e, V v) {
            V oldV = e.value;
            e.value = v;
            return oldV;
        }

        private V insert(LongObjectHashMap<V> t, Entry<V> oldE, Entry<V> newE) {
            oldE.next = newE;
            t.incSize();
            return null;
        }

        protected V remove(LongObjectHashMap<V> t, int i, long key) {
            if (this.key == key) {
                t.data[i] = this.next;
                t.decSize();
                return this.value;
            }
            Entry<V> e = this;
            while (e.next != null) {
                Entry<V> prev = e;
                e = e.next;
                if (e.key == key) {
                    return remove(t, prev, e);
                }
            }
            return null;
        }

        private V remove(LongObjectHashMap<V> t, Entry<V> prev, Entry<V> e) {
            V value = e.value;
            prev.next = e.next;
            t.decSize();
            return value;
        }

        private <T> void collect(Function<Entry<V>, T> f, Set<T> dest) {
            Entry<V> e = this;
            while (e != null) {
                dest.add(f.apply(e));
                e = e.next;
            }
        }

        protected void putRecursively(LongObjectHashMap<V> t) {
            Entry<V> e = this;
            while (e != null) {
                t.put(e.key, e.value);
                e = e.next;
            }
        }
    }
}
