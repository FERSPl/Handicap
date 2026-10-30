package com.handicap.fabric.gametest;

import com.handicap.client.Prosopagnosia;

import com.mojang.authlib.GameProfile;

import io.netty.channel.embedded.EmbeddedChannel;

import java.lang.reflect.Field;
import java.util.UUID;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;

public class ProsopagnosiaMultiplayerTest implements FabricClientGameTest {
	private static final String BUDDY = "TestBuddy";

	@Override
	public void runTest(ClientGameTestContext context) {
		UUID buddyId = context.computeOnClient(mc -> {
			UUID id = UUID.randomUUID();
			while (DefaultPlayerSkin.get(id).body().equals(DefaultPlayerSkin.getDefaultSkin().body())) {
				id = UUID.randomUUID();
			}
			return id;
		});
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getConnection().waitForChunksRender();
			world.getServer().runCommand("handicap settings timer off");
			world.getServer().runOnServer(server -> {
				GameProfile profile = new GameProfile(buddyId, BUDDY);
				ServerPlayer buddy = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
				server.getPlayerList().placeNewPlayer(new FakeConnection(), buddy, CommonListenerCookie.createInitial(profile, false));
			});
			world.getServer().runCommand("execute as @a[name=!" + BUDDY + "] at @s run tp " + BUDDY + " ^ ^ ^3 facing entity @s");
			context.waitTicks(40);
			context.takeScreenshot("multiplayer-before");

			world.getServer().runCommand("handicap give @a[name=!" + BUDDY + "] prosopagnosia");
			context.waitTicks(40);
			context.takeScreenshot("multiplayer-after");
			context.getInput().holdKey(options -> options.keyPlayerList);
			context.waitTicks(5);
			context.takeScreenshot("multiplayer-after-tab");
			context.getInput().releaseKey(options -> options.keyPlayerList);

			String problem = context.computeOnClient(mc -> {
				var steve = DefaultPlayerSkin.getDefaultSkin().body();
				PlayerInfo info = mc.getConnection().getPlayerInfo(buddyId);
				if (info == null) {
					return "the buddy is not in the player list";
				}
				if (!info.getSkin().body().equals(steve)) {
					return "the Tab list still shows the real skin";
				}
				if (!(mc.level.getPlayerByUUID(buddyId) instanceof AbstractClientPlayer buddy)) {
					return "the buddy is not visible in the world";
				}
				if (!buddy.getSkin().body().equals(steve)) {
					return "the buddy is not drawn as Steve";
				}
				if (!mc.player.getSkin().body().equals(DefaultPlayerSkin.get(mc.player.getUUID()).body())) {
					return "your own skin changed";
				}
				if (!Prosopagnosia.hideNames("<" + BUDDY + "> hello").equals("<???> hello")) {
					return "the buddy's name is not hidden in text";
				}
				if (Prosopagnosia.hideNames(mc.player.getName().getString()).contains("???")) {
					return "your own name is hidden";
				}
				return null;
			});
			if (problem != null) {
				throw new AssertionError(problem);
			}

			world.getServer().runCommand("handicap clear @a[name=!" + BUDDY + "] prosopagnosia");
			context.waitTicks(40);
			context.takeScreenshot("multiplayer-removed");
			String back = context.computeOnClient(mc ->
				mc.getConnection().getPlayerInfo(buddyId).getSkin().body().equals(DefaultPlayerSkin.get(buddyId).body())
					? null : "the real skin did not come back");
			if (back != null) {
				throw new AssertionError(back);
			}
		}
	}

	private static final class FakeConnection extends Connection {
		FakeConnection() {
			super(PacketFlow.SERVERBOUND);
			try {
				Field channel = Connection.class.getDeclaredField("channel");
				channel.setAccessible(true);
				channel.set(this, new EmbeddedChannel());
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocol, T listener) {
		}

		@Override
		public void setupOutboundProtocol(ProtocolInfo<?> protocol) {
		}

		@Override
		public void setReadOnly() {
		}

		@Override
		public void handleDisconnection() {
		}
	}
}
