package com.handicap.client.mixin;

import com.handicap.Handicap;
import com.handicap.client.ClientState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "net.minecraft.client.gui.GuiGraphicsExtractor")
public abstract class ItemCountMixin {
	@ModifyArg(
		method = "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"
		),
		index = 1,
		require = 0
	)
	private String handicap$obfuscateCount(String text) {
		return ClientState.has(Handicap.DYSLEXIA) ? "\u00a7k" + text : text;
	}
}
