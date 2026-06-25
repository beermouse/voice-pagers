package com.general.voicepagers;

import de.maxhenric.voicechat.api.VoicechatApi;
import de.maxhenric.voicechat.api.VoicechatConnection;
import de.maxhenric.voicechat.api.events.EventRegistration;
import de.maxhenric.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenric.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenric.voicechat.api.packets.MicrophonePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@de.maxhenric.voicechat.api.VoicechatPlugin
public class VoiceChatPlugin implements de.maxhenric.voicechat.api.VoicechatPlugin {

    private static final Logger LOGGER = LogManager.getLogger(VoicePagers.MODID);
    public static VoicechatApi voicechatApi;

    @Override
    public String getPluginId() {
        return VoicePagers.MODID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatApi = event.getVoicechat();
        LOGGER.info("Voice Pagers успешно подключился к API Simple Voice Chat!");
    }

    // Этот метод вызывается каждый раз, когда кто-то говорит в микрофон
    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;

        // Получаем игрока, который сейчас говорит
        if (senderConnection.getPlayer().getPlayer() instanceof ServerPlayer player) {
            int frequency = getPlayerPagerFrequency(player);

            // Если у игрока в инвентаре найден настроенный пейджер (частота > 0)
            if (frequency > 0) {
                MicrophonePacket packet = event.getPacket();
                
                // Перебираем всех игроков на сервере, подключенных к голосовому чату
                for (VoicechatConnection receiverConnection : voicechatApi.getConnections()) {
                    if (receiverConnection.getPlayer().getPlayer() instanceof ServerPlayer receiver) {
                        
                        // Если это не сам говорящий и у получателя пейджер на той же частоте
                        if (!receiver.getUUID().equals(player.getUUID()) && getPlayerPagerFrequency(receiver) == frequency) {
                            
                            // Транслируем аудиопакет напрямую в наушники получателя
                            voicechatApi.sendStaticSoundPacketTo(receiverConnection, packet.toStaticSoundPacket());
                        }
                    }
                }
            }
        }
    }

    // Вспомогательный метод: ищет пейджер в инвентаре игрока и возвращает его частоту
    private int getPlayerPagerFrequency(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            
            // Если нашли наш пейджер
            if (!stack.isEmpty() && stack.getItem() == VoicePagers.PAGER.get()) {
                CompoundTag nbt = stack.getTag();
                if (nbt != null && nbt.contains("frequency")) {
                    return nbt.getInt("frequency");
                }
            }
        }
        return 0; // Пейджер не найден или не настроен
    }
}
