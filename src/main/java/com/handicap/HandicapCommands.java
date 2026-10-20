package com.handicap;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class HandicapCommands {
	private static final SuggestionProvider<CommandSourceStack> HANDICAP_SUGGESTIONS = (ctx, builder) -> {
		List<String> options = new ArrayList<>(List.of("all", "random"));
		for (Handicap d : Handicap.values()) {
			options.add(d.id);
		}
		return SharedSuggestionProvider.suggest(options, builder);
	};

	private static final SuggestionProvider<CommandSourceStack> MINUTES_SUGGESTIONS = (ctx, builder) -> {
		for (String value : new String[] {"1", "5", "10", "30"}) {
			builder.suggest(value, Component.literal("minutes"));
		}
		return builder.buildFuture();
	};

	private HandicapCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(build()));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("handicap")
			.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.then(Commands.literal("give")
				.then(Commands.argument("targets", EntityArgument.players())
					.then(Commands.argument("handicap", StringArgumentType.word())
						.suggests(HANDICAP_SUGGESTIONS)
						.executes(ctx -> modify(ctx, true)))))
			.then(Commands.literal("clear")
				.executes(ctx -> removeAll(ctx, List.of(ctx.getSource().getPlayerOrException())))
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(ctx -> removeAll(ctx, EntityArgument.getPlayers(ctx, "targets")))
					.then(Commands.argument("handicap", StringArgumentType.word())
						.suggests(HANDICAP_SUGGESTIONS)
						.executes(ctx -> modify(ctx, false)))))
			.then(Commands.literal("list")
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(HandicapCommands::list)))
			.then(Commands.literal("next")
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(HandicapCommands::next)))
			.then(Commands.literal("stacking")
				.executes(HandicapCommands::stackingStatus)
				.then(Commands.argument("enabled", BoolArgumentType.bool())
					.executes(HandicapCommands::stackingSet)))
			.then(Commands.literal("timer")
				.executes(HandicapCommands::timerStatus)
				.then(Commands.literal("off")
					.executes(HandicapCommands::timerOff))
				.then(Commands.argument("minutes", DoubleArgumentType.doubleArg(0.1, 10000.0))
					.suggests(MINUTES_SUGGESTIONS)
					.executes(HandicapCommands::timerSet)));
	}

	private static boolean ready(CommandContext<CommandSourceStack> ctx) {
		if (HandicapManager.state() == null) {
			ctx.getSource().sendFailure(Component.literal("The mod is not ready yet."));
			return false;
		}
		return true;
	}

	private static int modify(CommandContext<CommandSourceStack> ctx, boolean give) throws CommandSyntaxException {
		if (!ready(ctx)) {
			return 0;
		}
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		String value = StringArgumentType.getString(ctx, "handicap").toLowerCase(Locale.ROOT);
		boolean all = value.equals("all");
		boolean random = value.equals("random");
		Handicap single = null;
		if (!all && !random) {
			single = Handicap.byId(value);
			if (single == null) {
				ctx.getSource().sendFailure(Component.literal("Unknown handicap: " + value));
				return 0;
			}
		}

		int changed = 0;
		for (ServerPlayer p : targets) {
			if (all) {
				for (Handicap d : Handicap.values()) {
					boolean ok = give ? HandicapManager.give(p, d, false) : HandicapManager.remove(p, d, false);
					if (ok) {
						changed++;
					}
				}
				p.sendSystemMessage(Component.literal(give ? "You received every handicap." : "All your handicaps have been removed."));
			} else if (random) {
				Handicap d = give ? HandicapManager.giveRandom(p) : HandicapManager.removeRandom(p);
				if (d != null) {
					changed++;
				}
			} else {
				boolean ok = give ? HandicapManager.give(p, single, true) : HandicapManager.remove(p, single, true);
				if (ok) {
					changed++;
				}
			}
		}

		HandicapManager.state().save();
		final int count = changed;
		final int players = targets.size();
		ctx.getSource().sendSuccess(() -> Component.literal(
			(give ? "Handicaps given: " : "Handicaps removed: ") + count + " (players targeted: " + players + ")"), true);
		return count;
	}

	private static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (!ready(ctx)) {
			return 0;
		}
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		ServerPlayer self = ctx.getSource().getPlayer();
		for (ServerPlayer p : targets) {
			if (p == self && HandicapManager.state().has(p.getUUID(), Handicap.ANOSOGNOSIA)) {
				ctx.getSource().sendSuccess(() -> Component.literal("You can't see your handicaps."), false);
				continue;
			}
			StringBuilder sb = new StringBuilder();
			for (Handicap d : HandicapManager.state().get(p.getUUID())) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(d.displayName);
			}
			final String text = p.getName().getString() + ": " + (sb.length() == 0 ? "no handicap" : sb);
			ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		}
		return targets.size();
	}

	private static int next(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (!ready(ctx)) {
			return 0;
		}
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
		int count = 0;
		for (ServerPlayer p : targets) {
			if (HandicapManager.giveRandom(p) != null) {
				count++;
			}
		}
		HandicapManager.state().resetTimer();
		HandicapManager.state().save();
		final int c = count;
		ctx.getSource().sendSuccess(() -> Component.literal("Next handicap given to " + c + " player(s). Timer restarted."), true);
		return count;
	}

	private static int removeAll(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
		if (!ready(ctx)) {
			return 0;
		}
		int removed = 0;
		for (ServerPlayer p : targets) {
			for (Handicap d : Handicap.values()) {
				if (HandicapManager.remove(p, d, false)) {
					removed++;
				}
			}
			p.sendSystemMessage(Component.literal("All your handicaps have been removed."));
		}
		HandicapManager.state().save();
		final int count = removed;
		final int players = targets.size();
		ctx.getSource().sendSuccess(() -> Component.literal(
			"Handicaps removed: " + count + " (players targeted: " + players + ")"), true);
		return count;
	}

	private static int stackingStatus(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		final String text = HandicapManager.state().stacking()
			? "Stacking is on: handicaps add up."
			: "Stacking is off: a new handicap replaces the previous one.";
		ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		ctx.getSource().sendSuccess(() -> Component.literal("Usage: /handicap stacking <true|false>"), false);
		return 1;
	}

	private static int stackingSet(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
		HandicapManager.state().setStacking(enabled);
		HandicapManager.state().save();
		final String text = enabled
			? "Stacking turned on: handicaps now add up."
			: "Stacking turned off: a new handicap now replaces the previous one.";
		ctx.getSource().sendSuccess(() -> Component.literal(text), true);
		return 1;
	}

	private static int timerStatus(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		HandicapState s = HandicapManager.state();
		final String text = s.timerEnabled()
			? "Next handicap in " + formatDuration(s.secondsRemaining()) + " (interval: " + formatDuration(s.timerSeconds()) + ")."
			: "The timer is off.";
		final String usage = "Usage: /handicap timer <minutes> or /handicap timer off";
		ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		ctx.getSource().sendSuccess(() -> Component.literal(usage), false);
		return 1;
	}

	private static int timerOff(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		HandicapState s = HandicapManager.state();
		s.setTimerEnabled(false);
		s.save();
		ctx.getSource().sendSuccess(() -> Component.literal("Timer turned off."), true);
		return 1;
	}

	private static int timerSet(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		HandicapState s = HandicapManager.state();
		double minutes = DoubleArgumentType.getDouble(ctx, "minutes");
		int seconds = (int) Math.round(minutes * 60.0);
		if (seconds < 5) {
			ctx.getSource().sendFailure(Component.literal("Invalid duration: enter a number of minutes (minimum 0.1), or off."));
			return 0;
		}
		s.setTimerSeconds(seconds);
		s.save();
		ctx.getSource().sendSuccess(() -> Component.literal("A handicap will be given every " + formatDuration(seconds) + "."), true);
		return 1;
	}

	private static String formatDuration(int totalSeconds) {
		int h = totalSeconds / 3600;
		int m = (totalSeconds % 3600) / 60;
		int sec = totalSeconds % 60;
		StringBuilder sb = new StringBuilder();
		if (h > 0) {
			sb.append(h).append(" h ");
		}
		if (m > 0) {
			sb.append(m).append(" min ");
		}
		if (sec > 0 || sb.length() == 0) {
			sb.append(sec).append(" s");
		}
		return sb.toString().trim();
	}
}
