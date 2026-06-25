package com.general.voicepagers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PagerItem extends Item {
    public PagerItem(Properties properties) {
        super(properties);
    }

    // Метод срабатывает при клике правой кнопкой мыши (ПКМ) с пейджером в руке
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Будем менять частоту только на стороне сервера, чтобы избежать рассинхрона
        if (!level.isClientSide()) {
            CompoundTag nbt = stack.getOrCreateTag();
            
            // Считываем текущую частоту (если её нет, вернётся 0) и циклически гоняем от 1 до 9
            int currentFrequency = nbt.getInt("frequency");
            int newFrequency = currentFrequency >= 9 ? 1 : currentFrequency + 1;
            
            nbt.putInt("frequency", newFrequency);
            
            // Выводим сообщение игроку в чат
            player.sendSystemMessage(Component.literal("§a[Пейджер] §fНастроена частота: §e" + newFrequency + " МГц"));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // Добавляем красивое описание предмета в инвентаре при наведении
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int frequency = 0;
        if (stack.hasTag() && stack.getTag().contains("frequency")) {
            frequency = stack.getTag().getInt("frequency");
        }
        
        if (frequency > 0) {
            tooltip.add(Component.literal("§7Частота: §e" + frequency + " МГц"));
        } else {
            tooltip.add(Component.literal("§8§oПейджер не настроен. Нажмите ПКМ."));
        }
        
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
