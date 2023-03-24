package com.iabtcf.utils;

/*-
 * #%L
 * IAB TCF Core Library
 * %%
 * Copyright (C) 2020 IAB Technology Laboratory, Inc
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

import com.iabtcf.utils.LengthOffsetCache.LeanEnumArrayIntMap;

public class LengthOffsetCacheTest {

    private final FieldDefs[] fd = FieldDefs.values();

    @Test
    public void testLeanEnumArrayIntMap() {
        final LeanEnumArrayIntMap<FieldDefs> map = new LeanEnumArrayIntMap<>(FieldDefs.CORE_VERSION);
        for (int i = 0; i < fd.length; i++) {
            assertFalse(map.contains(fd[i]));
        }

        for (int i = 0; i < fd.length; i++) {
            map.put(fd[i], 99999);
            for (int j = 0; j < fd.length; j++) {
                if (j == i) {
                    assertTrue(map.contains(fd[j]));
                    assertEquals(99999, map.get(fd[j]));
                } else {
                    assertFalse(map.contains(fd[j]));
                }
            }

            map.remove(fd[i]);
            for (int j = 0; j < fd.length; j++) {
                assertFalse(map.contains(fd[j]));
            }
        }
    }
}
