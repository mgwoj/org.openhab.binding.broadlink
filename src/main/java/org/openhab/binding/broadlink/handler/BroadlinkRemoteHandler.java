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
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.broadlink.internal.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 *
 * @author Cato Sognen - Initial contribution
 */
public class BroadlinkRemoteHandler extends BroadlinkBaseThingHandler {
   private Logger logger = LoggerFactory.getLogger(BroadlinkRemoteHandler.class);

   public BroadlinkRemoteHandler(final Thing thing) {
      super(thing);
   }

   protected void sendCode(byte[] code) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      try {
         outputStream.write(new byte[]{2, 0, 0, 0});
         outputStream.write(code);
      } catch (IOException e) {
//         e.printStackTrace();
      }

      if (outputStream.size() % 16 == 0) {
         this.sendDatagram(this.buildMessage((byte)106, outputStream.toByteArray()));
      }

   }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
      if (command == null) {
         if (this.logger.isDebugEnabled()) {
            this.logger.debug("Command passed to handler for thing {} is null");
         }

      } else if (!this.isOnline()) {
         if (this.logger.isDebugEnabled()) {
            this.logger.debug("Can't handle command {} because handler for thing {} is not ONLINE", command, this.getThing().getLabel());
         }

      } else if (command instanceof RefreshType) {
         this.updateItemStatus();
      } else {
        final Channel channel = this.thing.getChannel(channelUID.getId());
        switch (channel.getChannelTypeUID().getId()) {
            case "command": {
               if (this.logger.isDebugEnabled()) {
                  this.logger.debug("Handling ir/rf command {} on channel {} of thing {}", new Object[]{command, channelUID.getId(), this.getThing().getLabel()});
               }

               final byte[] code = this.lookupCode(command, channelUID);
               if (code != null) {
                  this.sendCode(code);
               }
               break;
            }
         default:
            if (this.logger.isDebugEnabled()) {
               this.logger.debug("Thing {} has unknown channel type {}", this.getThing().getLabel(), channel.getChannelTypeUID().getId());
            }
         }

      }
   }

   private byte[] lookupCode(final Command command, final ChannelUID channelUID) {
      if (command.toString() == null) {
         if (this.logger.isDebugEnabled()) {
            this.logger.debug("Unable to perform transform on null command string");
         }

         return null;
      } else {
         final String mapFile = (String)this.thing.getConfiguration().get("mapFilename");
         if (StringUtils.isEmpty(mapFile)) {
            if (this.logger.isDebugEnabled()) {
               this.logger.debug("MAP file is not defined in configuration of thing {}", this.getThing().getLabel());
            }

            return null;
         } else {
            final TransformationService transformService = TransformationHelper.getTransformationService(this.bundleContext, "MAP");
            if (transformService == null) {
               this.logger.error("Failed to get MAP transformation service for thing {}; is bundle installed?", this.getThing().getLabel());
               return null;
            } else {
               String value;
               byte[] code;
               try {
                  value = transformService.transform(mapFile, command.toString());
                  code = Hex.convertHexToBytes(value);
               } catch (TransformationException e) {
                  this.logger.error("Failed to transform {} for thing {} using map file '{}', exception={}", new Object[]{command, this.getThing().getLabel(), mapFile, e.getMessage()});
                  return null;
               }

               if (StringUtils.isEmpty(value)) {
                  this.logger.error("No entry for {} in map file '{}' for thing {}", new Object[]{command, mapFile, this.getThing().getLabel()});
                  return null;
               } else {
                  if (this.logger.isDebugEnabled()) {
                     this.logger.debug("Transformed {} for thing {} with map file '{}'", new Object[]{command, this.getThing().getLabel(), mapFile});
                  }

                  return code;
               }
            }
         }
      }
   }
}
