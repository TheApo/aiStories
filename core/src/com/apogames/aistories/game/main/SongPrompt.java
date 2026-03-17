package com.apogames.aistories.game.main;

import com.apogames.aistories.game.objects.EnumInterface;
import com.apogames.aistories.game.objects.GameObjectives;
import com.apogames.aistories.game.settings.SongSettings;
import com.apogames.aistories.game.settings.StorySettings;

public class SongPrompt {

    public static String buildPromptTemplate(SongSettings settings) {
        String style = getStyleDescription(settings.getMusicStyle());
        String age = settings.getAgeDescription();
        String length = settings.getLengthDescription();
        String ageTone = getAgeTone(settings.getAgeGroup());
        String ageStructure = getAgeStructure(settings.getAgeGroup());

        return "Erstelle ein " + length + " im Stil von " + style + " fuer Hoerer im Alter von " + age + ".\n" +
                ageTone + "\n" +
                ageStructure + "\n" +
                "Der Text muss sich reimen (AABB Schema): Reimpaare muessen die gleiche Silbenanzahl haben (gleiches Versmass). Eingaengig und singbar.";
    }

    public static String buildFullPrompt(SongSettings settings, GameObjectives objectives) {
        String template = settings.getPromptTemplate();
        if (template == null || template.isEmpty()) {
            template = buildPromptTemplate(settings);
        }

        StringBuilder sb = new StringBuilder(template);
        sb.append("\n\n");

        if (objectives.getMainCharacter() != null) {
            sb.append("Hauptcharakter: ").append(objectives.getMainCharacter().getDisplayName());
            sb.append(" (").append(objectives.getMainCharacter().getDisplayDetails()).append("). ");
        }
        if (objectives.getSupportingCharacter() != null) {
            sb.append("Nebencharakter: ").append(objectives.getSupportingCharacter().getDisplayName());
            sb.append(" (").append(objectives.getSupportingCharacter().getDisplayDetails()).append("). ");
        }
        if (objectives.getUniverse() != null) {
            sb.append("Welt: ").append(objectives.getUniverse().getDisplayName());
            sb.append(" (").append(objectives.getUniverse().getDisplayDetails()).append("). ");
        }
        if (objectives.getPlaces() != null) {
            sb.append("Ort: ").append(objectives.getPlaces().getDisplayName());
            sb.append(" (").append(objectives.getPlaces().getDisplayDetails()).append("). ");
        }
        if (objectives.getObjectives() != null) {
            sb.append("Objekt: ").append(objectives.getObjectives().getDisplayName());
            sb.append(" (").append(objectives.getObjectives().getDisplayDetails()).append(").");
        }

        return sb.toString();
    }

    public static String buildCompactTemplate(SongSettings settings) {
        return getCompactStyle(settings.getMusicStyle())
                + " (" + settings.getSongLength().getLabel() + ")"
                + " fuer " + settings.getAgeDescription() + ". "
                + getCompactAgeTone(settings.getAgeGroup()) + " "
                + getCompactStructure(settings.getAgeGroup()) + " "
                + "Text muss sich reimen (AABB), Reimpaare gleiche Silbenanzahl, eingaengig und singbar.";
    }

    public static String buildObjectivesText(GameObjectives objectives) {
        String[][] elements = collectElementsFixed(objectives);
        StringBuilder sb = new StringBuilder();
        for (String[] el : elements) {
            if (el != null) {
                sb.append("\n").append(el[0]).append(": ").append(el[1]);
                if (el[2] != null && !el[2].isEmpty()) {
                    sb.append(" (").append(el[2]).append(")");
                }
            }
        }
        return sb.toString();
    }

