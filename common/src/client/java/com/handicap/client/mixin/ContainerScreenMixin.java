package com.handicap.client.mixin;

import com.handicap.client.InventoryPanel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin {
	@Shadow
	protected int leftPos;

	@Shadow
	protected int topPos;

	@Shadow
	@Final
	protected int imageWidth;

	@Inject(method = "render", at = @At("TAIL"))
	private void handicap$panel(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (InventoryPanel.supports((Screen) (Object) this)) {
			InventoryPanel.render(graphics, leftPos, topPos, imageWidth, mouseX, mouseY);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void handicap$click(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (InventoryPanel.supports((Screen) (Object) this) && InventoryPanel.click(mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void handicap$scroll(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
		if (InventoryPanel.supports((Screen) (Object) this) && InventoryPanel.scroll(mouseX, mouseY, scrollY)) {
			cir.setReturnValue(true);
		}
	}
}
