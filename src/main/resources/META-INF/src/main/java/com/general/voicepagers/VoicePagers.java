package com.general.voicepagers;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(VoicePagers.MODID)
public class VoicePagers {
    public static final String MODID = "voicepagers";

    // Создаем регистратор для предметов
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Регистрируем наш пейджер со стандартными свойствами (стакается до 1 штуки)
    public static final RegistryObject<Item> PAGER = ITEMS.register("pager", 
        () -> new Item(new Item.Properties().stacksTo(1)));

    public VoicePagers() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Подключаем регистратор к шине событий мода
        ITEMS.register(modEventBus);

        // Добавляем пейджер в ванильную вкладку инструментов в креативе
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PAGER);
        }
    }
}
