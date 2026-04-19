/*
 * Copyright (c) 2026. Tim Langhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.jpangolin.friendlyimp.ioimp;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Helper class for all kind of digest related compuations.
 *
 * @author jTzipi
 */
public final class DigestImp {

    // LOG
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DigestImp.class);
    private static final String DEFAULT_MESSAGE_DIGEST_ALGO = MessageDigestAlgorithms.SHA_512_256;

    private DigestImp() {
        throw new AssertionError("__!?!__");
    }


    /// calculated the message digest of the byte array unsing {@link DigestUtils}.
    /// Hint: _LibACCodec_ refers to Apache Common Codec
    ///
    /// @param bytes the bytes to calculate
    /// @throws NullPointerException if {@code bytes}
    public static String digestAsHexLibACCodec(final byte[] bytes) {
        Objects.requireNonNull(bytes);

        String hex = new DigestUtils(DEFAULT_MESSAGE_DIGEST_ALGO).digestAsHex(bytes);
        LOG.info("<<digestAsHexLibCodec>> Digest hex ='{}'", hex);
        return hex;
    }
}