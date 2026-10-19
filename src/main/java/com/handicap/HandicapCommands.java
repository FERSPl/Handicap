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
	private static final Pattern DURATION = Pattern.compile("(\\d{1,6})([smh]?)");

	private static final SuggestionProvider<CommandSourceStack> HANDICAP_SUGGESTIONS = (ctx, builder) -> {
		List<String> options = new ArrayList<>(List.of("all", "random"));
		for (Handicap d : Handicap.values()) {
			options.add(d.id);
		}
		return SharedSuggestionProvider.suggest(options, builder);
	};

	private static final SuggestionProvider<CommandSourceStack> TIMER_SUGGESTIONS = (ctx, builder) ->
		SharedSuggestionProvider.suggest(List.of("off", "5m", "10m", "30m", "1h"), builder);

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
				.then(Commands.argument("targets", EntityArgument.players())
					.then(Commands.argument("handicap", StringArgumentType.word())
						.suggests(HANDICAP_SUGGESTIONS)
						.executes(ctx -> modify(ctx, false)))))
			.then(Commands.literal("list")
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(HandicapCommands::list)))
			.then(Commands.literal("next")
				.then(Commands.argument("targets", EntityArgument.players())
					.executes(HandicapCommands::next)))
			.then(Commands.literal("reset")
				.executes(HandicapCommands::reset))
			.then(Commands.literal("timer")
				.executes(HandicapCommands::timerStatus)
				.then(Commands.argument("value", StringArgumentType.word())
					.suggests(TIMER_SUGGESTIONS)
					.executes(HandicapCommands::timerSet)));
	}

	private static boolean ready(CommandContext<CommandSourceStack> ctx) {
		if (HandicapManager.state() == null) {
			ctx.getSource().sendFailure(Component.literal("Le mod n'est pas encore prêt."));
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
				ctx.getSource().sendFailure(Component.literal("Handicap inconnu : " + value));
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
				p.sendSystemMessage(Component.literal(give ? "Tu as reçu tous les handicaps." : "Tous tes handicaps ont été retirés."));
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
			(give ? "Handicaps donnés : " : "Handicaps retirés : ") + count + " (joueurs ciblés : " + players + ")"), true);
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
				ctx.getSource().sendSuccess(() -> Component.literal("Tu ne vois pas tes handicaps."), false);
				continue;
			}
			StringBuilder sb = new StringBuilder();
			for (Handicap d : HandicapManager.state().get(p.getUUID())) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(d.displayName);
			}
			final String text = p.getName().getString() + " : " + (sb.length() == 0 ? "aucun handicap" : sb);
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
		ctx.getSource().sendSuccess(() -> Component.literal("Prochain handicap donné à " + c + " joueur(s). Timer relancé."), true);
		return count;
	}

	private static int reset(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		HandicapManager.state().resetAll();
		for (ServerPlayer p : ctx.getSource().getServer().getPlayerList().getPlayers()) {
			HandicapManager.refresh(p);
		}
		HandicapManager.state().save();
		ctx.getSource().sendSuccess(() -> Component.literal("Tous les handicaps ont été retirés et le timer est relancé."), true);
		return 1;
	}

	private static int timerStatus(CommandContext<CommandSourceStack> ctx) {
		if (!ready(ctx)) {
			return 0;
		}
		HandicapState s = HandicapManager.state();
		final String text = s.timerEnabled()
			? "Prochain handicap dans " + s.secondsRemaining() + " s (intervalle : " + s.timerSeconds() + " s)."
			: "Le timer est désactivé.";
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
			ctx.getSource().sendSuccess(() -> Component.literal("Timer désactivé."), true);
			return 1;
		}
		int seconds = parseSeconds(value);
		if (seconds < 5) {
			ctx.getSource().sendFailure(Component.literal("Durée invalide (exemples : off, 30s, 10m, 1h ; minimum 5 s)."));
			return 0;
		}
		s.setTimerSeconds(seconds);
		s.save();
		ctx.getSource().sendSuccess(() -> Component.literal("Un handicap sera donné toutes les " + seconds + " secondes."), true);
		return 1;
	}

	private static int parseSeconds(String value) {
		Matcher m = DURATION.matcher(value);
		if (!m.matches()) {
			return -1;
		}
		long n = Long.parseLong(m.group(1));
		String unit = m.group(2);
		long seconds = switch (unit) {
			case "m" -> n * 60;
			case "h" -> n * 3600;
			default -> n;
		};
		return seconds > 1_000_000 ? -1 : (int) seconds;
	}
}
