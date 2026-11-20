package fr.rulekeeper;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RuleKeeper.MODID)
public class RuleKeeper {
    public static final String MODID = "rulekeeper";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RuleKeeper() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RuleEvents());
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent event) {
        RuleCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        RuleManager.load();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        RuleManager.save();
    }
}
