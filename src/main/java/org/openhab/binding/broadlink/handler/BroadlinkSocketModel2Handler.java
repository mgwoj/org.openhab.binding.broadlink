/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.handler;

import java.util.Map;

import javax.crypto.spec.IvParameterSpec;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.broadlink.internal.Hex;
import org.openhab.binding.broadlink.internal.Utils;

/**
 *
 *
 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkSocketModel2Handler extends BroadlinkSocketHandler {
    boolean powerON;

    public BroadlinkSocketModel2Handler(final Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (channelUID.getId().equals("powerOn")) {
            if (command == OnOffType.ON) {
                this.setStatusOnDevice(1);
            } else if (command == OnOffType.OFF) {
                this.setStatusOnDevice(0);
            }
        }

    }

    private void setStatusOnDevice(final int status) {
        byte[] payload = new byte[16];
        payload[0] = 2;
        payload[4] = (byte) status;
        final byte[] message = this.buildMessage((byte) 106, payload);
        this.sendDatagram(message);
    }

    public boolean getStatusFromDevice() {
        final byte[] payload = new byte[16];
        payload[0] = 1;

        try {
            final byte[] message = this.buildMessage((byte) 106, payload);
            this.sendDatagram(message);
            final byte[] response = this.receiveDatagram();
            if (response != null) {
                int error = response[34] | response[35] << 8;
                if (error == 0) {
                    final IvParameterSpec ivSpec = new IvParameterSpec(Hex.convertHexToBytes(this.thingConfig.getIV()));
                    Map<String, String> properties = this.editProperties();
                    final byte[] decodedPayload = Utils.decrypt(Hex.fromHexString(properties.get("key")), ivSpec,
                            Utils.slice(response, 56, 88));
                    if (decodedPayload != null) {
                        if (payload[4] == 1) {
                            this.updateState("powerOn", OnOffType.ON);
                        } else {
                            this.updateState("powerOn", OnOffType.OFF);
                        }

                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateItemStatus() {
        if (this.getStatusFromDevice()) {
            if (!this.isOnline()) {
                this.updateStatus(ThingStatus.ONLINE);
            }
        } else if (!this.isOffline()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not control device at IP address " + this.thingConfig.getIpAddress());
        }

    }
}
