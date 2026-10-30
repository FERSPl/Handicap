package com.handicap;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(build());
	}

	private static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("handicap")
			.requires(HandicapConfig::allows)
			.then(Commands.literal("give")
				.then(Commands.argument("targets", EntityArgument.players())
					.then(Commands.argument("handicap", StringArgumentType.word())
						.suggests(HANDICAP_SUGGESTIONS)
						.executes(ctx -> modify(ctx, true)))))
			.then(Commands.literal("clear")
				.executes(ctx -> removeAll(ctx, List.of(ctx.getSource().getPlayerOrException())))
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(ctx -> removeAll(ctx, targets(ctx)))
					.then(Commands.argument("handicap", StringArgumentType.word())
						.suggests(HANDICAP_SUGGESTIONS)
						.executes(ctx -> modify(ctx, false)))))
			.then(Commands.literal("list")
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(HandicapCommands::list)))
			.then(Commands.literal("next")
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(HandicapCommands::next)))
			.then(Commands.literal("settings")
				.executes(HandicapCommands::settingsStatus)
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
						.executes(HandicapCommands::timerSet)))
				.then(Commands.literal("max")
					.executes(HandicapCommands::maxStatus)
					.then(Commands.literal("off")
						.executes(ctx -> maxSet(ctx, 0)))
					.then(Commands.argument("count", IntegerArgumentType.integer(1, Handicap.values().length))
						.executes(ctx -> maxSet(ctx, IntegerArgumentType.getInteger(ctx, "count")))))
				.then(Commands.literal("duration")
					.executes(HandicapCommands::durationStatus)
					.then(Commands.literal("off")
						.executes(ctx -> durationSet(ctx, 0)))
					.then(Commands.argument("minutes", DoubleArgumentType.doubleArg(0.1, 10000.0))
						.suggests(MINUTES_SUGGESTIONS)
						.executes(ctx -> durationSet(ctx,
							(int) Math.round(DoubleArgumentType.getDouble(ctx, "minutes") * 60.0))))));
	}

	private static Collection<ServerPlayer> targets(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<ServerPlayer> found = EntityArgument.getPlayers(ctx, "targets");
		CommandSourceStack source = ctx.getSource();
		if (!Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source)) {
			ServerPlayer self = source.getPlayer();
			for (ServerPlayer p : found) {
				if (self == null || !p.getUUID().equals(self.getUUID())) {
					throw new SimpleCommandExceptionType(
						Component.literal("Without operator permission you can only target yourself (use your own name).")).create();
				}
			}
		}
		return found;
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
		Collection<ServerPlayer> targets = targets(ctx);
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
		Collection<ServerPlayer> targets = targets(ctx);
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
		Collection<ServerPlayer> targets = targets(ctx);
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

	private static int settingsStatus(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		HandicapState s = HandicapManager.state();
		String timer = s.timerEnabled() ? "every " + formatDuration(s.timerSeconds()) : "off";
		String max = s.maxHandicaps() > 0 ? String.valueOf(s.maxHandicaps()) : "off";
		String duration = s.durationSeconds() > 0 ? formatDuration(s.durationSeconds()) : "off";
		final String text = "Timer: " + timer + " | Stacking: " + s.stacking() + " | Max: " + max + " | Duration: " + duration;
		ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		ctx.getSource().sendSuccess(() -> Component.literal("Usage: /handicap settings <timer|stacking|max|duration>"), false);
		return 1;
	}

	private static int maxStatus(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		int max = HandicapManager.state().maxHandicaps();
		final String text = max > 0
			? "Max: " + max + " handicaps. When a new one goes over it, the oldest is removed."
			: "Max is off: there is no limit.";
		ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		ctx.getSource().sendSuccess(() -> Component.literal("Usage: /handicap settings max <count|off>"), false);
		return 1;
	}

	private static int maxSet(CommandContext<CommandSourceStack> ctx, int max) {
		if (!ready(ctx)) {
			return 0;
		}
		HandicapManager.state().setMaxHandicaps(max);
		HandicapManager.state().save();
		final String text = max > 0
			? "Max set to " + max + " handicaps: the oldest is removed when a new one goes over it."
			: "Max turned off: there is no limit.";
		ctx.getSource().sendSuccess(() -> Component.literal(text), true);
		return 1;
	}

	private static int durationStatus(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		int seconds = HandicapManager.state().durationSeconds();
		final String text = seconds > 0
			? "Duration: each handicap wears off after " + formatDuration(seconds) + "."
			: "Duration is off: handicaps never wear off.";
		ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		ctx.getSource().sendSuccess(() -> Component.literal("Usage: /handicap settings duration <minutes|off>"), false);
		return 1;
	}

	private static int durationSet(CommandContext<CommandSourceStack> ctx, int seconds) {
		if (!ready(ctx)) {
			return 0;
		}
		if (seconds != 0 && seconds < 5) {
			ctx.getSource().sendFailure(Component.literal("Invalid duration: enter a number of minutes (minimum 0.1), or off."));
			return 0;
		}
		HandicapManager.state().setDurationSeconds(seconds);
		HandicapManager.state().save();
		final String text = seconds > 0
			? "Each handicap now wears off after " + formatDuration(seconds) + "."
			: "Duration turned off: handicaps never wear off.";
		ctx.getSource().sendSuccess(() -> Component.literal(text), true);
		return 1;
	}

	private static int stackingStatus(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		final String text = HandicapManager.state().stacking()
			? "Stacking is on: handicaps add up."
			: "Stacking is off: a new handicap replaces the previous one.";
		ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		ctx.getSource().sendSuccess(() -> Component.literal("Usage: /handicap settings stacking <true|false>"), false);
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
		final String usage = "Usage: /handicap settings timer <minutes|off>";
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
