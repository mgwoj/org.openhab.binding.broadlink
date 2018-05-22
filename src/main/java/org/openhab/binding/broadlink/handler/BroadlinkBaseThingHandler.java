/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.IvParameterSpec;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.broadlink.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.Hex;
import org.openhab.binding.broadlink.internal.Utils;
import org.openhab.binding.broadlink.internal.config.BroadlinkDeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 * 
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkBaseThingHandler extends BaseThingHandler {
    public static final Set SUPPORTED_THING_TYPES;
    private static Logger logger;
    private static DatagramSocket socket;
    int count = 0;
    String authenticationKey;
    String iv;
    static Boolean commandRunning;
    public BroadlinkDeviceConfiguration thingConfig;

    static {
        SUPPORTED_THING_TYPES = new HashSet(Arrays.asList(BroadlinkBindingConstants.THING_TYPE_A1,
                BroadlinkBindingConstants.THING_TYPE_RM, BroadlinkBindingConstants.THING_TYPE_RM2,
                BroadlinkBindingConstants.THING_TYPE_RM3, BroadlinkBindingConstants.THING_TYPE_SP1,
                BroadlinkBindingConstants.THING_TYPE_SP2, BroadlinkBindingConstants.THING_TYPE_MP1,
                BroadlinkBindingConstants.THING_TYPE_MP2, BroadlinkBindingConstants.THING_TYPE_SP3));
        logger = LoggerFactory.getLogger(BroadlinkBaseThingHandler.class);
        socket = null;
        commandRunning = false;
    }

    public BroadlinkBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing Broadlink device handler '{}'", this.getThing().getUID());
        }

        this.count = (new Random()).nextInt(65535);
        this.thingConfig = this.getConfigAs(BroadlinkDeviceConfiguration.class);
        if (this.iv != this.thingConfig.getIV() || this.authenticationKey != this.thingConfig.getAuthorizationKey()) {
            this.iv = this.thingConfig.getIV();
            this.authenticationKey = this.thingConfig.getAuthorizationKey();
            final Map<String, String> properties = (Map<String, String>)this.editProperties();
            properties.put("id", null);
            properties.put("key", null);
            this.updateProperties(properties);
            if (this.authenticate()) {
                this.updateStatus(ThingStatus.ONLINE);
            } else {
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }

        if (this.thingConfig.getPollingInterval() != 0) {
            this.scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    BroadlinkBaseThingHandler.this.updateItemStatus();
                }
            }, 1L, this.thingConfig.getPollingInterval(), TimeUnit.SECONDS);
        }

    }

    @Override
    public void thingUpdated(Thing thing) {
        if (this.iv != this.thingConfig.getIV() || this.authenticationKey != this.thingConfig.getAuthorizationKey()) {
            this.iv = this.thingConfig.getIV();
            this.authenticationKey = this.thingConfig.getAuthorizationKey();
            if (this.authenticate()) {
                this.updateStatus(ThingStatus.ONLINE);
            } else {
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }

        this.updateItemStatus();
    }

    @Override
    public void dispose() {
        logger.error("'{}' is being disposed", this.getThing().getLabel());
        super.dispose();
    }

    private boolean authenticate() {
        final byte[] payload = { 0, 0, 0, 0, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 84, 101, 115, 116, 32, 32, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
  
        if (!this.sendDatagram(this.buildMessage((byte) 101, payload))) {
            logger.debug("Authenticated device '{}' failed.", this.getThing().getUID());
            return false;
        } else {
            byte[] response = this.receiveDatagram();
            if (response == null) {
                logger.debug("Authenticated device '{}' failed.", this.getThing().getUID());
                return false;
            } else {
                int error = response[34] | response[35] << 8;
                if (error != 0) {
                    logger.debug("Authenticated device '{}' failed.", this.getThing().getUID());
                    return false;
                } else {
                    final byte[] decryptResponse = Utils.decrypt(Hex.convertHexToBytes(this.authenticationKey),
                            new IvParameterSpec(Hex.convertHexToBytes(this.iv)), Utils.slice(response, 56, 88));
                    final byte[] deviceId = Utils.getDeviceId(decryptResponse);
                    final byte[] deviceKey = Utils.getDeviceKey(decryptResponse);
                    final Map<String, String> properties = (Map<String, String>)this.editProperties();
                    properties.put("key", Hex.toHexString(deviceKey));
                    properties.put("id", Hex.toHexString(deviceId));
                    this.updateProperties(properties);
                    this.thingConfig = this.getConfigAs(BroadlinkDeviceConfiguration.class);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Authenticated device '{}' with id '{}' and key '{}'.", new Object[] {
                                this.getThing().getUID(), Hex.toHexString(deviceId), Hex.toHexString(deviceKey) });
                    }

                    return true;
                }
            }
        }
    }

    public boolean sendDatagram(byte[] message) {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
            }

            InetAddress host = InetAddress.getByName(this.thingConfig.getIpAddress());
            int port = this.thingConfig.getPort();
            DatagramPacket sendPacket = new DatagramPacket(message, message.length, new InetSocketAddress(host, port));
            commandRunning = true;
            socket.send(sendPacket);
            return true;
        } catch (IOException var5) {
            logger.error("IO error for device '{}' during UDP command sending: {}", this.getThing().getUID(),
                    var5.getMessage());
            return false;
        }
    }

    public byte[] receiveDatagram() {
        try {
            socket.setReuseAddress(true);
            socket.setSoTimeout(5000);
            if (commandRunning) {
                byte[] response = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(response, response.length);
                socket.receive(receivePacket);
                response = receivePacket.getData();
                byte[] var4 = response;
                return var4;
            }
        } catch (SocketTimeoutException var15) {
            if (logger.isDebugEnabled()) {
                logger.debug("No further response received for device '{}'", this.getThing().getUID());
            }

            commandRunning = false;
            return null;
        } catch (IOException var16) {
            logger.error("IO error: Broadlink Device: {}", var16.getMessage());
            commandRunning = false;
        } finally {
            try {
                if (socket != null) {
                    commandRunning = false;
                    socket.close();
                }
            } catch (Exception var14) {
                logger.error("IO Exception: '{}", var14.getMessage());
                commandRunning = false;
            }

        }

        return null;
    }

    protected byte[] buildMessage(byte command, byte[] payload) {
        this.count = this.count + 1 & '\uffff';
        byte[] packet = new byte[56];
        byte[] mac = this.thingConfig.getMAC();
        Map properties = this.editProperties();
        byte[] id;
        if (properties.get("id") == null) {
            id = new byte[4];
        } else {
            id = Hex.fromHexString((String) properties.get("id"));
        }

        packet[0] = 90;
        packet[1] = -91;
        packet[2] = -86;
        packet[3] = 85;
        packet[4] = 90;
        packet[5] = -91;
        packet[6] = -86;
        packet[7] = 85;
        packet[36] = 42;
        packet[37] = 39;
        packet[38] = command;
        packet[40] = (byte) (this.count & 255);
        packet[41] = (byte) (this.count >> 8);
        packet[42] = mac[0];
        packet[43] = mac[1];
        packet[44] = mac[2];
        packet[45] = mac[3];
        packet[46] = mac[4];
        packet[47] = mac[5];
        packet[48] = id[0];
        packet[49] = id[1];
        packet[50] = id[2];
        packet[51] = id[3];
        int checksum = 48815;
        byte[] var12 = payload;
        int var11 = payload.length;

        int i = 0;
        for (int var10 = 0; var10 < var11; ++var10) {
            byte b = var12[var10];
            i = Byte.toUnsignedInt(b);
            checksum += i;
            checksum &= 65535;
        }

        packet[52] = (byte) (checksum & 255);
        packet[53] = (byte) (checksum >> 8);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            outputStream.write(packet);
            if (properties.get("key") != null && properties.get("id") != null) {
                outputStream.write(Utils.encrypt(Hex.fromHexString((String) properties.get("key")),
                        new IvParameterSpec(Hex.convertHexToBytes(this.thingConfig.getIV())), payload));
            } else {
                outputStream.write(Utils.encrypt(Hex.convertHexToBytes(this.thingConfig.getAuthorizationKey()),
                        new IvParameterSpec(Hex.convertHexToBytes(this.thingConfig.getIV())), payload));
            }
        } catch (IOException var15) {
            // var15.printStackTrace();
            return null;
        }

        byte[] data = outputStream.toByteArray();
        checksum = 48815;
        byte[] var14 = data;
        int var13 = data.length;

        for (int var20 = 0; var20 < var13; ++var20) {
            byte b = var14[var20];
            i = Byte.toUnsignedInt(b);
            checksum += i;
            checksum &= 65535;
        }

        data[32] = (byte) (checksum & 255);
        data[33] = (byte) (checksum >> 8);
        return data;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            this.updateItemStatus();
        }

    }

    public void updateItemStatus() {
        if (hostAvailabilityCheck(this.thingConfig.getIpAddress(), 3000)) {
            if (!this.isOnline()) {
                this.updateStatus(ThingStatus.ONLINE);
            }
        } else if (!this.isOffline()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not control device at IP address " + this.thingConfig.getIpAddress());
        }

    }

    protected static boolean hostAvailabilityCheck(String host, int timeout) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(timeout);
        } catch (Exception var3) {
            logger.error("Host is not reachable: {}", var3.getMessage());
            return false;
        }
    }

    protected boolean isOnline() {
        return this.thing.getStatus().equals(ThingStatus.ONLINE);
    }

    protected boolean isOffline() {
        return this.thing.getStatus().equals(ThingStatus.OFFLINE);
    }
}
