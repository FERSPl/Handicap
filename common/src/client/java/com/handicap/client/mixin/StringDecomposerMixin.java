package com.handicap.client.mixin;

import com.handicap.client.DyslexiaText;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "net.minecraft.util.StringDecomposer")
public abstract class StringDecomposerMixin {
	@ModifyVariable(method = {"iterate", "iterateFormatted"}, at = @At("HEAD"), argsOnly = true, require = 0)
	private static String handicap$scramble(String text) {
		return DyslexiaText.apply(text);
	}
}
