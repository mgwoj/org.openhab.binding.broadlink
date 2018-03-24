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
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.broadlink.internal.Hex;
import org.openhab.binding.broadlink.internal.Utils;

/**


 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkStripModel1Handler extends BroadlinkBaseThingHandler {
    public BroadlinkStripModel1Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            this.updateItemStatus();
        }

        if (channelUID.getId().equals("s1powerOn")) {
            if (command == OnOffType.ON) {
                this.setStatusOnDevice((byte) 1, (byte) 1);
            } else if (command == OnOffType.OFF) {
                this.setStatusOnDevice((byte) 1, (byte) 0);
            }
        }

        if (channelUID.getId().equals("s2powerOn")) {
            if (command == OnOffType.ON) {
                this.setStatusOnDevice((byte) 2, (byte) 1);
            } else if (command == OnOffType.OFF) {
                this.setStatusOnDevice((byte) 2, (byte) 1);
            }
        }

        if (channelUID.getId().equals("s3powerOn")) {
            if (command == OnOffType.ON) {
                this.setStatusOnDevice((byte) 3, (byte) 1);
            } else if (command == OnOffType.OFF) {
                this.setStatusOnDevice((byte) 3, (byte) 1);
            }

            if (channelUID.getId().equals("s4powerOn")) {
                if (command == OnOffType.ON) {
                    this.setStatusOnDevice((byte) 4, (byte) 1);
                } else if (command == OnOffType.OFF) {
                    this.setStatusOnDevice((byte) 4, (byte) 1);
                }
            }
        }

    }

    private void setStatusOnDevice(byte sid, byte state) {
        int sid_mask = 1 << sid - 1;
        byte[] payload = new byte[16];
        payload[0] = 13;
        payload[2] = -91;
        payload[3] = -91;
        payload[4] = 90;
        payload[5] = 90;
        if (state == 1) {
            payload[6] = (byte) (178 + (sid_mask << 1));
        } else {
            payload[6] = (byte) sid_mask;
        }

        payload[7] = -64;
        payload[8] = 2;
        payload[10] = 3;
        payload[13] = (byte) sid_mask;
        if (state == 1) {
            payload[14] = (byte) sid_mask;
        } else {
            payload[14] = 0;
        }

        byte[] message = this.buildMessage((byte) 106, payload);
        this.sendDatagram(message);
    }

    private boolean getStatusFromDevice() {
        byte[] payload = new byte[16];
        payload[0] = 10;
        payload[2] = -91;
        payload[3] = -91;
        payload[4] = 90;
        payload[5] = 90;
        payload[6] = -82;
        payload[7] = -64;
        payload[8] = 1;

        try {
            byte[] message = this.buildMessage((byte) 106, payload);
            this.sendDatagram(message);
            byte[] response = this.receiveDatagram();
            if (response != null) {
                int error = response[34] | response[35] << 8;
                if (error == 0) {
                    IvParameterSpec ivSpec = new IvParameterSpec(Hex.convertHexToBytes(this.thingConfig.getIV()));
                    Map properties = this.editProperties();
                    byte[] decodedPayload = Utils.decrypt(Hex.fromHexString((String) properties.get("key")), ivSpec,
                            Utils.slice(response, 56, 88));
                    if (decodedPayload != null) {
                        int status = payload[14];
                        if (status == 1) {
                            this.updateState("s1powerOn", OnOffType.ON);
                        } else {
                            this.updateState("s1powerOn", OnOffType.OFF);
                        }

                        if (status == 2) {
                            this.updateState("s2powerOn", OnOffType.ON);
                        } else {
                            this.updateState("s2powerOn", OnOffType.OFF);
                        }

                        if (status == 4) {
                            this.updateState("s3powerOn", OnOffType.ON);
                        } else {
                            this.updateState("s3powerOn", OnOffType.OFF);
                        }

                        if (status == 8) {
                            this.updateState("s4powerOn", OnOffType.ON);
                        } else {
                            this.updateState("s4powerOn", OnOffType.OFF);
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
        } catch (Exception var9) {
            // var9.printStackTrace();
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
