package com.apogames.aistories.game.main;

import com.apogames.aistories.game.objects.GameObjectives;
import com.apogames.aistories.game.settings.StorySettings;
import com.apogames.common.Localization;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
public class Prompt {

    public static final String DIRECTORY = "data/";

    private String systemSetting;

    private String prompt;

    private String fileNameMP3;

    private String fileNameTxt;

    private final GameObjectives gameObjectives = new GameObjectives();

    private StorySettings storySettings;

    public Prompt() {
        this.storySettings = new StorySettings();
        this.systemSetting = buildSystemSetting(storySettings);
        this.setUpShufflePrompt();
    }

    public void updateFromSettings(StorySettings settings) {
        this.storySettings = settings;
        this.systemSetting = buildSystemSetting(settings);
    }

    public void setUpShufflePrompt() {
        this.gameObjectives.shuffle();
        this.setUpPrompt();
    }

    public void setUpPrompt() {
        String template = storySettings.getPromptTemplate();
        if (template == null || template.isEmpty()) {
            template = buildPromptTemplate(storySettings);
        }

        String characterBlock = buildCharacterBlock();

        this.prompt = template + "\n\n" + characterBlock;

        addLanguageInstruction();
        buildFileNames();
    }

    public String buildPromptTemplate(StorySettings settings) {
        String age = settings.getAgeDescription();
        int charCount = settings.getStoryLength().getCharCount();
        String chapters = settings.getStoryLength().getChapters();
        String complexity = settings.getComplexityDescription();

        String typeIntro;
        String typeStyle;
        switch (settings.getStoryType()) {
            case FRIENDSHIP:
                typeIntro = "Schreibe eine herzewaermende Freundschaftsgeschichte fuer Kinder im Alter von " + age + ".";
                typeStyle = "Die Erzaehlweise sollte " + complexity + ", einfuehlsam und beruehrend sein, mit einer positiven Botschaft ueber wahre Freundschaft.";
                break;
            case DETECTIVE:
                typeIntro = "Schreibe eine spannende Detektivgeschichte mit Raetseln und Hinweisen fuer Kinder im Alter von " + age + ".";
                typeStyle = "Die Erzaehlweise sollte " + complexity + " und spannend sein, mit geschickt platzierten Hinweisen und einer ueberraschenden Aufloesung.";
                break;
            case ADVENTURE:
                typeIntro = "Schreibe eine aufregende Abenteuergeschichte voller Action und Entdeckungen fuer Kinder im Alter von " + age + ".";
                typeStyle = "Die Erzaehlweise sollte " + complexity + " und mitreissend sein, mit lebhaften Beschreibungen und dynamischen Szenen.";
                break;
            case FAIRYTALE:
                typeIntro = "Schreibe ein klassisches Maerchen mit modernem Touch fuer Kinder im Alter von " + age + ".";
                typeStyle = "Die Erzaehlweise sollte " + complexity + " und maerchenhaft sein, mit fantastischen Elementen und einer klaren Moral. Und starte auf jeden Fall mit Es war einmal. Es muss als Märchen erkannt werden.";
                break;
            default:
                typeIntro = "Schreibe eine beruhigende, fantasievolle und liebevolle Gute-Nacht-Geschichte fuer Kinder im Alter von " + age + ".";
                typeStyle = "Die Erzaehlweise sollte " + complexity + " und beruhigend sein, mit einem sanften, friedlichen Ende, das zum Einschlafen einlaedt.";
                break;
        }

        return typeIntro + "\n" +
                "Die Geschichte soll gut strukturiert, in " + chapters + " Kapiteln unterteilt und etwa " + charCount + " Zeichen lang sein.\n" +
                "Sie soll eine Mischung aus Humor, Abenteuer und Emotionen enthalten, sodass Kinder sich mit den Charakteren identifizieren koennen.\n\n" +
                typeStyle + "\n" +
                "Achte darauf, dass die Geschichte eine runde Handlung mit einer spannenden Einleitung, einer abenteuerlichen Mitte und einem schoenen Abschluss hat.";
    }

