/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.internal.config;

/**
 *
 * @author Cato - Initial contribution
 */
public class BroadlinkDeviceConfiguration {
    private String ipAddress;
    private int port;
    private String mac;
    private int pollingInterval = 30;
    private String mapFilename;
    private String authorizationKey;
    private String iv;

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public void setMAC(final String mac) {
        this.mac = mac;
    }

    public byte[] getMAC() {
        final byte[] configMac = new byte[6];
        final String[] elements = this.mac.split(":");

        for (int i = 0; i < 6; ++i) {
            final String element = elements[i];
            configMac[i] = (byte) Integer.parseInt(element, 16);
        }

        return configMac;
    }

    public int getPollingInterval() {
        return this.pollingInterval;
    }

    public void setPollingInterval(final int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getMapFilename() {
        return this.mapFilename;
    }

    public void setMapFilename(final String mapFilename) {
        this.mapFilename = mapFilename;
    }

    public String getAuthorizationKey() {
        return this.authorizationKey;
    }

    public void setAuthorizationKey(final String authorizationKey) {
        this.authorizationKey = authorizationKey;
    }

    public String getIV() {
        return this.iv;
    }

    public void setIV(final String iv) {
        this.iv = iv;
    }

    @Override
    public String toString() {
        return "BroadlinkDeviceConfiguration [ipAddress=" + this.ipAddress + ", port=" + this.port + ", mac=" + this.mac
                + ", pollingInterval=" + this.pollingInterval + ", mapFilename=" + this.mapFilename
                + ", authorizationKey=" + this.authorizationKey + ", iv=" + this.iv + "]";
    }
}
