package com.general.voicepagers;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(VoicePagers.MODID)
public class VoicePagers {
    public static final String MODID = "voicepagers";
    public static final Logger LOGGER = LogManager.getLogger();

    // Создаем регистратор для наших предметов
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Временно регистрируем пейджер как обычный базовый предмет (в следующих шагах мы дадим ему логику и NBT)
    public static final RegistryObject<Item> PAGER = ITEMS.register("pager", 
        () -> new Item(new Item.Properties().stacksTo(1)));

    public VoicePagers() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрируем наш список предметов в системе Forge
        ITEMS.register(modEventBus);

        // Подключаем метод добавления в креативный инвентарь
        modEventBus.addListener(this::addCreative);

        // Регистрируем сам мод в главном автобусе событий Майнкрафта
        MinecraftForge.EVENT_BUS.register(this);
    }

    // Добавляем наш пейджер во вкладку "Инструменты" в креативе
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PAGER);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Мод Voice Pagers успешно запущен на сервере!");
    }
}
