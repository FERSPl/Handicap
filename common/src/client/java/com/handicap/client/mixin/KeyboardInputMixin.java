package com.handicap.client.mixin;

import com.handicap.Handicap;
import com.handicap.client.ClientState;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void handicap$swapLeftRight(CallbackInfo ci) {
		if (!ClientState.has(Handicap.SPATIAL_DISORIENTATION)) {
			return;
		}
		ClientInput input = (ClientInput) (Object) this;
		Input keys = input.keyPresses;
		input.keyPresses = new Input(keys.forward(), keys.backward(), keys.right(), keys.left(), keys.jump(), keys.shift(), keys.sprint());
		Vec2 move = input.getMoveVector();
		((ClientInputAccessor) input).handicap$setMoveVector(new Vec2(-move.x, move.y));
	}
}
