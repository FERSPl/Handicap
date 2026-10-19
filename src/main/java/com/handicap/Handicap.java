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
	OCD("ocd", "OCD", "You have compulsions you must obey"),
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
	CELIAC("celiac", "Celiac Disease", "Bread and cookies make you sick"),
	TINNITUS("tinnitus", "Tinnitus", "A ringing noise plays from time to time");

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
