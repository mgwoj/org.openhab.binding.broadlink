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
import org.openhab.binding.broadlink.internal.Utils;

/**
 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkRemoteModel2Handler extends BroadlinkRemoteHandler {
    public BroadlinkRemoteModel2Handler(final Thing thing) {
        super(thing);
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

    public boolean getStatusFromDevice() {
        final byte[] payload = new byte[16];
        payload[0] = 1;
        final byte[] message = this.buildMessage((byte) 106, payload);
        this.sendDatagram(message);
        final byte[] response = this.receiveDatagram();
        if (response != null) {
            final int error = response[34] | response[35] << 8;
            if (error == 0) {
                final IvParameterSpec ivSpec = new IvParameterSpec(Hex.convertHexToBytes(this.thingConfig.getIV()));
                final Map<String, String> properties = this.editProperties();
                final byte[] decodedPayload = Utils.decrypt(Hex.fromHexString(properties.get("key")), ivSpec,
                        Utils.slice(response, 56, 88));
                if (decodedPayload != null) {
                    final float temperature = (float) ((decodedPayload[4] * 10 + decodedPayload[5]) / 10.0D);
                    this.updateState("temperature", new DecimalType(temperature));
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
    }
}
