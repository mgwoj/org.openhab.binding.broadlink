/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.internal.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.broadlink.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.socket.BroadlinkSocket;
import org.openhab.binding.broadlink.internal.socket.BroadlinkSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkDiscoveryService extends AbstractDiscoveryService implements BroadlinkSocketListener {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES;
    private final Logger logger = LoggerFactory.getLogger(BroadlinkDiscoveryService.class);

    static {
        SUPPORTED_THING_TYPES = BroadlinkBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    public BroadlinkDiscoveryService() {
        super(SUPPORTED_THING_TYPES, 10, true);
    }

    @Override
    public void startScan() {
        BroadlinkSocket.registerListener(this);
        this.discoverDevices();
        this.waitUntilEnded();
        BroadlinkSocket.unregisterListener(this);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        this.removeOlderResults(this.getTimestampOfLastScan());
    }

    private void waitUntilEnded() {
        final Semaphore discoveryEndedLock = new Semaphore(0);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                discoveryEndedLock.release();
            }
        }, 10L, TimeUnit.SECONDS);

        try {
            discoveryEndedLock.acquire();
        } catch (InterruptedException e) {
            this.logger.error("Discovery problem {}", e.getMessage());
        }

    }

    @Override
    public void onDataReceived(final String remoteAddress, final int remotePort, final String remoteMAC,
            final ThingTypeUID thingTypeUID) {
        this.discoveryResultSubmission(remoteAddress, remotePort, remoteMAC, thingTypeUID);
    }

    private void discoveryResultSubmission(final String remoteAddress, final int remotePort, final String remoteMAC,
            final ThingTypeUID thingTypeUID) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Adding new Broadlink device on {} with mac '{}' to Smarthome inbox", remoteAddress,
                    remoteMAC);
        }

        final Map<String, Object> properties = new HashMap<String, Object>(6);
        properties.put("ipAddress", remoteAddress);
        properties.put("port", remotePort);
        properties.put("mac", remoteMAC);
        final ThingUID thingUID = new ThingUID(thingTypeUID, remoteMAC.replace(":", "-"));
        if (thingUID != null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Device '{}' discovered on '{}'.", thingUID, remoteAddress);
            }

            DiscoveryResult result;
            if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_RM) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                        .withLabel("Broadlink RM [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            } else if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_RM2) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                        .withLabel("Broadlink RM2 [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            } else if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_RM3) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                        .withLabel("Broadlink RM3 [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            } else if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_A1) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                        .withLabel("Broadlink A1 [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            } else if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_SP1) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                        .withLabel("SP1 [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            } else if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_SP2) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                        .withLabel("SP2 [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            } else if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_SP3) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                        .withLabel("SP3 [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            } else if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_MP1) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                        .withLabel("Broadlink MP1 [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            } else if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_MP2) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID).withProperties(properties)
                        .withLabel("Broadlink MP2 [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            } else if (thingTypeUID == BroadlinkBindingConstants.THING_TYPE_S1C) {
                result = DiscoveryResultBuilder.create(thingUID).withThingType(BroadlinkBindingConstants.THING_TYPE_S1C)
                        .withProperties(properties).withLabel("Smart One Controller [" + remoteAddress + "]").build();
                this.thingDiscovered(result);
            }
        }

    }

    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            final Enumeration<?> ifaces = NetworkInterface.getNetworkInterfaces();

            while (ifaces.hasMoreElements()) {
                final NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                final Enumeration<?> inetAddrs = iface.getInetAddresses();

                while (inetAddrs.hasMoreElements()) {
                    final InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        }

                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }

            if (candidateAddress != null) {
                return candidateAddress;
            } else {
                final InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
                if (jdkSuppliedAddress == null) {
                    throw new UnknownHostException(
                            "The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
                } else {
                    return jdkSuppliedAddress;
                }
            }
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    "Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    private void discoverDevices() {
        try {
            final InetAddress localAddress = getLocalHostLANAddress();
            final int localPort = this.nextFreePort(localAddress, 1024, 3000);
            final byte[] message = this.buildDisoveryPacket(localAddress.getHostAddress(), localPort);
            BroadlinkSocket.sendMessage(message, "255.255.255.255", 80);
        } catch (UnknownHostException e) {
            // e.printStackTrace();
        }

    }

    public int nextFreePort(final InetAddress host, final int from, final int to) {
        int port;
        for (port = randInt(from, to); !this.isLocalPortFree(host, port); port = ThreadLocalRandom.current()
                .nextInt(from, to)) {
            ;
        }

        return port;
    }

    private boolean isLocalPortFree(InetAddress host, int port) {
        try {
            new ServerSocket(port, 50, host).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static int randInt(int min, int max) {
        int randomNum = ThreadLocalRandom.current().nextInt(max - min + 1) + min;
        return randomNum;
    }

    private byte[] buildDisoveryPacket(final String host, final int port) {
        String[] localAddress = null;
        localAddress = host.toString().split("\\.");
        int[] ipAddress = new int[4];

        for (int i = 0; i < 4; ++i) {
            ipAddress[i] = Integer.parseInt(localAddress[i]);
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(2);
        final TimeZone timeZone = TimeZone.getDefault();
        final int timezone = timeZone.getRawOffset() / 3600000;
        final byte[] packet = new byte[48];
        if (timezone < 0) {
            packet[8] = (byte) (255 + timezone - 1);
            packet[9] = -1;
            packet[10] = -1;
            packet[11] = -1;
        } else {
            packet[8] = 8;
            packet[9] = 0;
            packet[10] = 0;
            packet[11] = 0;
        }

        packet[12] = (byte) (calendar.get(1) & 0xFF);
        packet[13] = (byte) (calendar.get(1) >> 8);
        packet[14] = (byte) calendar.get(12);
        packet[15] = (byte) calendar.get(11);
        packet[16] = (byte) (calendar.get(1) - 2000);
        packet[17] = (byte) (calendar.get(7) + 1);
        packet[18] = (byte) calendar.get(5);
        packet[19] = (byte) (calendar.get(2) + 1);
        packet[24] = (byte) ipAddress[0];
        packet[25] = (byte) ipAddress[1];
        packet[26] = (byte) ipAddress[2];
        packet[27] = (byte) ipAddress[3];
        packet[28] = (byte) (port & 255);
        packet[29] = (byte) (port >> 8);
        packet[38] = 6;
        int checksum = 48815;

        for (int k = 0; k < packet.length; ++k) {
            byte b = packet[k];
            checksum += Byte.toUnsignedInt(b);
        }

        checksum &= 65535;
        packet[32] = (byte) (checksum & 0xFF);
        packet[33] = (byte) (checksum >> 8);
        return packet;
    }
}
