package com.black_dog20.permissionlevels;

import com.black_dog20.permissionlevels.commands.CommandOp;
import com.google.common.collect.ImmutableSet;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

@Mod(PermissionLevels.MOD_ID)
public class PermissionLevels
{

    public static final String MOD_ID = "permissionlevels";
    private static final Logger LOGGER = LogManager.getLogger();
    private Set<Commands.CommandSelection> environmentTypeSet = ImmutableSet.of(Commands.CommandSelection.ALL, Commands.CommandSelection.DEDICATED);

    public PermissionLevels() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCommandRegister(RegisterCommandsEvent event) {
        if (environmentTypeSet.contains(event.getCommandSelection())) {
            CommandOp.register(event.getDispatcher());
        }
    }

    private static final String PROTOCOL_VERSION = Integer.toString(1);

    public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MOD_ID, "network"))
            .clientAcceptedVersions((s)-> true)
            .serverAcceptedVersions((s)-> true)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
}
