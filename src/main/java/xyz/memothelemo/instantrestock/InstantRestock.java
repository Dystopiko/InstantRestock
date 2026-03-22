package xyz.memothelemo.instantrestock;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstantRestock implements DedicatedServerModInitializer {
    public static final String MOD_ID = "InstantRestock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // As of writing this, fabric-permissions-api doesn't have the version provided
    // for the 26.1 snapshot. Therefore, all players can have instant restock effect.
    //
    // TODO: Require `instantrestock` permission for this function to return true
    public static boolean canApplyIRForPlayer(ServerPlayer player) {
        return true;
    }

    @Override
    public void onInitializeServer() {
        LOGGER.info("Loaded Instant Restock mod");
    }
}
