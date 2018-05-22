/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.handler;

import java.util.Collections;
import java.util.Set;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.broadlink.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.BroadlinkHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkControllerHandler extends BaseBridgeHandler {
   private Logger logger = LoggerFactory.getLogger(BroadlinkControllerHandler.class);
   public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS;
   private BroadlinkHandlerFactory factory;

   static {
      SUPPORTED_THING_TYPES_UIDS = Collections.singleton(BroadlinkBindingConstants.THING_TYPE_S1C);
   }

   public BroadlinkControllerHandler(Bridge bridge, BroadlinkHandlerFactory factory) {
      super(bridge);
      this.factory = factory;
   }

    public void handleCommand(final ChannelUID channelUID, final Command command) {
   }

   public void initialize() {
   }

   public void dispose() {
   }

    protected void updateStatus(final ThingStatus status, final ThingStatusDetail detail, final String comment) {
      super.updateStatus(status, detail, comment);
      this.logger.debug("Updating listeners with status {}", status);
   }

    public void addControllerStatusListener(final ControllerStatusListener listener) {
   }

    public void removeControllerStatusListener(final ControllerStatusListener listener) {
   }
}