    public static String buildLyricsPrompt(SongSettings settings, GameObjectives objectives) {
        String style = getStyleDescription(settings.getMusicStyle());
        String age = settings.getAgeDescription();
        String length = settings.getLengthDescription();
        String ageTone = getAgeTone(settings.getAgeGroup());
        String lengthStructure = getLyricsStructure(settings.getSongLength());

        StringBuilder sb = new StringBuilder();
        sb.append("Schreibe den Liedtext fuer ein ").append(length).append(" im Stil von ").append(style);
        sb.append(" fuer Hoerer im Alter von ").append(age).append(".\n\n");
        sb.append(ageTone).append("\n\n");
        sb.append(lengthStructure).append("\n\n");

        sb.append("WICHTIG: Halte dich EXAKT an die oben angegebene Struktur und Reihenfolge. ");
        sb.append("Nicht mehr und nicht weniger Abschnitte als angegeben. ");
        sb.append("Der [Chorus] hat immer den gleichen Text und wird nach jeder Strophe wiederholt. ");
        sb.append("Formatiere jeden Abschnitt mit dem passenden Tag auf eigener Zeile.\n\n");

        sb.append("REIMSCHEMA UND VERSMASS (ZWINGEND EINHALTEN):\n");
        sb.append("- Paarreim AABB: Jeweils zwei aufeinanderfolgende Zeilen muessen sich reimen.\n");
        sb.append("- Gleiches Versmass: Zwei Zeilen die sich reimen muessen die gleiche Silbenanzahl haben (z.B. beide 8 Silben).\n");
        sb.append("- Zaehle die Silben jeder Zeile und stelle sicher dass Reimpaare gleich lang sind.\n");
        sb.append("- Beispiel: 'Die Sonne scheint so warm und hell' (8 Silben) / 'Ich mag den Tag, er ist so schnell' (8 Silben).\n");
        sb.append("- Jede Zeile muss singbar sein: maximal 10 Woerter pro Zeile, keine langen Schachtelsaetze.\n");
        sb.append("- Schreibe KEINE Reimschema-Markierungen wie (A), (B) hinter die Zeilen.\n\n");

        sb.append("Gib NUR den Liedtext aus, keine Erklaerungen oder Kommentare.\n\n");

        if (settings.isIncludeObjectives()) {
            if (objectives.getMainCharacter() != null) {
                sb.append("Hauptcharakter: ").append(objectives.getMainCharacter().getDisplayName());
                sb.append(" (").append(objectives.getMainCharacter().getDisplayDetails()).append("). ");
            }
            if (objectives.getSupportingCharacter() != null) {
                sb.append("Nebencharakter: ").append(objectives.getSupportingCharacter().getDisplayName());
                sb.append(" (").append(objectives.getSupportingCharacter().getDisplayDetails()).append("). ");
            }
            if (objectives.getUniverse() != null) {
                sb.append("Welt: ").append(objectives.getUniverse().getDisplayName());
                sb.append(" (").append(objectives.getUniverse().getDisplayDetails()).append("). ");
            }
            if (objectives.getPlaces() != null) {
                sb.append("Ort: ").append(objectives.getPlaces().getDisplayName());
                sb.append(" (").append(objectives.getPlaces().getDisplayDetails()).append("). ");
            }
            if (objectives.getObjectives() != null) {
                sb.append("Objekt: ").append(objectives.getObjectives().getDisplayName());
                sb.append(" (").append(objectives.getObjectives().getDisplayDetails()).append(").");
            }
        }

        String template = settings.getPromptTemplate();
        if (template != null && !template.isEmpty()) {
            sb.append("\n\nZusaetzliche Anweisungen: ").append(template);
        }

        return sb.toString();
    }

    private static final int SUNO_STYLE_MAX = 1000;

    public static String buildSunoStyle(SongSettings settings) {
        String style = getStyleDescription(settings.getMusicStyle())
                + " fuer Hoerer im Alter von " + settings.getAgeDescription() + ". "
                + getAgeMusicTone(settings.getAgeGroup());
        if (style.length() > SUNO_STYLE_MAX) {
            style = style.substring(0, SUNO_STYLE_MAX);
        }
        return style;
    }

    private static String getAgeMusicTone(StorySettings.AgeGroup ageGroup) {
        switch (ageGroup) {
            case AGE_0_1:
                return "Warm, zart und beruhigend. Sanfte und viele Wiederholungen und weiche Klaenge, wie ein Wiegenlied zum Einschlummern.";
            case AGE_2_4:
                return "Froelich, verspielt und albern. Zum Mittanzen und Mitsingen einladend, mit einfachem eingaengigem Rhythmus.";
            case AGE_5_7:
                return "Froehlich, abenteuerlich und ermutigend. Eingaengiger Mitsing-Refrain mit Ohrwurm-Qualitaet.";
            case AGE_8_12:
                return "Energiegeladen, ermutigend und cool. Kraftvoller Refrain zum lauten Mitsingen.";
            case AGE_12_16:
                return "Emotional, kraeftig und authentisch. Direkt und echt, mit Intensitaet und Tiefe. Refrain braucht Ohrwurm Qualität.";
            case AGE_16_PLUS:
                return "Poetisch, nachdenklich und intensiv. Mit Gaensehaut-Momenten und emotionaler Tiefe, aber echtem Ohrwurm im Refrain.";
            default:
                return "Froelich, kindgerecht und mitsingbar.";
        }
    }

