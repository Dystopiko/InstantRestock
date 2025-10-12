package xyz.memothelemo.instantrestock;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModRunner implements ModInitializer {
	public static final String MOD_ID = "instantrestock";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final String PERMISSION_ID = "instantrestock";

    public static boolean canInstantlyRestock(Player player) {
        if (player.level().isClientSide()) return false;
        return Permissions.check(player, PERMISSION_ID);
    }

	@Override
	public void onInitialize() {
        LOGGER.info("Loaded Instant Restock mod");
    }
}