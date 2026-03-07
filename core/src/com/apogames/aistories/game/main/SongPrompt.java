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
                "Der Text soll sich reimen (AABB Schema), eingaengig und singbar sein.";
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

    /**
     * Compact prompt for Suno API (max 490 chars, non-custom mode limit is 500).
     */
    private static final int SUNO_MAX = 490;

    public static String buildSunoPrompt(SongSettings settings, GameObjectives objectives) {
        String header = getCompactStyle(settings.getMusicStyle())
                + " fuer " + settings.getAgeDescription() + ". "
                + getCompactAgeTone(settings.getAgeGroup()) + " "
                + getCompactStructure(settings.getAgeGroup()) + " "
                + "Text muss sich reimen (AABB), eingaengig und singbar sein.";

        // Collect active elements
        String[][] elements = collectElements(objectives);

        // Step 1: Try full prompt with all details
        String full = buildWithDetails(header, elements, Integer.MAX_VALUE);
        if (full.length() <= SUNO_MAX) {
            return full;
        }

        // Step 2: Distribute remaining space for shortened details
        String namesOnly = buildWithDetails(header, elements, 0);
        int remaining = SUNO_MAX - namesOnly.length();
        if (remaining > 0 && elements.length > 0) {
            int perElement = remaining / elements.length;
            if (perElement >= 20) {
                String shortened = buildWithDetails(header, elements, perElement);
                if (shortened.length() <= SUNO_MAX) {
                    return shortened;
                }
            }
        }

        // Step 3: Names only, no details
        if (namesOnly.length() <= SUNO_MAX) {
            return namesOnly;
        }

        // Step 4: Fallback truncate
        return namesOnly.substring(0, SUNO_MAX);
    }

    private static String[][] collectElements(GameObjectives objectives) {
        String[][] pairs = new String[5][];
        int count = 0;
        if (objectives.getMainCharacter() != null) pairs[count++] = new String[]{"Hauptfigur", objectives.getMainCharacter().getDisplayName(), objectives.getMainCharacter().getDisplayDetails()};
        if (objectives.getSupportingCharacter() != null) pairs[count++] = new String[]{"Nebenfigur", objectives.getSupportingCharacter().getDisplayName(), objectives.getSupportingCharacter().getDisplayDetails()};
        if (objectives.getUniverse() != null) pairs[count++] = new String[]{"Welt", objectives.getUniverse().getDisplayName(), objectives.getUniverse().getDisplayDetails()};
        if (objectives.getPlaces() != null) pairs[count++] = new String[]{"Ort", objectives.getPlaces().getDisplayName(), objectives.getPlaces().getDisplayDetails()};
        if (objectives.getObjectives() != null) pairs[count++] = new String[]{"Objekt", objectives.getObjectives().getDisplayName(), objectives.getObjectives().getDisplayDetails()};
        String[][] result = new String[count][];
        System.arraycopy(pairs, 0, result, 0, count);
        return result;
    }

    private static String buildWithDetails(String header, String[][] elements, int maxDetailLen) {
        StringBuilder sb = new StringBuilder(header);
        for (String[] el : elements) {
            sb.append("\n").append(el[0]).append(": ").append(el[1]);
            if (maxDetailLen > 0 && el[2] != null && !el[2].isEmpty()) {
                String details = el[2];
                if (details.length() > maxDetailLen) {
                    details = truncateAtWord(details, maxDetailLen);
                }
                if (!details.isEmpty()) {
                    sb.append(" (").append(details).append(")");
                }
            }
        }
        return sb.toString();
    }

    private static String truncateAtWord(String text, int maxLen) {
        if (text.length() <= maxLen) return text;
        int cut = text.lastIndexOf(' ', maxLen);
        if (cut <= 0) cut = maxLen;
        return text.substring(0, cut);
    }

    private static String getCompactStyle(SongSettings.MusicStyle style) {
        switch (style) {
            case POP: return "Eingaengiges Pop-Lied";
            case ROCK: return "Energisches Rock-Lied";
            case COUNTRY: return "Warmes Country-Lied";
            case HIPHOP: return "Rhythmisches HipHop-Lied";
            case LULLABY: return "Sanftes Schlaflied";
            case FOLK: return "Akustisches Folk-Lied";
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
            case COUNTRY:
                return "Country — warme, erzaehlerische Stimmung, akustische Gitarren-Atmosphaere, bodenstaendig und herzlich, mit einer kleinen Geschichte im Text";
            case HIPHOP:
                return "HipHop — rhythmischer Sprechgesang, wortgewandt und kreativ, mit cleverem Wortspiel, fliessenden Reimen und einem eingaengigen Hook";
            case LULLABY:
                return "Schlaflied/Wiegenlied — sanft, beruhigend, langsames Tempo, weiche Melodie, wiederholende Muster die zum Einschlafen einladen";
            case FOLK:
                return "Folk — akustisch, natuerlich, mit erzaehlerischem Charakter, Gemeinschaftsgefuehl und einem zeitlosen Klang zum Mitsingen";
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