    public String buildSystemSetting(StorySettings settings) {
        String base;
        switch (settings.getAgeGroup()) {
            case AGE_0_1:
                base = "Du erzaehlst Geschichten fuer die Allerkleinsten. Verwende nur sehr einfache Woerter, kurze Saetze und viele Wiederholungen. Lautmalerei und einfache Reime sind willkommen.";
                break;
            case AGE_2_4:
                base = "Du bist ein liebevoller Geschichtenerzaehler fuer kleine Kinder. Verwende einfache Sprache, kurze Saetze und vertraute Alltagssituationen. Wiederholungen und einfache Reime machen deine Geschichten besonders.";
                break;
            case AGE_5_7:
                base = "Du bist ein talentierter Kinderbuchautor mit lebendiger Fantasie. Verwende kindgerechte Sprache mit kurzen, klaren Saetzen. Deine Geschichten sind lustig, fantasievoll und leicht verstaendlich.";
                break;
            case AGE_12_16:
                base = "Du bist ein erfahrener Jugendbuchautor, der fesselnde Geschichten mit Tiefgang schreibt. Deine Charaktere sind vielschichtig, die Handlung komplex und spannend. Du traust deinen Lesern anspruchsvollere Themen zu.";
                break;
            case AGE_16_PLUS:
                base = "Du bist ein versierter Autor, der anspruchsvolle Geschichten mit literarischem Anspruch schreibt. Deine Werke zeichnen sich durch komplexe Handlungen, philosophische Untertoene und vielschichtige Charaktere aus.";
                break;
            default:
                base = "Du bist ein talentierter Kinderbuchautor, der spannende, fantasievolle und lustige Geschichten schreibt. Dein Stil erinnert an Astrid Lindgren und Erich Kaestner, mit lebendigen Figuren, liebevollen Details und einem Hauch von Abenteuer.";
                break;
        }

        String typeFlavor;
        switch (settings.getStoryType()) {
            case FRIENDSHIP:
                typeFlavor = " Deine Geschichten zeigen den Wert wahrer Freundschaft und machen Mut.";
                break;
            case DETECTIVE:
                typeFlavor = " Deine Geschichten sind voller Raetsel, Hinweise und ueberraschender Wendungen, die zum Mitraten einladen.";
                break;
            case ADVENTURE:
                typeFlavor = " Deine Geschichten stecken voller Abenteuer, mutiger Entscheidungen und aufregender Entdeckungen.";
                break;
            case FAIRYTALE:
                typeFlavor = " Deine Geschichten verbinden klassische Maerchenelemente mit modernen Themen und einer klaren Moral.";
                break;
            default:
                typeFlavor = " Deine Geschichten laden zum Traeumen ein und haben ein sanftes, beruhigendes Ende.";
                break;
        }

        return base + typeFlavor;
    }

    private String buildCharacterBlock() {
        return "Die Geschichte beinhaltet folgende Elemente:\n" +
                "Hauptcharakter: " + gameObjectives.getMainCharacter().getDisplayName() + ", \n" +
                "Nebencharakter: " + gameObjectives.getSupportingCharacter().getDisplayName() + ", \n" +
                "Spielort: " + gameObjectives.getPlaces().getDisplayName() + ", \n" +
                "Universum: " + gameObjectives.getUniverse().getDisplayName() + ", \n" +
                "Ein zentrales magisches oder wichtiges Objekt: " + gameObjectives.getObjectives().getDisplayName() + ".\n\n" +
                "Die Geschichte kann (muss aber nicht komplett) folgende Fragen beantworten:\n" +
                "- Was macht den Hauptcharakter besonders? " + gameObjectives.getMainCharacter().getDisplayDetails() + "\n" +
                "- Welche lustigen oder aussergewoehnlichen Eigenschaften hat der Nebencharakter? " + gameObjectives.getSupportingCharacter().getDisplayDetails() + "\n" +
                "- Welche geheimnisvollen oder magischen Elemente gibt es im Universum? " + gameObjectives.getUniverse().getDisplayDetails() + "\n" +
                "- Welche Herausforderungen oder Ueberraschungen gibt es am Ort der Handlung? " + gameObjectives.getPlaces().getDisplayDetails() + "\n" +
                "- Welche besondere Bedeutung hat das Objekt und wie beeinflusst es die Handlung? " + gameObjectives.getObjectives().getDisplayDetails();
    }

    private void addLanguageInstruction() {
        if (Localization.getInstance().getLocale().equals(Locale.ENGLISH)) {
            this.prompt += "\n Schreibe die Geschichte in einem freundlichen Englisch.";
        } else if (Localization.getInstance().getLocale().equals(Locale.FRENCH)) {
            this.prompt += "\n Schreibe die Geschichte in einem freundlichen Franzoesisch.";
        } else if (Localization.getInstance().getLocale().equals(Locale.ITALIAN)) {
            this.prompt += "\n Schreibe die Geschichte in einem freundlichen Italienisch.";
        } else if (Localization.getInstance().getLocale().getLanguage().equals("es")) {
            this.prompt += "\n Schreibe die Geschichte in einem freundlichen Spanisch.";
        } else if (Localization.getInstance().getLocale().getLanguage().equals("tr")) {
            this.prompt += "\n Schreibe die Geschichte in einem freundlichen Tuerkisch.";
        }
    }

    private void buildFileNames() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_");
        String formattedDate = currentDateTime.format(myFormatObj);

        this.fileNameMP3 = formattedDate + gameObjectives.getMainCharacter().getDisplayName() + "_" + gameObjectives.getSupportingCharacter().getDisplayName() + "_" + gameObjectives.getUniverse().getDisplayName() + ".mp3";
        this.fileNameTxt = formattedDate + gameObjectives.getMainCharacter().getDisplayName() + "_" + gameObjectives.getSupportingCharacter().getDisplayName() + "_" + gameObjectives.getUniverse().getDisplayName() + ".txt";
    }
}
