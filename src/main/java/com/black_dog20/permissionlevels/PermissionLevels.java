package com.black_dog20.permissionlevels;

import com.black_dog20.permissionlevels.commands.CommandOp;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PermissionLevels.MOD_ID)
public class PermissionLevels
{

    public static final String MOD_ID = "permissionlevels";
    private static final Logger LOGGER = LogManager.getLogger();

    public PermissionLevels() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCommandRegister(RegisterCommandsEvent event) {
        CommandOp.register(event.getDispatcher());
    }

    private static final String PROTOCOL_VERSION = Integer.toString(1);

    public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MOD_ID, "network"))
            .clientAcceptedVersions((s)-> true)
            .serverAcceptedVersions((s)-> true)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
}
