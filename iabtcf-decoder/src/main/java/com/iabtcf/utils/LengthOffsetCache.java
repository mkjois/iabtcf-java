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

import java.util.function.ToIntFunction;

class LengthOffsetCache {
    private final BitReader bbv;
    private final LeanEnumArrayIntMap<FieldDefs> lengthCache = new LeanEnumArrayIntMap<>(FieldDefs.CORE_VERSION);
    private final LeanEnumArrayIntMap<FieldDefs> offsetCache = new LeanEnumArrayIntMap<>(FieldDefs.CORE_VERSION);

    public LengthOffsetCache(BitReader bbv) {
        this.bbv = bbv;
    }

    public int getLength(FieldDefs field, ToIntFunction<BitReader> f) {
        return memoize(field, lengthCache, f);
    }

    public int getOffset(FieldDefs field, ToIntFunction<BitReader> f) {
        return memoize(field, offsetCache, f);
    }

    private int memoize(FieldDefs field, LeanEnumArrayIntMap<FieldDefs> cache, ToIntFunction<BitReader> f) {
        if (!field.isDynamic()) {
            return f.applyAsInt(bbv);
        }
        if (cache.contains(field)) {
            return cache.get(field);
        }
        int rv = f.applyAsInt(bbv);
        cache.put(field, rv);
        return rv;
    }

    /**
     * A leaner enum to primitive integer map with minimal functionality and invariant checks.
     * Users MUST always check for existence with .contains() before trusting the value returned by .get(),
     * otherwise the value returned by .get() will be GARBAGE.
     * @param <K> Enum type
     */
    static class LeanEnumArrayIntMap<K extends Enum<K>> {
        private final UnsafeLeanBitSet existence;
        private final int[] values;

        LeanEnumArrayIntMap(final K example) {
            final K[] constants = example.getDeclaringClass().getEnumConstants();
            this.existence = new UnsafeLeanBitSet(constants.length);
            this.values = new int[constants.length];
        }

        boolean contains(final K key) {
            return existence.unsafeGet(key.ordinal());
        }

        int get(final K key) {
            return values[key.ordinal()];
        }

        void put(final K key, final int value) {
            values[key.ordinal()] = value;
            existence.unsafeSet(key.ordinal(), true);
        }

        void remove(final K key) {
            existence.unsafeSet(key.ordinal(), false);
        }
    }
}
