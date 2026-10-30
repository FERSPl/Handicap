package com.handicap;

import java.util.Locale;

public enum Handicap {
	BLIND("blind", "Blind", "Permanent blindness"),
	MUTE("mute", "Mute", "You can't speak in chat"),
	CANCER("cancer", "Cancer", "Your maximum health slowly wastes away"),
	AMPUTATED_OFFHAND("amputated_offhand", "Amputated Offhand", "Your offhand is gone"),
	SHORT("short", "Short", "You are smaller and your reach is shorter"),
	ASTHMA("asthma", "Asthma", "Sprinting for too long leaves you out of breath"),
	NARCOLEPSY("narcolepsy", "Narcolepsy", "You randomly fall asleep"),
	POLLEN_ALLERGY("pollen_allergy", "Pollen Allergy", "Flowers nearby poison you"),
	INSOMNIA("insomnia", "Insomnia", "You can't sleep"),
	ARTHRITIS("arthritis", "Arthritis", "Mining and attacking are slower"),
	BRITTLE_BONES("brittle_bones", "Brittle Bones", "You take twice as much fall damage"),
	DEMENTIA("dementia", "Dementia", "Items move around in your inventory"),
	TOURETTES("tourettes", "Tourette's", "Your camera jerks and you say random things in chat"),
	AMPUTATED_LEG("amputated_leg", "Amputated Leg", "You must jump to move around"),
	KLEPTOMANIA("kleptomania", "Kleptomania", "You can't drop items"),
	LACTOSE_INTOLERANCE("lactose_intolerance", "Lactose Intolerance", "Drinking milk kills you instantly"),
	DIABETES("diabetes", "Diabetes", "The sweeter the food, the more it hurts you"),
	SEIZURES("seizures", "Seizures", "Damage, low hunger or bad effects can trigger seizures"),
	OCD("ocd", "OCD", "Ignore a compulsion and lose half a heart every second until you obey"),
	CEREBRAL_PALSY("cerebral_palsy", "Cerebral Palsy", "Random involuntary movements"),
	PARKINSONS("parkinsons", "Parkinson's", "Your crosshair shakes constantly"),
	ADHD("adhd", "ADHD", "Your selected hotbar slot changes at random"),
	VERTIGO("vertigo", "Vertigo", "The world randomly starts to spin"),
	SCHIZOPHRENIA("schizophrenia", "Schizophrenia", "You see and hear things that aren't real"),
	ANOSOGNOSIA("anosognosia", "Anosognosia", "You can't see your own handicaps"),
	DEAF("deaf", "Deaf", "You can't hear anything"),
	SCOLIOSIS("scoliosis", "Scoliosis", "Your view is permanently tilted"),
	COLOR_BLIND("color_blind", "Color Blind", "You see in black and white"),
	DYSLEXIA("dyslexia", "Dyslexia", "Most text becomes hard to read"),
	PARAPLEGIA("paraplegia", "Paraplegia", "You can't jump or sprint"),
	REDUCED_MOBILITY("reduced_mobility", "Reduced Mobility", "You move more slowly"),
	CELIAC("celiac", "Celiac Disease", "Eating bread or cookies is fatal"),
	TINNITUS("tinnitus", "Tinnitus", "A quiet ringing plays regularly"),
	MUSCLE_WEAKNESS("muscle_weakness", "Muscle Weakness", "Your melee damage is reduced"),
	RESPIRATORY_FAILURE("respiratory_failure", "Respiratory Failure", "You run out of air much faster underwater"),
	HYPOTENSION("hypotension", "Hypotension", "Episodes of weakness and slowness"),
	POOR_CIRCULATION("poor_circulation", "Poor Circulation", "Standing still for too long slows you down"),
	CLUMSINESS("clumsiness", "Clumsiness", "You sometimes drop the item in your hand"),
	FISH_ALLERGY("fish_allergy", "Fish Allergy", "Eating fish is fatal"),
	CLAUSTROPHOBIA("claustrophobia", "Claustrophobia", "Tight spaces slow you down"),
	SLOW_REFLEXES("slow_reflexes", "Slow Reflexes", "You are slowed down after taking damage"),
	MONSTER_PHOBIA("monster_phobia", "Monster Phobia", "Nearby monsters weaken and slow you"),
	PARTIAL_DEAFNESS("partial_deafness", "Partial Deafness", "All sounds are played at one fifth volume"),
	INVERTED_VISION("inverted_vision", "Inverted Vision", "Your view is permanently flipped upside down"),
	HICCUPS("hiccups", "Hiccups", "Random hiccups jolt your view"),
	SNEEZING("sneezing", "Sneezing", "Sneezing fits jolt your view and slow you down"),
	JUMP_CURSE("jump_curse", "Jump Curse", "You jump on your own more often"),
	MAGNET_FEET("magnet_feet", "Magnet Feet", "You can't jump for a moment after landing"),
	SLEEPWALKING("sleepwalking", "Sleepwalking", "You sometimes walk forward on your own"),
	STICKY_HAND("sticky_hand", "Sticky Hand", "You can't switch hotbar slots for a moment after switching"),
	MYOPIA("myopia", "Myopia", "Your vision is blurry"),
	HYPEROPIA("hyperopia", "Hyperopia", "The bottom third of your screen is blurry"),
	TUNNEL_VISION("tunnel_vision", "Tunnel Vision", "Your field of view is permanently narrowed"),
	PHANTOM_LIGHT("phantom_light", "Phantom Light", "Soft glows of light appear from time to time"),
	SPATIAL_DISORIENTATION("spatial_disorientation", "Spatial Disorientation", "Left and right are swapped"),
	ARMOR_INTOLERANCE("armor_intolerance", "Armor Intolerance", "You can't wear armor"),
	DRUNK("drunk", "Drunk", "The ground is slippery and your movement feels unsteady"),
	SLEEP_APNEA("sleep_apnea", "Sleep Apnea", "You wake up gasping for air and feel slow"),
	HAND_TREMORS("hand_tremors", "Hand Tremors", "Your hands shake and interrupt mining"),
	VISUAL_AGNOSIA("visual_agnosia", "Visual Agnosia", "Colors blend together"),
	MIGRAINE("migraine", "Migraine", "Migraine episodes blur and brighten the edges of your screen"),
	BROKEN_ARM("broken_arm", "Broken Arm", "Breaking blocks takes much longer"),
	PROSOPAGNOSIA("prosopagnosia", "Prosopagnosia", "You can't recognize faces: players, villagers and monsters all look alike");

	public final String id;
	public final String displayName;
	public final String description;

	Handicap(String id, String displayName, String description) {
		this.id = id;
		this.displayName = displayName;
		this.description = description;
	}

	public long bit() {
		return 1L << ordinal();
	}

	public static Handicap byId(String id) {
		String wanted = id.toLowerCase(Locale.ROOT);
		for (Handicap d : values()) {
			if (d.id.equals(wanted)) {
				return d;
			}
		}
		return null;
	}
}
