/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

/**


 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkSocketModel1Handler extends BroadlinkSocketHandler {
    public BroadlinkSocketModel1Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals("powerOn")) {
            if (command == OnOffType.ON) {
                this.setStatusOnDevice(1);
            } else if (command == OnOffType.OFF) {
                this.setStatusOnDevice(0);
            }
        }

    }

    public void setStatusOnDevice(int state) {
        byte[] payload = new byte[16];
        payload[0] = (byte) state;
        byte[] message = this.buildMessage((byte) 102, payload);
        this.sendDatagram(message);
    }
}
