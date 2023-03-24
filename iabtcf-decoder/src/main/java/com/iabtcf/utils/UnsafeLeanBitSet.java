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

import java.util.Arrays;
import java.util.BitSet;

/**
 * This class provides some basic BitSet functionality similar to the one in standard library.
 * If fact, much of the code here is analogous or identical to the standard library code.
 *
 * However, this class does not perform nearly as much invariant checking on every operation.
 * It is up to the user to ensure the underlying storage has enough capacity by either
 * initializing with a fixed length and never indexing beyond that, or by calling the
 * provided method(s) to ensure capacity for the maximum expected number of bits to work with.
 *
 * As a result, this performs decently faster for the required operations at high scale
 * when compared to the standard library's BitSet. Needless to say, this is intended only for
 * judicious use within this module. Proceed with extreme caution.
 */
public class UnsafeLeanBitSet {

    private static final long WORD_MASK = 0xffffffffffffffffL;

    private long[] words;

    public UnsafeLeanBitSet(final int nbits) {
        if (nbits < 0) {
            throw new NegativeArraySizeException("nbits < 0: " + nbits);
        }
        words = new long[wordIndex(nbits) + (bitIndex(nbits) == 0 ? 0 : 1)];
    }

    public BitSet toBitSet() {
        return BitSet.valueOf(words);
    }

    private static int wordIndex(final int bit) {
        return bit >> 6;
    }

    private static int bitIndex(final int bit) {
        return bit & 0x3f;
    }

    public void ensureCapacity(final int nbits) {
        final int wordsRequired = 1 + wordIndex(nbits - 1);
        if (words.length < wordsRequired) {
            words = Arrays.copyOf(words, Math.max(2 * words.length, wordsRequired));
        }
    }

    public boolean unsafeGet(final int bit) {
        final int wordIndex = wordIndex(bit);
        final int bitIndex = bitIndex(bit);
        return ((words[wordIndex] >>> bitIndex) & 1) == 1;
    }

    public void unsafeSet(final int bit, final boolean flag) {
        final int wordIndex = wordIndex(bit);
        final int bitIndex = bitIndex(bit);
        words[wordIndex] = flag
                ? words[wordIndex] | (1L << bitIndex)
                : words[wordIndex] & (~(1L << bitIndex));
    }

    public void unsafeSet(final int startBit, final int endBit, final boolean flag) {
        if (endBit <= startBit) {
            return;
        }

        final int startWordIndex = wordIndex(startBit);
        final int endWordIndex = wordIndex(endBit - 1);
        final long firstWordMask = WORD_MASK << startBit;
        final long lastWordMask  = WORD_MASK >>> -endBit;

        // Mostly taken verbatim from BitSet standard library code.
        if (flag) {
            if (startWordIndex == endWordIndex) {
                words[startWordIndex] |= (firstWordMask & lastWordMask);
            } else {
                words[startWordIndex] |= firstWordMask;
                for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                    words[i] = WORD_MASK;
                }
                words[endWordIndex] |= lastWordMask;
            }

        } else {
            if (startWordIndex == endWordIndex) {
                words[startWordIndex] &= ~(firstWordMask & lastWordMask);
            } else {
                words[startWordIndex] &= ~firstWordMask;
                for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                    words[i] = 0;
                }
                words[endWordIndex] &= ~lastWordMask;
            }
        }
    }
}
