package com.handicap.client.mixin;

import com.handicap.Handicap;
import com.handicap.client.ClientState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.minecraft.client.gui.GuiGraphicsExtractor")
public abstract class ItemCountMixin {
	@Redirect(
		method = "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"
		),
		require = 1
	)
	private void handicap$obfuscateCount(GuiGraphicsExtractor graphics, Font font, String text,
			int x, int y, int color, boolean shadow) {
		if (ClientState.has(Handicap.DYSLEXIA)) {
			graphics.text(font, Component.literal(text).withStyle(ChatFormatting.OBFUSCATED), x, y, color, shadow);
		} else {
			graphics.text(font, text, x, y, color, shadow);
		}
	}
}
