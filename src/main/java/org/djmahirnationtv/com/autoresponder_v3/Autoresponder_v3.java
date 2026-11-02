package org.djmahirnationtv.com.autoresponder_v3;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Autoresponder_v3 implements ModInitializer {
    public static final String MOD_ID = "autoresponder_v3";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }
}
