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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.broadlink.internal.Hex;
import org.openhab.binding.broadlink.internal.ModelMapper;
import org.openhab.binding.broadlink.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkA1Handler extends BroadlinkBaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(BroadlinkA1Handler.class);

    public BroadlinkA1Handler(Thing thing) {
        super(thing);
    }

    private boolean getStatusFromDevice() {
        byte[] payload = new byte[16];
        payload[0] = 1;

        try {
            byte[] message = this.buildMessage((byte) 106, payload);
            if (!this.sendDatagram(message)) {
                this.logger.error("Sending packet to device '{}' failed.", this.getThing().getUID());
                return false;
            } else {
                byte[] response = this.receiveDatagram();
                if (response == null) {
                    this.logger.debug("Incoming packet from device '{}' is null.", this.getThing().getUID());
                    return false;
                } else {
                    int error = response[34] | response[35] << 8;
                    if (error != 0) {
                        this.logger.error("Response from device '{}' is not valid.", this.thingConfig.getIpAddress());
                        return false;
                    } else {
                        IvParameterSpec ivSpec = new IvParameterSpec(Hex.convertHexToBytes(this.thingConfig.getIV()));
                        Map properties = this.editProperties();
                        byte[] decryptResponse = Utils.decrypt(Hex.fromHexString((String) properties.get("key")),
                                ivSpec, Utils.slice(response, 56, 88));
                        float temperature = (float) ((decryptResponse[4] * 10 + decryptResponse[5]) / 10.0D);
                        this.updateState("temperature", new DecimalType(temperature));
                        this.updateState("humidity",
                                new DecimalType((decryptResponse[6] * 10 + decryptResponse[7]) / 10.0D));
                        this.updateState("light", ModelMapper.getLightValue(decryptResponse[8]));
                        this.updateState("air", ModelMapper.getAirValue(decryptResponse[10]));
                        this.updateState("noise", ModelMapper.getNoiseValue(decryptResponse[12]));
                        return true;
                    }
                }
            }
        } catch (Exception var9) {
            // var9.printStackTrace();
            this.logger.error("{}.", var9.getMessage());
            return false;
        }
    }

    @Override
    public void updateItemStatus() {
        if (hostAvailabilityCheck(this.thingConfig.getIpAddress(), 3000)) {
            if (this.getStatusFromDevice()) {
                if (!this.isOnline()) {
                    this.updateStatus(ThingStatus.ONLINE);
                }
            } else {
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not control device at IP address " + this.thingConfig.getIpAddress());
            }
        } else {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not control device at IP address " + this.thingConfig.getIpAddress());
        }

    }
}