    private static String getLyricsStructure(SongSettings.SongLength length) {
        switch (length) {
            case SHORT:
                return "Struktur (EXAKT einhalten, ALLE Abschnitte schreiben):\n"
                        + "[Verse 1] (4 Zeilen)\n[Chorus] (4 Zeilen)\n[Chorus]\n[Outro] (2 Zeilen)\n"
                        + "Kurzes Lied, ca. 1:30-2:00 Minuten.";
            case MEDIUM:
                return "Struktur (EXAKT einhalten, ALLE Abschnitte schreiben):\n"
                        + "[Verse 1] (4-6 Zeilen)\n[Chorus] (4 Zeilen)\n[Verse 2] (4-6 Zeilen)\n[Chorus]\n[Bridge] (4 Zeilen)\n[Chorus]\n[Outro] (2-4 Zeilen)\n"
                        + "Mittellanges Lied, ca. 2:30-3:00 Minuten.";
            case LONG:
                return "Struktur (EXAKT einhalten, ALLE Abschnitte schreiben):\n"
                        + "[Intro] (2-4 Zeilen)\n[Verse 1] (6-8 Zeilen)\n[Chorus] (4-6 Zeilen)\n[Verse 2] (6-8 Zeilen)\n[Chorus]\n[Bridge] (4 Zeilen)\n[Chorus]\n[Verse 3] (6-8 Zeilen)\n[Chorus]\n[Outro] (2-4 Zeilen)\n"
                        + "Langes Lied, ca. 3:30-4:00 Minuten.";
            default:
                return "Struktur: [Verse 1], [Chorus], [Verse 2], [Chorus], [Outro].";
        }
    }

    private static final int SUNO_MAX = 500;

    // Detail removal order: Universe(2) → Places(3) → SupportingCharacter(1) → Objectives(4) → MainCharacter(0)
    private static final int[] DETAIL_REMOVAL_ORDER = {2, 3, 1, 4, 0};

    public static String buildSunoPrompt(SongSettings settings, GameObjectives objectives) {
        String template = settings.getPromptTemplate();
        String header;
        if (template != null && !template.isEmpty()) {
            header = template;
        } else {
            header = buildCompactTemplate(settings);
        }

        if (!settings.isIncludeObjectives()) {
            return header.length() <= SUNO_MAX ? header : header.substring(0, SUNO_MAX);
        }

        String[][] elements = collectElementsFixed(objectives);
        boolean[] includeDetails = new boolean[5];
        for (int i = 0; i < 5; i++) {
            includeDetails[i] = true;
        }

        // Step 1: Try with all details
        String prompt = buildWithFlags(header, elements, includeDetails);
        if (prompt.length() <= SUNO_MAX) {
            return prompt;
        }

        // Step 2: Remove details one by one in order: Universe → Places → Nebenfigur → Objekt → Hauptfigur
        for (int idx : DETAIL_REMOVAL_ORDER) {
            if (elements[idx] != null) {
                includeDetails[idx] = false;
                prompt = buildWithFlags(header, elements, includeDetails);
                if (prompt.length() <= SUNO_MAX) {
                    return prompt;
                }
            }
        }

        // Step 3: Names only still too long → no objectives, just header
        return header.length() <= SUNO_MAX ? header : header.substring(0, SUNO_MAX);
    }

    // Fixed positions: 0=MainCharacter, 1=SupportingCharacter, 2=Universe, 3=Places, 4=Objectives
    private static String[][] collectElementsFixed(GameObjectives objectives) {
        String[][] pairs = new String[5][];
        if (objectives.getMainCharacter() != null)
            pairs[0] = new String[]{"Hauptfigur", objectives.getMainCharacter().getDisplayName(), objectives.getMainCharacter().getDisplayDetails()};
        if (objectives.getSupportingCharacter() != null)
            pairs[1] = new String[]{"Nebenfigur", objectives.getSupportingCharacter().getDisplayName(), objectives.getSupportingCharacter().getDisplayDetails()};
        if (objectives.getUniverse() != null)
            pairs[2] = new String[]{"Welt", objectives.getUniverse().getDisplayName(), objectives.getUniverse().getDisplayDetails()};
        if (objectives.getPlaces() != null)
            pairs[3] = new String[]{"Ort", objectives.getPlaces().getDisplayName(), objectives.getPlaces().getDisplayDetails()};
        if (objectives.getObjectives() != null)
            pairs[4] = new String[]{"Objekt", objectives.getObjectives().getDisplayName(), objectives.getObjectives().getDisplayDetails()};
        return pairs;
    }

