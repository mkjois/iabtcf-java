package com.iabtcf.utils;

/*-
 * #%L
 * IAB TCF Java Decoder Library
 * %%
 * Copyright (C) 2020 - 2023 IAB Technology Laboratory, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UnsafeLeanBitSetTest {

    @Test
    public void testUnsafeLeanBitSetSingleOperations() {
        UnsafeLeanBitSet bs = new UnsafeLeanBitSet(0);
        assertTrue(bs.toBitSet().isEmpty());

        // BitSet will optimize to strip trailing unset bits.
        bs.ensureCapacity(1000);
        assertEquals(0, bs.toBitSet().toLongArray().length);
        bs.unsafeSet(999, true);
        assertEquals(16, bs.toBitSet().toLongArray().length);

        // Test single set
        bs = new UnsafeLeanBitSet(0);
        bs.ensureCapacity(1);
        bs.unsafeSet(0, true);
        assertTrue(bs.unsafeGet(0));
        assertEquals(1, bs.toBitSet().toLongArray().length);

        bs.ensureCapacity(64);
        bs.unsafeSet(63, true);
        assertTrue(bs.unsafeGet(63));
        assertFalse(bs.unsafeGet(62));
        bs.unsafeSet(63, true);
        assertTrue(bs.unsafeGet(63));
        assertFalse(bs.unsafeGet(62));
        assertEquals(1, bs.toBitSet().toLongArray().length);

        bs.ensureCapacity(65);
        bs.unsafeSet(64, true);
        assertTrue(bs.unsafeGet(64));
        assertTrue(bs.unsafeGet(63));
        assertFalse(bs.unsafeGet(62));
        assertEquals(2, bs.toBitSet().toLongArray().length);

        // Test single clear
        bs.unsafeSet(0, false);
        assertFalse(bs.unsafeGet(0));
        assertFalse(bs.unsafeGet(1));

        bs.unsafeSet(50, false);
        assertFalse(bs.unsafeGet(49));
        assertFalse(bs.unsafeGet(50));
        assertFalse(bs.unsafeGet(51));

        bs.unsafeSet(63, false);
        assertFalse(bs.unsafeGet(62));
        assertFalse(bs.unsafeGet(63));
        assertTrue(bs.unsafeGet(64));

        bs.unsafeSet(64, false);
        for (int i = 0; i < 65; i++) {
            assertFalse(bs.unsafeGet(i));
        }
    }

    @Test
    public void testUnsafeLeanBitSetBulkOperations() {
        UnsafeLeanBitSet bs = new UnsafeLeanBitSet(0);
        assertTrue(bs.toBitSet().isEmpty());

        // BitSet will optimize to strip trailing unset bits.
        bs.ensureCapacity(1000);
        bs.unsafeSet(2, 100, true);
        assertEquals(2, bs.toBitSet().toLongArray().length);
        for (int i = 0; i < 1000; i++) {
            if (i >= 2 && i < 100) {
                assertTrue(bs.unsafeGet(i));
            } else {
                assertFalse(bs.unsafeGet(i));
            }
        }

        bs.unsafeSet(60, 70, false);
        bs.unsafeSet(63, 65, true);
        bs.unsafeSet(40, false);
        bs.unsafeSet(61, true);
        for (int i = 0; i < 1000; i++) {
            if (i >= 2 && i < 100 && i != 40 && i != 60 && i != 62 && !(i >= 65 && i < 70)) {
                assertTrue(bs.unsafeGet(i));
            } else {
                assertFalse(bs.unsafeGet(i));
            }
        }
    }
}
