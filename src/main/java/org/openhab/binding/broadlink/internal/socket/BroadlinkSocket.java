/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.internal.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.broadlink.internal.Hex;
import org.openhab.binding.broadlink.internal.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkSocket {
    private static final int BUFFER_LENGTH = 1024;
    private static byte[] buffer = new byte[1024];
    private static DatagramPacket datagramPacket;
    private static MulticastSocket socket;
    private static Thread socketReceiveThread;
    private static List listeners;
    private static Logger logger;

    static {
        datagramPacket = new DatagramPacket(buffer, buffer.length);
        socket = null;
        listeners = new ArrayList();
        logger = LoggerFactory.getLogger(BroadlinkSocket.class);
    }

    public static void registerListener(BroadlinkSocketListener listener) {
        listeners.add(listener);
        if (socket == null) {
            setupSocket();
        }

    }

    public static void unregisterListener(BroadlinkSocketListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty() && socket != null) {
            closeSocket();
        }

    }

    private static void setupSocket() {
        Class var0 = BroadlinkSocket.class;
        synchronized (BroadlinkSocket.class) {
            try {
                socket = new MulticastSocket();
            } catch (IOException var2) {
                logger.error("Setup socket error '{}'.", var2.getMessage());
            }

            socketReceiveThread = new BroadlinkSocket.ReceiverThread((BroadlinkSocket.ReceiverThread) null);
            socketReceiveThread.start();
        }
    }

    private static void closeSocket() {
        Class var0 = BroadlinkSocket.class;
        synchronized (BroadlinkSocket.class) {
            if (socketReceiveThread != null) {
                socketReceiveThread.interrupt();
            }

            if (socket != null) {
                logger.info("Socket closed");
                socket.close();
                socket = null;
            }

        }
    }

    public static void sendMessage(byte[] message) {
        sendMessage(message, "255.255.255.255", 80);
    }

    public static void sendMessage(byte[] message, String host, int port) {
        try {
            DatagramPacket sendPacket = new DatagramPacket(message, message.length, InetAddress.getByName(host), port);
            socket.send(sendPacket);
        } catch (IOException var4) {
            logger.error("IO Error sending message: '{}'", var4.getMessage());
        }

    }

    /**


     *
     * @author Cato Sognen - Initial contribution
     */
    private static class ReceiverThread extends Thread {
        private ReceiverThread() {
        }

        @Override
        public void run() {
            this.receiveData(BroadlinkSocket.socket, BroadlinkSocket.datagramPacket);
        }

        private void receiveData(MulticastSocket socket, DatagramPacket dgram) {
            try {
                while (true) {
                    socket.receive(dgram);
                    Iterator var4 = (new ArrayList(BroadlinkSocket.listeners)).iterator();

                    while (var4.hasNext()) {
                        BroadlinkSocketListener listener = (BroadlinkSocketListener) var4.next();
                        byte[] receivedPacket = dgram.getData();
                        byte[] remoteMAC = Arrays.copyOfRange(receivedPacket, 58, 64);
                        int model = Byte.toUnsignedInt(receivedPacket[52])
                                | Byte.toUnsignedInt(receivedPacket[53]) << 8;
                        ThingTypeUID deviceType = ModelMapper.getThingType(model);
                        listener.onDataReceived(dgram.getAddress().getHostAddress(), dgram.getPort(),
                                Hex.decodeMAC(remoteMAC), deviceType);
                    }
                }
            } catch (IOException var9) {
                if (!this.isInterrupted()) {
                    BroadlinkSocket.logger.error("Error while receiving '{}'", var9);
                }

                BroadlinkSocket.logger.info("Receiver thread ended");
            }
        }

        // $FF: synthetic method
        ReceiverThread(BroadlinkSocket.ReceiverThread var1) {
            this();
        }
    }
}
