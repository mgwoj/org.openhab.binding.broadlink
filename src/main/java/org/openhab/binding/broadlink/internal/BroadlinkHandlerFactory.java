/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.openhab.binding.broadlink.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.handler.BroadlinkA1Handler;
import org.openhab.binding.broadlink.handler.BroadlinkControllerHandler;
import org.openhab.binding.broadlink.handler.BroadlinkRemoteHandler;
import org.openhab.binding.broadlink.handler.BroadlinkRemoteModel2Handler;
import org.openhab.binding.broadlink.handler.BroadlinkSocketModel1Handler;
import org.openhab.binding.broadlink.handler.BroadlinkSocketModel2Handler;
import org.openhab.binding.broadlink.handler.BroadlinkSocketModel3Handler;
import org.openhab.binding.broadlink.handler.BroadlinkStripModel1Handler;
import org.openhab.binding.broadlink.internal.discovery.BroadlinkDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(BroadlinkHandlerFactory.class);
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private List<ChannelType> channelTypes = new CopyOnWriteArrayList<>();
    private List<ChannelGroupType> channelGroupTypes = new CopyOnWriteArrayList<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BroadlinkBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Creating Thing handler for '{}'", thingTypeUID.getAsString());
        }

        if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM)) {
            return new BroadlinkRemoteModel2Handler(thing);
        } else if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM2)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("RM 2 handler requested created");
            }

            return new BroadlinkRemoteModel2Handler(thing);
        } else if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_RM3)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("RM 3 handler requested created");
            }

            return new BroadlinkRemoteHandler(thing);
        } else if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_A1)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("A1 handler requested created");
            }

            return new BroadlinkA1Handler(thing);
        } else if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MP1)) {
            return new BroadlinkStripModel1Handler(thing);
        } else if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP1)) {
            return new BroadlinkSocketModel1Handler(thing);
        } else if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP2)) {
            return new BroadlinkSocketModel2Handler(thing);
        } else if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_SP3)) {
            return new BroadlinkSocketModel3Handler(thing);
        } else if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MP1)) {
            return new BroadlinkStripModel1Handler(thing);
        } else if (thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MP2)) {
            return new BroadlinkStripModel1Handler(thing);
        } else {
            thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_S1C);
            thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_PIR);
            thingTypeUID.equals(BroadlinkBindingConstants.THING_TYPE_MAGNET);
            return null;
        }
    }

    @Override
    protected synchronized void removeHandler(final ThingHandler thingHandler) {
        if (thingHandler instanceof BroadlinkControllerHandler) {
            final ServiceRegistration serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }

    }

    private synchronized void registerBroadlinkDeviceDiscoveryService(
            final BroadlinkControllerHandler broadlinkControllerHandler) {
        final BroadlinkDeviceDiscoveryService discoveryService = new BroadlinkDeviceDiscoveryService(
                broadlinkControllerHandler);
        this.discoveryServiceRegs.put(broadlinkControllerHandler.getThing().getUID(), this.bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable()));
    }
}