    private static String buildWithFlags(String header, String[][] elements, boolean[] includeDetails) {
        StringBuilder sb = new StringBuilder(header);
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null) {
                sb.append("\n").append(elements[i][0]).append(": ").append(elements[i][1]);
                if (includeDetails[i] && elements[i][2] != null && !elements[i][2].isEmpty()) {
                    sb.append(" (").append(elements[i][2]).append(")");
                }
            }
        }
        return sb.toString();
    }

    private static String getCompactStyle(SongSettings.MusicStyle style) {
        switch (style) {
            case POP: return "Eingaengiges Pop-Lied";
            case ROCK: return "Energisches Rock-Lied";
            case EIGHTIES: return "80er Synthpop-Lied";
            case HIPHOP: return "Rhythmisches HipHop-Lied";
            case LULLABY: return "Sanftes Schlaflied";
            case PIANO: return "Ruhiges emotionales Piano-Lied";
            case ELECTRONIC: return "Tanzbares Electronic-Lied";
            case MUSICAL: return "Dramatisches Musical-Lied";
            default: return "Eingaengiges Pop-Lied";
        }
    }

    private static String getCompactAgeTone(StorySettings.AgeGroup ageGroup) {
        switch (ageGroup) {
            case AGE_0_1: return "Ganz einfach, liebevoll, Lautmalerei, sanfte Wiederholungen.";
            case AGE_2_4: return "Verspielt, lustig, Tiergerausche, zum Mitmachen und Bewegen.";
            case AGE_5_7: return "Fantasievoll, abenteuerlich, Ohrwurm-Refrain, einfache Reime.";
            case AGE_8_12: return "Humorvoll, mutig, erzaehlerisch, kraftvoller Refrain.";
            case AGE_12_16: return "Emotional, ehrlich, kraeftig, authentische Gefuehle.";
            case AGE_16_PLUS: return "Poetisch, tiefgruendig, metaphernreich, intensiv.";
            default: return "Froelich, kindgerecht, mitsingbar.";
        }
    }

    private static String getCompactStructure(StorySettings.AgeGroup ageGroup) {
        switch (ageGroup) {
            case AGE_0_1: return "1-2 kurze Strophen, viele Wiederholungen.";
            case AGE_2_4: return "2 Strophen, einfacher Refrain 2x.";
            case AGE_5_7: return "2-3 Strophen, Refrain 2x.";
            case AGE_8_12: return "2-3 Strophen, Refrain 2-3x, optional Bridge.";
            case AGE_12_16: return "2-3 Strophen, emotionaler Refrain, Bridge.";
            case AGE_16_PLUS: return "2-3 Strophen, wandelbarer Refrain, Bridge.";
            default: return "2 Strophen, Refrain 2x.";
        }
    }

    private static String getStyleDescription(SongSettings.MusicStyle style) {
        switch (style) {
            case POP:
                return "Pop — eingaengige Melodie, klarer Beat, leicht mitzusingen, mit einem Ohrwurm-Refrain";
            case ROCK:
                return "Rock — energiegeladen, kraftvoller Rhythmus, treibende Gitarren-Atmosphaere, mit Dynamik zwischen ruhigen und lauten Passagen";
            case EIGHTIES:
                return "80er Synthpop — helle Synthesizer, elektronische Drum Machines, eingaengige Melodien, treibender Bass, polierter Sound mit Retro-Charme und Neon-Vibes";
            case HIPHOP:
                return "HipHop — rhythmischer Sprechgesang, wortgewandt und kreativ, mit cleverem Wortspiel, fliessenden Reimen und einem eingaengigen Hook";
            case LULLABY:
                return "Schlaflied/Wiegenlied — sanft, beruhigend, langsames Tempo, weiche Melodie, wiederholende Muster die zum Einschlafen einladen";
            case PIANO:
                return "Piano — ruhig, emotional, sanfte Klaviermelodie, gefuehlvoll und introspektiv, mit zartem Ausdruck und beruhigender Atmosphaere";
            case ELECTRONIC:
                return "Electronic/Dance — moderner, tanzbarer Beat, synthetische Klaenge, repetitiver eingaengiger Refrain der zum Bewegen einlaedt";
            case MUSICAL:
                return "Musical — theatralisch, dramatisch, mit emotionalen Hoehepunkten, ausdrueckstarkem Gesang und einer klaren Handlung im Lied";
            default:
                return "Pop — eingaengige Melodie, klarer Beat, leicht mitzusingen";
        }
    }

    private static String getAgeTone(StorySettings.AgeGroup ageGroup) {
        switch (ageGroup) {
            case AGE_0_1:
                return "Der Text soll ganz einfach und liebevoll sein: nur wenige Worte pro Zeile, viel Lautmalerei (la-la, du-du, brumm-brumm), " +
                        "sanfte Wiederholungen und weiche Klaenge. Themen: Geborgenheit, Naehe, Mama und Papa, Kuscheln, Schlafen. " +
                        "Der Ton ist warm, zart und beruhigend — wie ein Wiegenlied zum Einschlummern.";
            case AGE_2_4:
                return "Der Text soll einfach, verspielt und lustig sein: kurze Saetze, einfache Reime, Tiergerausche (miau, wau-wau, muh), " +
                        "Bewegungsaufforderungen (klatschen, huepfen, drehen). Themen: Tiere, Farben, Zahlen, Alltag (essen, spielen, baden), Quatsch und Spass. " +
                        "Der Ton ist froelich, albern und zum Mitmachen einladend — Kinder sollen mittanzen und mitsingen wollen.";
            case AGE_5_7:
                return "Der Text soll eingaengig, fantasievoll und mitreissend sein: klare Reime, ein starker Mitsing-Refrain, einfache aber lebendige Sprache. " +
                        "Themen: Abenteuer, Freundschaft, Fantasiewelten, Superhelden, magische Tiere, Mut und Zusammenhalt. " +
                        "Der Ton ist froehlich, abenteuerlich und ermutigend — der Refrain soll ein echter Ohrwurm sein.";
            case AGE_8_12:
                return "Der Text soll lebendig, humorvoll und abwechslungsreich sein: die Strophen erzaehlen eine kleine Geschichte, " +
                        "der Refrain ist kraftvoll und eingaengig. Themen: Mut, Selbstvertrauen, Freundschaft, Abenteuer, Traeume verwirklichen, " +
                        "Humor und Witz. Der Ton ist energiegeladen, ermutigend und cool — ein Lied das man laut mitsingen will.";
            case AGE_12_16:
                return "Der Text soll emotional, ehrlich und kraftvoll sein: anspruchsvollere Sprache, tiefere Reime, " +
                        "authentische Gefuehle statt Kitsch. Themen: Identitaet, Zusammenhalt, erste grosse Gefuehle, Freiheit, " +
                        "sich gegen Erwartungen behaupten, Freundschaft die traegt, Zweifel und Staerke. " +
                        "Der Ton ist kraeftig, direkt und echt — wie ein Song den man auf dem Schulweg laut hoert.";
            case AGE_16_PLUS:
                return "Der Text soll poetisch, emotional komplex und anspruchsvoll sein: metaphernreiche Sprache, " +
                        "unerwartete Bilder, tiefgruendige Gedanken. Themen: Liebe und Verlust, Identitaetssuche, gesellschaftliche Fragen, " +
                        "Sehnsucht, Aufbruch, innere Konflikte. " +
                        "Der Ton ist literarisch, nachdenklich und intensiv — ein Lied mit Gaensehaut-Momenten und Tiefgang.";
            default:
                return "Der Text soll lebendig, humorvoll und mitsingbar sein, passend fuer Kinder.";
        }
    }

    private static String getAgeStructure(StorySettings.AgeGroup ageGroup) {
        switch (ageGroup) {
            case AGE_0_1:
                return "Struktur: Sehr kurz, 1-2 Strophen mit vielen Wiederholungen. Jede Zeile maximal 4-5 Worte. Der Refrain besteht aus 2-3 sich wiederholenden Zeilen.";
            case AGE_2_4:
                return "Struktur: 2 kurze Strophen und ein einfacher Refrain der mindestens 2x vorkommt. Zeilen mit 5-8 Worten. Wiederholungen sind erwuenscht.";
            case AGE_5_7:
                return "Struktur: 2-3 Strophen und ein eingaengiger Refrain der 2x vorkommt. Optional eine Bridge vor dem letzten Refrain.";
            case AGE_8_12:
                return "Struktur: 2-3 Strophen die eine Geschichte erzaehlen, ein kraftvoller Refrain (2-3x), und optional eine Bridge.";
            case AGE_12_16:
                return "Struktur: 2-3 inhaltlich aufbauende Strophen, ein emotionaler Refrain (2-3x), eine kontrastierende Bridge. Die letzte Strophe oder der letzte Refrain darf variiert werden fuer einen starken Abschluss.";
            case AGE_16_PLUS:
                return "Struktur: Freiere Form erlaubt. 2-3 Strophen mit Entwicklung, ein Refrain der sich im Verlauf des Liedes in seiner Bedeutung wandeln darf, eine Bridge mit emotionalem Hoehepunkt.";
            default:
                return "Struktur: Mindestens 1 Strophe und 2 Refrains.";
        }
    }
}
