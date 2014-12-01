package atm.util;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class TreeEntryTest {

    @Test
    public void test_shouldFindInsertedValues() {
        LongObjectHashMap.TreeEntry<String> tree = new LongObjectHashMap.TreeEntry<>(0, 0 + "");
        LongObjectHashMap<String> mockTable = new LongObjectHashMap<>(0);
        for (int i = 1; i < 20; i++) {
            tree.insert(mockTable, i, i + "");
        }

        for (int i = 0; i < 20; i++) {
            assertEquals(String.valueOf(i), tree.find(i));
        }
    }

    @Test
    public void test_shouldReturnNullForRemoved() {
        LongObjectHashMap.TreeEntry<String> tree = new LongObjectHashMap.TreeEntry<>(0, 0 + "");
        LongObjectHashMap<String> mockTable = new LongObjectHashMap<>(1);
        for (int i = 1; i < 2000; i++) {
            tree.insert(mockTable, i, i + "");
        }
        for (int i = 0; i < 2000; i++) {
            if (i % 2 == 0) {
                tree.remove(mockTable, 0, i);
            }
            tree.insert(mockTable, i, i + "");
        }

        for (int i = 0; i < 2000; i++) {
            if (i % 2 == 0) {
                assertNull(tree.find(i));
            } else {
                assertEquals(String.valueOf(i), tree.find(i));
            }
        }
    }

    @Test
    public void testCollect() {
        LongObjectHashMap.TreeEntry<String> tree = new LongObjectHashMap.TreeEntry<>(0, 0 + "");
        LongObjectHashMap<String> mockTable = new LongObjectHashMap<>(1);
        Set<Long> exp = new HashSet<>();
        exp.add(0L);
        for (long i = 0; i < 20; i++) {
            tree.insert(mockTable, i, i + "");
            exp.add(i);
        }
        Set<Long> act = new HashSet<>();

        tree.collect(LongObjectHashMap.Entry::getKey, act);

        assertEquals(exp.size(), act.size());
        exp.forEach((e) -> assertTrue(act.contains(e)));
    }

    @Test
    public void testPutRecursively() {
        LongObjectHashMap.TreeEntry<String> tree = new LongObjectHashMap.TreeEntry<>(0, 0 + "");
        LongObjectHashMap<String> mockTable = new LongObjectHashMap<>(1);
        for (int i = 1; i < 20; i++) {
            tree.insert(mockTable, i, i + "");
        }
        mockTable.clear();

        tree.putRecursively(mockTable);
        for (int i = 0; i < 20; i++) {
            assertEquals(String.valueOf(i), mockTable.get(i));
        }
    }
}