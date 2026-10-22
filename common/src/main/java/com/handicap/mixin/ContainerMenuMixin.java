package com.handicap.mixin;

import com.handicap.Handicap;
import com.handicap.HandicapManager;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.world.inventory.AbstractContainerMenu")
public abstract class ContainerMenuMixin {
	@Inject(method = "clicked", at = @At("HEAD"), cancellable = true, require = 0)
	private void handicap$noDrop(int slotId, int button, ContainerInput clickType, Player player, CallbackInfo ci) {
		if (player instanceof ServerPlayer sp
			&& HandicapManager.state() != null
			&& HandicapManager.state().has(sp.getUUID(), Handicap.KLEPTOMANIA)
			&& (clickType == ContainerInput.THROW || (slotId == -999 && clickType == ContainerInput.PICKUP))) {
			ci.cancel();
			sp.sendOverlayMessage(Component.literal("You can't let go of this item!"));
		}
	}
}
