/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.internal;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**


 *
 * @author Cato Sognen - Initial contribution
 */
public class Utils {
    public static byte[] getDeviceId(byte[] response) {
        return slice(response, 0, 4);
    }

    public static byte[] getDeviceKey(byte[] response) {
        return slice(response, 4, 20);
    }

    public static byte[] slice(byte[] source, int from, int to) {
        if (from > to) {
            return null;
        } else if (to - from > source.length) {
            return null;
        } else {
            byte[] sliced;
            if (to == from) {
                sliced = new byte[] { source[from] };
                return sliced;
            } else {
                sliced = new byte[to - from];
                System.arraycopy(source, from, sliced, 0, to - from);
                return sliced;
            }
        }
    }

    public static byte[] encrypt(byte[] key, IvParameterSpec ivSpec, byte[] data) {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(1, secretKey, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception var6) {
            // var6.printStackTrace();
            return null;
        }
    }

    public static byte[] decrypt(byte[] key, IvParameterSpec ivSpec, byte[] data) {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(2, secretKey, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception var6) {
            // var6.printStackTrace();
            return null;
        }
    }
}
