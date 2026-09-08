/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.efaps.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignUtil
{

    private static final Logger LOG = LoggerFactory.getLogger(SignUtil.class);
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static SignUtil INST;

    private String privateKey;

    private SignUtil()
    {
        init();
    }

    private void init()
    {
        final var config = ConfigProvider.getConfig();
        privateKey = config.getOptionalValue("backend.privateKey", String.class).orElse(null);
    }

    private String signInternal(final Object... values)
        throws NoSuchAlgorithmException, InvalidKeyException
    {

        final var bldr = new StringBuilder();
        for (final var value : values) {
            if (!bldr.isEmpty()) {
                bldr.append("|");
            }
            bldr.append(String.format("%s", value));
        }
        final String toSign = bldr.toString();
        LOG.info("toSign: {}", bldr.toString());
        final var key = Base64.getDecoder().decode(privateKey);

        final var sha512Hmac = Mac.getInstance(HMAC_SHA256);
        final SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_SHA256);
        sha512Hmac.init(keySpec);
        final byte[] macData = sha512Hmac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));

        final var signature = Base64.getUrlEncoder().withoutPadding().encodeToString(macData);
        LOG.info("signature: {}", signature);
        return signature;
    }

    public static String sign(final Object... values)
        throws InvalidKeyException, NoSuchAlgorithmException
    {
        if (INST == null) {
            INST = new SignUtil();
        }
        return INST.signInternal(values);
    }
}
