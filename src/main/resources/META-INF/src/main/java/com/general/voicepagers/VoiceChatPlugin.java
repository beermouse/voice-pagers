package com.general.voicepagers;

import de.maxhenric.voicechat.api.VoicechatApi;
import de.maxhenric.voicechat.api.events.EventRegistration;
import de.maxhenric.voicechat.api.events.VoicechatServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@de.maxhenric.voicechat.api.VoicechatPlugin
public class VoiceChatPlugin implements de.maxhenric.voicechat.api.VoicechatPlugin {

    private static final Logger LOGGER = LogManager.getLogger(VoicePagers.MODID);
    public static VoicechatApi voicechatApi;

    // Этот метод возвращает уникальный ID нашего плагина голосового чата
    @Override
    public String getPluginId() {
        return VoicePagers.MODID;
    }

    // Здесь мы регистрируем обработчики событий голосового чата
    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    // Метод сработает, когда сервер Simple Voice Chat полностью запустится
    private void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatApi = event.getVoicechat();
        LOGGER.info("Voice Pagers успешно подключился к API Simple Voice Chat!");
    }
}
