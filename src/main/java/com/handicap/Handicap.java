package com.handicap;

import java.util.Locale;

public enum Handicap {
	BLIND("blind", "Aveugle", "Cécité permanente"),
	MUTE("mute", "Muet", "Tu ne peux plus parler dans le chat"),
	CANCER("cancer", "Cancer", "Ta santé maximale diminue peu à peu"),
	AMPUTATED_OFFHAND("amputated_offhand", "Main secondaire amputée", "Ta deuxième main a disparu"),
	SHORT("short", "Petite taille", "Tu es plus petit et ta portée est réduite"),
	ASTHMA("asthma", "Asthme", "Courir trop longtemps te coupe le souffle"),
	NARCOLEPSY("narcolepsy", "Narcolepsie", "Tu t'endors n'importe quand"),
	POLLEN_ALLERGY("pollen_allergy", "Allergie au pollen", "Les fleurs t'empoisonnent"),
	INSOMNIA("insomnia", "Insomnie", "Tu ne peux pas dormir"),
	ARTHRITIS("arthritis", "Arthrite", "Miner et frapper est plus lent"),
	BRITTLE_BONES("brittle_bones", "Os fragiles", "Tu subis deux fois plus de dégâts de chute"),
	DEMENTIA("dementia", "Démence", "Les objets de ton inventaire changent de place"),
	TOURETTES("tourettes", "Tourette", "Ta caméra est secouée et tu dis des choses dans le chat"),
	AMPUTATED_LEG("amputated_leg", "Jambe amputée", "Tu dois sauter pour te déplacer"),
	KLEPTOMANIA("kleptomania", "Kleptomanie", "Tu ne peux plus jeter d'objets"),
	LACTOSE_INTOLERANCE("lactose_intolerance", "Intolérance au lactose", "Boire du lait te tue instantanément"),
	DIABETES("diabetes", "Diabète", "Plus un aliment est sucré, plus il te fait mal"),
	SEIZURES("seizures", "Épilepsie", "Dégâts, faim ou effets négatifs peuvent causer des crises"),
	OCD("ocd", "TOC", "Tu as des compulsions à satisfaire"),
	CEREBRAL_PALSY("cerebral_palsy", "Paralysie cérébrale", "Mouvements incontrôlés aléatoires"),
	PARKINSONS("parkinsons", "Parkinson", "Ton viseur tremble en permanence"),
	ADHD("adhd", "TDAH", "Ton emplacement de barre d'action change au hasard"),
	VERTIGO("vertigo", "Vertige", "Le monde se met à tourner de temps en temps"),
	SCHIZOPHRENIA("schizophrenia", "Schizophrénie", "Tu vois et entends des choses qui n'existent pas"),
	ANOSOGNOSIA("anosognosia", "Anosognosie", "Tu ne vois plus tes handicaps"),
	DEAF("deaf", "Sourd", "Tu n'entends plus rien");

	public final String id;
	public final String displayName;
	public final String description;

	Handicap(String id, String displayName, String description) {
		this.id = id;
		this.displayName = displayName;
		this.description = description;
	}

	public int bit() {
		return 1 << ordinal();
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
