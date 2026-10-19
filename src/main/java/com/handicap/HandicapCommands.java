package com.handicap;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class HandicapCommands {
	private static final Pattern DURATION = Pattern.compile("(\\d{1,5}(?:\\.\\d{1,2})?)([smh]?)");

	private static final SuggestionProvider<CommandSourceStack> HANDICAP_SUGGESTIONS = (ctx, builder) -> {
		List<String> options = new ArrayList<>(List.of("all", "random"));
		for (Handicap d : Handicap.values()) {
			options.add(d.id);
		}
		return SharedSuggestionProvider.suggest(options, builder);
	};

	private static final SuggestionProvider<CommandSourceStack> TIMER_SUGGESTIONS = (ctx, builder) ->
		SharedSuggestionProvider.suggest(List.of("off", "1", "5", "10", "30s", "0.5"), builder);

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
			.then(Commands.literal("remove")
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
			.then(Commands.literal("timer")
				.executes(HandicapCommands::timerStatus)
				.then(Commands.argument("value", StringArgumentType.word())
					.suggests(TIMER_SUGGESTIONS)
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

	private static int timerStatus(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		HandicapState s = HandicapManager.state();
		final String text = s.timerEnabled()
			? "Next handicap in " + formatDuration(s.secondsRemaining()) + " (interval: " + formatDuration(s.timerSeconds()) + ")."
			: "The timer is off.";
		ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		return 1;
	}

	private static int timerSet(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		HandicapState s = HandicapManager.state();
		String value = StringArgumentType.getString(ctx, "value").toLowerCase(Locale.ROOT);
		if (value.equals("off")) {
			s.setTimerEnabled(false);
			s.save();
			ctx.getSource().sendSuccess(() -> Component.literal("Timer turned off."), true);
			return 1;
		}
		int seconds = parseSeconds(value);
		if (seconds < 5) {
			ctx.getSource().sendFailure(Component.literal("Invalid duration (minutes by default: off, 1, 0.5, 30s, 10m, 1h; minimum 5 s)."));
			return 0;
		}
		s.setTimerSeconds(seconds);
		s.save();
		ctx.getSource().sendSuccess(() -> Component.literal("A handicap will be given every " + formatDuration(seconds) + "."), true);
		return 1;
	}

	private static int parseSeconds(String value) {
		Matcher m = DURATION.matcher(value);
		if (!m.matches()) {
			return -1;
		}
		double n = Double.parseDouble(m.group(1));
		String unit = m.group(2);
		double seconds = switch (unit) {
			case "s" -> n;
			case "h" -> n * 3600;
			default -> n * 60;
		};
		long rounded = Math.round(seconds);
		return rounded > 1_000_000 ? -1 : (int) rounded;
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
