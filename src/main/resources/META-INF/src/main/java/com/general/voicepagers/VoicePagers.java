package com.general.voicepagers;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(VoicePagers.MODID)
public class VoicePagers {
    public static final String MODID = "voicepagers";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> PAGER = ITEMS.register("pager", 
        () -> new PagerItem(new Item.Properties().stacksTo(1)));

    public VoicePagers() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);

        // Регистрируем наш файл конфигурации на сервере
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VoicePagersConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PAGER);
        }
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        String message = event.getRawText();

        // Всегда отменяем стандартную ванильную отправку сообщения, так как глобального чата больше нет
        event.setCanceled(true);

        boolean isPagerMessage = message.startsWith("!");
        int frequency = isPagerMessage ? getPlayerPagerFrequency(sender) : 0;

        Component formattedText;
        double maxDistanceSqr;

        if (isPagerMessage && frequency > 0) {
            // Режим ПЕЙДЖЕРА
            String pagerMessage = message.substring(1).trim();
            formattedText = Component.literal("§8[§bПейджер СН-" + frequency + "§8] §7" + sender.getScoreboardName() + "§r: " + pagerMessage);
            
            // Берем радиус из конфига и возводим в квадрат для быстрой проверки дистанции
            double radius = VoicePagersConfig.PAGER_CHAT_RADIUS.get();
            maxDistanceSqr = radius * radius;
        } else {
            // Режим ЛОКАЛЬНОГО чата (если сообщения без "!" ИЛИ у игрока просто нет пейджера в кармане)
            formattedText = Component.literal("§7[Локально] " + sender.getScoreboardName() + "§r: " + message);
            
            double radius = VoicePagersConfig.LOCAL_CHAT_RADIUS.get();
            maxDistanceSqr = radius * radius;
        }

        // Перебираем игроков и рассылаем сообщения в зависимости от условий
        for (ServerPlayer receiver : sender.getServer().getPlayerList().getPlayers()) {
            // Проверяем, что игроки находятся в одном и том же мире (измерении)
            if (receiver.level() == sender.level()) {
                
                // Считаем квадрат расстояния между отправителем и получателем
                double distSqr = sender.distanceToSqr(receiver);
                
                if (distSqr <= maxDistanceSqr) {
                    if (isPagerMessage && frequency > 0) {
                        // Для пейджера проверяем, совпадает ли частота у получателя
                        if (getPlayerPagerFrequency(receiver) == frequency) {
                            receiver.sendSystemMessage(formattedText);
                        }
                    } else {
                        // Для локального чата отправляем всем, кто просто попал в радиус
                        receiver.sendSystemMessage(formattedText);
                    }
                }
            }
        }
    }

    private int getPlayerPagerFrequency(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == PAGER.get()) {
                CompoundTag nbt = stack.getTag();
                if (nbt != null && nbt.contains("frequency")) {
                    return nbt.getInt("frequency");
                }
            }
        }
        return 0;
    }
}
