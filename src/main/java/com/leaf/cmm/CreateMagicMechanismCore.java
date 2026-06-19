package com.leaf.cmm;

import com.leaf.cmm.recipe.CmmAllRecipeTypes;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CreateMagicMechanismCore.MOD_ID)
public class CreateMagicMechanismCore {
    public static final String MOD_ID = "cmm";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    static {
        REGISTRATE.setTooltipModifierFactory(item ->
            new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                    .andThen(TooltipModifier.mapNull(KineticStats.create((item)))));
    }

    @SuppressWarnings("removal")
    public CreateMagicMechanismCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);

        CmmAllRecipeTypes.register(modEventBus);

        REGISTRATE.registerEventListeners(modEventBus);

        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation asResource(String path){
        return ResourceLocation.fromNamespaceAndPath("cmm", path);
    }

    public static ResourceLocation modLoc(String path){
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
