/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.internal;

import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Cato Sognen - Initial contribution
 */
public class Hex {
    public static String decodeMAC(final byte[] mac) {
        if (mac == null) {
            return null;
        } else {
            final StringBuilder sb = new StringBuilder(18);

            for (int i = 5; i >= 0; --i) {
                if (sb.length() > 0) {
                    sb.append(':');
                }

                sb.append(String.format("%02x", mac[i]));
            }

            return sb.toString();
        }
    }

    public static boolean isHexCode(final String code) {
        final Pattern pattern = Pattern.compile("0000( +[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f])+");
        return pattern.matcher(code).find();
    }

    public static byte[] convertHexToBytes(final String code) {
        return DatatypeConverter.parseHexBinary(code);
    }

    public static byte[] fromHexString(final String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters");
        } else {
            final byte[] result = new byte[hex.length() / 2];
            final char[] bytes = hex.toCharArray();

            for (int i = 0; i < bytes.length; i += 2) {
                final StringBuilder curr = new StringBuilder(2);
                curr.append(bytes[i]).append(bytes[i + 1]);
                result[i / 2] = (byte) Integer.parseInt(curr.toString(), 16);
            }

            return result;
        }
    }

    public static String toHexString(final byte[] raw) {
        String HEXES = "0123456789ABCDEF";
        if (raw == null) {
            return null;
        } else {
            final StringBuilder hex = new StringBuilder(2 * raw.length);
            for (final byte b : raw) {
                hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt(b & 0xF));
            }
            return hex.toString();
        }
    }
}
