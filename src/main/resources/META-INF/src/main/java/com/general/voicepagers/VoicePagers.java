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
import net.minecraftforge.fml.common.Mod;
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

        // Регистрируем наш класс в главной шине событий, чтобы ловить сообщения чата
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PAGER);
        }
    }

    // Ловим отправку сообщений в чат
    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        String message = event.getRawText();

        // Если сообщение начинается с "!", считаем это отправкой в пейджер
        if (message.startsWith("!")) {
            int frequency = getPlayerPagerFrequency(sender);

            // Если у игрока есть настроенный пейджер
            if (frequency > 0) {
                // Отменяем отправку сообщения в глобальный обычный чат
                event.setCanceled(true);

                // Отрезаем "!" от текста
                String pagerMessage = message.substring(1).trim();
                
                // Форматируем красивый текст: [Пейджер CH-3] Игрок: Текст
                Component formattedText = Component.literal("§8[§bПейджер СН-" + frequency + "§8] §7" + sender.getScoreboardName() + "§r: " + pagerMessage);

                // Рассылаем всем игрокам, у кого есть пейджер на этой же частоте
                for (ServerPlayer receiver : sender.getServer().getPlayerList().getPlayers()) {
                    if (getPlayerPagerFrequency(receiver) == frequency) {
                        receiver.sendSystemMessage(formattedText);
                    }
                }
            }
        }
    }

    // Метод поиска пейджера в инвентаре
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
