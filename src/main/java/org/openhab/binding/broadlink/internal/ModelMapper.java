/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.broadlink.internal;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.broadlink.BroadlinkBindingConstants;

/**


 *
 * @author Cato Sognen - Initial contribution
 */
public class ModelMapper {
    public static ThingTypeUID getThingType(int model) {
        if (model == 0) {
            return BroadlinkBindingConstants.THING_TYPE_SP1;
        } else if (model == 10001) {
            return BroadlinkBindingConstants.THING_TYPE_SP2;
        } else if (model != 10009 && model != 31001 && model != 10010 && model != 31002) {
            if (model == 10016) {
                return BroadlinkBindingConstants.THING_TYPE_SP2;
            } else if (model == 30014) {
                return BroadlinkBindingConstants.THING_TYPE_SP2;
            } else if (model == 10024) {
                return BroadlinkBindingConstants.THING_TYPE_SP2;
            } else if (model != 10035 && model != 10046) {
                if (model >= 30000 && model <= 31000) {
                    return BroadlinkBindingConstants.THING_TYPE_SP2;
                } else if (model == 10038) {
                    return BroadlinkBindingConstants.THING_TYPE_SP2;
                } else if (model == 30014) {
                    return BroadlinkBindingConstants.THING_TYPE_SP3;
                } else if (model == 10002) {
                    return BroadlinkBindingConstants.THING_TYPE_RM2;
                } else if (model == 10039) {
                    return BroadlinkBindingConstants.THING_TYPE_RM3;
                } else if (model == 10045) {
                    return BroadlinkBindingConstants.THING_TYPE_RM;
                } else if (model == 10115) {
                    return BroadlinkBindingConstants.THING_TYPE_RM2;
                } else if (model == 10108) {
                    return BroadlinkBindingConstants.THING_TYPE_RM2;
                } else if (model == 10026) {
                    return BroadlinkBindingConstants.THING_TYPE_RM2;
                } else if (model == 10119) {
                    return BroadlinkBindingConstants.THING_TYPE_RM2;
                } else if (model == 10123) {
                    return BroadlinkBindingConstants.THING_TYPE_RM2;
                } else if (model == 10127) {
                    return BroadlinkBindingConstants.THING_TYPE_RM;
                } else if (model == 10004) {
                    return BroadlinkBindingConstants.THING_TYPE_A1;
                } else if (model == 20149) {
                    return BroadlinkBindingConstants.THING_TYPE_MP1;
                } else if (model == 20251) {
                    return BroadlinkBindingConstants.THING_TYPE_MP2;
                } else if (model == 20215) {
                    return BroadlinkBindingConstants.THING_TYPE_MP1;
                } else if (model == 10018) {
                    return BroadlinkBindingConstants.THING_TYPE_S1C;
                } else {
                    return model == 20045 ? null : null;
                }
            } else {
                return BroadlinkBindingConstants.THING_TYPE_SP2;
            }
        } else {
            return BroadlinkBindingConstants.THING_TYPE_SP2;
        }
    }

    public static StringType getAirValue(byte b) {
        int air = Byte.toUnsignedInt(b);
        if (air == 0) {
            return new StringType("PERFECT");
        } else if (air == 1) {
            return new StringType("GOOD");
        } else if (air == 2) {
            return new StringType("NORMAL");
        } else {
            return air == 3 ? new StringType("BAD") : new StringType("UNKNOWN");
        }
    }

    public static StringType getLightValue(byte b) {
        int light = Byte.toUnsignedInt(b);
        if (light == 0) {
            return new StringType("DARK");
        } else if (light == 1) {
            return new StringType("DIM");
        } else if (light == 2) {
            return new StringType("NORMAL");
        } else {
            return light == 3 ? new StringType("BRIGHT") : new StringType("UNKNOWN");
        }
    }

    public static StringType getNoiseValue(byte b) {
        int noise = Byte.toUnsignedInt(b);
        if (noise == 0) {
            return new StringType("QUIET");
        } else if (noise == 1) {
            return new StringType("NORMAL");
        } else if (noise == 2) {
            return new StringType("NOISY");
        } else {
            return noise == 3 ? new StringType("EXTREME") : new StringType("UNKNOWN");
        }
    }
}
