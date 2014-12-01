package atm.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class LongObjectHashMap<V> {
    private InnerEntry<V>[] data = new InnerEntry[0];
    private int size = 0;

    public LongObjectHashMap() {}

    public LongObjectHashMap(int size) {
        this.data = new InnerEntry[size + size / 3];
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
        InnerEntry<V> e = data[i];
        if (e == null) {
            return null;
        }
        return e.find(key);
    }

    public V put(long key, V value) {
        int i = index(key);
        if (data[i] == null) {
            return insertRootEntry(i, getNewEntry(key, value));
        }
        InnerEntry<V> e = data[i];
        return e.insert(this, key, value);
    }

    private InnerEntry<V> getNewEntry(long key, V value) {
        return new LinkedEntry<>(key, value);
    }

    private V insertRootEntry(int i, InnerEntry<V> newEntry) {
        assert data[i] == null;
        data[i] = newEntry;
        incSize();
        return null;
    }

    public V remove(long key) {
        int i = index(key);
        InnerEntry<V> e = data[i];
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
        for (InnerEntry<V> rootEntry : data) {
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
            InnerEntry<V>[] oldData = data;
            data = new InnerEntry[data.length * 2];
            fill(oldData);
        }
        // may be truncate?
    }

    private void fill(InnerEntry<V>[] data) {
        for (InnerEntry<V> e : data) {
            if (e != null) {
                e.putRecursively(this);
            }
        }
    }

    public static interface Entry<V> {
        long getKey();
        V getValue();
    }

    static abstract class InnerEntry<V> implements Entry<V> {
        abstract protected V find(long key);
        abstract protected V insert(LongObjectHashMap<V> table, long key, V value);
        abstract protected V remove(LongObjectHashMap<V> t, int i, long key);
        abstract protected <T> void collect(Function<Entry<V>, T> f, Set<T> dest);
        abstract protected void putRecursively(LongObjectHashMap<V> t);
    }

    // static: ugly, but memory saving
    static class LinkedEntry<V> extends InnerEntry<V> {
        protected final long key;
        protected V value;
        private LinkedEntry<V> next;

        private LinkedEntry(long key, V value) {
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
            LinkedEntry<V> e = this;
            while (e != null) {
                if (e.key == key) {
                    return e.value;
                }
                e = e.next;
            }
            return null;
        }

        protected V insert(LongObjectHashMap<V> table, long key, V value) {
            LinkedEntry<V> e = this;
            LinkedEntry<V> oldE = this;
            while (e != null) {
                oldE = e;
                if (e.key == key) {
                    return insert(e, value);
                }
                e = e.next;
            }
            return insert(table, oldE, new LinkedEntry<>(key, value));
        }

        private V insert(LinkedEntry<V> e, V v) {
            V oldV = e.value;
            e.value = v;
            return oldV;
        }

        private V insert(LongObjectHashMap<V> t, LinkedEntry<V> oldE, LinkedEntry<V> newE) {
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
            LinkedEntry<V> e = this;
            while (e.next != null) {
                LinkedEntry<V> prev = e;
                e = e.next;
                if (e.key == key) {
                    return remove(t, prev, e);
                }
            }
            return null;
        }

        private V remove(LongObjectHashMap<V> t, LinkedEntry<V> prev, LinkedEntry<V> e) {
            V value = e.value;
            prev.next = e.next;
            t.decSize();
            return value;
        }

        protected <T> void collect(Function<Entry<V>, T> f, Set<T> dest) {
            LinkedEntry<V> e = this;
            while (e != null) {
                dest.add(f.apply(e));
                e = e.next;
            }
        }

        protected void putRecursively(LongObjectHashMap<V> t) {
            LinkedEntry<V> e = this;
            while (e != null) {
                t.put(e.key, e.value);
                e = e.next;
            }
        }
    }

    static class TreeEntry<V> extends InnerEntry<V> {
        private RBNode<V> root;

        TreeEntry(long key, V value) {
            root = new RBNode<>(key, value, null, true);
        }

        @Override
        protected V find(long key) {
            return find(this.root, key);
        }

        private V find(RBNode<V> node, long key) {
            if (node == null) {
                return null;
            }
            if (key < node.key) {
                return find(node.left, key);
            }
            if (node.key == key) {
                return node.value;
            }
            return find(node.right, key);
        }

        @Override
        protected V insert(LongObjectHashMap<V> table, long key, V value) {
            if (this.root == null) {
                root = new RBNode<>(key, value, null, true);
                return null;
            }
            return insert(root, table, key, value);
        }

        private V insert(RBNode<V> node, LongObjectHashMap<V> table, long key, V value) {
            if (node.key == key) {
                V oldValue = node.value;
                node.value = value;
                return oldValue;
            }
            if (key < node.key) {
                if (node.left != null) {
                    return insert(node.left, table, key, value);
                } else {
                    node.left = new RBNode<>(key, value, node, false);
                    table.incSize();
                    insert1(node.left);
                    return null;
                }
            } else {
                if (node.right != null) {
                    return insert(node.right, table, key, value);
                } else {
                    node.right = new RBNode<>(key, value, node, false);
                    table.incSize();
                    insert1(node.right);
                    return null;
                }
            }
        }

        // when inserted vertex is root
        private void insert1(RBNode<V> node) {
            if (node.isRoot()) {
                node.isBlack = true;
            } else {
                insert2(node);
            }
        }

        // if parent of inserted vertex is black all is right
        private void insert2(RBNode<V> node) {
            if (node.parent.isBlack) {
                return;
            }
            insert3(node);
        }

        // if parent is red and uncle is red
        private void insert3(RBNode<V> node) {
            if (node.getUncle() != null && !node.getUncle().isBlack && !node.parent.isBlack) {
                node.getUncle().isBlack = true;
                node.parent.isBlack = true;
                node.getGrandparent().isBlack = false;
                insert1(node.getGrandparent());
            } else {
                insert4(node);
            }
        }

        private void insert4(RBNode<V> node) {
            if (node == node.parent.right && node.parent == node.getGrandparent().left) {
                rotateLeft(node.parent);
                node = node.left;
            } else if (node == node.parent.left && node.parent == node.getGrandparent().right) {
                rotateRight(node.parent);
                node = node.right;
            }
            insert5(node);
        }

        private void insert5(RBNode<V> node) {
            node.parent.isBlack = true;
            RBNode<V> grandparent = node.getGrandparent();
            grandparent.isBlack = false;
            if (node.parent == grandparent.left) {
                rotateRight(grandparent);
            } else {
                rotateLeft(grandparent);
            }
        }

        @Override
        protected V remove(LongObjectHashMap<V> table, int i, long key) {
            if (root.key == key && root.left == null && root.right == null) {
                table.data[i] = null;
                return root.value;
            }
            return remove(root, table, key);
        }

        private V remove(RBNode<V> node, LongObjectHashMap<V> table, long key) {
            if (node.key == key) {
                V oldValue = node.value;
                remove(node);
                table.decSize();
                return oldValue;
            }
            if (key < node.key) {
                if (node.left == null) {
                    return null;
                }
                return remove(node.left, table, key);
            } else {
                if (node.right == null) {
                    return null;
                }
                return remove(node.right, table, key);
            }
        }

        private void remove(RBNode<V> node) {
            if (node.isLeaf()) {
                if (node.isLeftChild()) {
                    node.parent.left = null;
                } else {
                    node.parent.right = null;
                }
            }
            RBNode<V> child = node.right.isLeaf() ? node.left : node.right;
            replaceNode(node, child);
            if (node.isBlack) {
                if (child.isBlack) {
                    remove1(child);
                } else {
                    child.isBlack = true;
                }
            }
        }

        private void replaceNode(RBNode<V> toReplace, RBNode<V> withReplace) {
            if (toReplace.isRoot()) {
                root = withReplace;
                return;
            }
            if (toReplace.isLeftChild()) {
                toReplace.parent.left = withReplace;
            } else {
                toReplace.parent.right = withReplace;
            }
        }

        private void remove1(RBNode<V> node) {
            if (!node.isRoot()) {
                remove2(node);
            }
        }

        private void remove2(RBNode<V> node) {
            RBNode<V> sibling = node.getSibling();
            if (!sibling.isBlack) {
                node.parent.isBlack = false;
                sibling.isBlack = true;
                if (node.isLeftChild()) {
                    rotateLeft(node.parent);
                } else {
                    rotateRight(node.parent);
                }
            }
            remove3(node);
        }

        private void remove3(RBNode<V> node) {
            RBNode<V> sibling = node.getSibling();
            if (node.parent.isBlack && sibling.isBlack && sibling.left.isBlack && sibling.right.isBlack) {
                sibling.isBlack = false;
                remove1(node.parent);
            } else {
                remove4(node);
            }
        }

        private void remove4(RBNode<V> node) {
            RBNode<V> sibling = node.getSibling();
            if (!node.parent.isBlack && sibling.isBlack && sibling.left.isBlack && sibling.right.isBlack) {
                sibling.isBlack = false;
                node.parent.isBlack = true;
            } else {
                remove5(node);
            }
        }

        private void remove5(RBNode<V> node) {
            RBNode<V> sibling = node.getSibling();
            if  (sibling.isBlack) {
                if (node.isLeftChild() && sibling.right.isBlack && !sibling.left.isBlack) {
                    sibling.isBlack = false;
                    sibling.left.isBlack = true;
                    rotateRight(sibling);
                } else if (!node.isLeftChild() && sibling.left.isBlack && !sibling.right.isBlack) {
                    sibling.isBlack = false;
                    sibling.right.isBlack = true;
                    rotateLeft(sibling);
                }
            }
            remove6(node);
        }

        private void remove6(RBNode<V> node) {
            RBNode<V> sibling = node.getSibling();
            sibling.isBlack = node.parent.isBlack;
            node.parent.isBlack = true;
            if (node.isLeftChild()) {
                sibling.right.isBlack = true;
                rotateLeft(node.parent);
            } else {
                sibling.left.isBlack = true;
                rotateRight(node.parent);
            }
        }

        private void rotateRight(RBNode<V> node) {
            RBNode<V> newRoot = node.left;
            RBNode<V> rightNephew = node.left.right;
            newRoot.parent = node.parent;
            if (node.isRoot()) {
                this.root = newRoot;
            }
            node.parent = newRoot;
            node.left = rightNephew;
            newRoot.right = node;
        }

        private void rotateLeft(RBNode<V> node) {
            RBNode<V> newRoot = node.right;
            RBNode<V> leftNephew = node.right.left;
            newRoot.parent = node.parent;
            if (node.isRoot()) {
                this.root = newRoot;
            }
            node.parent = newRoot;
            node.right = leftNephew;
            newRoot.left = node;
        }

        @Override
        protected <T> void collect(Function<Entry<V>, T> f, Set<T> dest) {
            collect(f, dest, root);
        }

        private <T> void collect(Function<Entry<V>, T> f, Set<T> dest, RBNode<V> node) {
            if (node == null) {
                return;
            }
            dest.add(f.apply(node));
            collect(f, dest, node.left);
            collect(f, dest, node.right);
        }

        @Override
        protected void putRecursively(LongObjectHashMap<V> t) {
            putRecursively(t, root);
        }

        private void putRecursively(LongObjectHashMap<V> t, RBNode<V> node) {
            if (node == null) {
                return;
            }
            t.put(node.key, node.value);
            putRecursively(t, node.left);
            putRecursively(t, node.right);
        }

        @Override
        public long getKey() {
            throw new UnsupportedOperationException();
        }

        @Override
        public V getValue() {
            throw new UnsupportedOperationException();
        }

        static class RBNode<V> implements Entry<V> {
            private boolean isBlack;
            private RBNode<V> parent;
            private RBNode<V> left;
            private RBNode<V> right;
            private long key;
            private V value;

            public RBNode(long key, V value, RBNode<V> parent, boolean isBlack) {
                this.key = key;
                this.value = value;
                this.parent = parent;
                this.isBlack = isBlack;
            }

            public RBNode<V> getGrandparent() {
                if (this.parent != null && this.parent.parent != null) {
                    return this.parent.parent;
                }
                return null;
            }

            public RBNode<V> getUncle() {
                RBNode<V> grandparent = this.getGrandparent();
                if (grandparent == null) {
                    return null;
                }
                if (grandparent.left == this.parent) {
                    return grandparent.right;
                }
                return grandparent.left;
            }

            public RBNode<V> getSibling() {
                if (this == parent.left) {
                    return parent.right;
                } else {
                    return parent.left;
                }
            }

            public boolean isLeaf() {
                return left == null && right == null;
            }

            public boolean isRoot() {
                return parent == null;
            }

            public boolean isLeftChild() {
                return parent != null && parent.left == this;
            }

            @Override
            public long getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }
        }
    }
}
