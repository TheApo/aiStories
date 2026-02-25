package com.apogames.aistories.game.main;

import com.apogames.aistories.game.objects.GameObjectives;
import com.apogames.common.Localization;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
public class Prompt {

    public static final String DIRECTORY = "data/";

    private final String systemSetting;

    private String prompt;

    private String fileNameMP3;

    private String fileNameTxt;


    private final GameObjectives gameObjectives = new GameObjectives();

    public Prompt() {
        this.setUpShufflePrompt();
        this.systemSetting = "Du bist ein talentierter Kinderbuchautor, der spannende, fantasievolle und lustige Gute-Nacht-Geschichten schreibt. Dein Stil erinnert an Astrid Lindgren und Erich Kästner, mit lebendigen Figuren, liebevollen Details und einem Hauch von Abenteuer. Deine Geschichten sollen Kinder zwischen 7 und 10 Jahren begeistern, indem sie kreative Welten erkunden, mit humorvollen und mutigen Charakteren mitfiebern und gleichzeitig eine kindgerechte Sprache verwenden.";
    }

    public void setUpShufflePrompt() {
        this.gameObjectives.shuffle();
        this.setUpPrompt();
    }

    public void setUpPrompt() {
        this.prompt = "Schreibe eine spannende, lustige und fantasievolle Gute-Nacht-Geschichte für Kinder im Alter von 7 bis 10 Jahren. \n" +
                "Die Geschichte soll gut strukturiert, in 5 Kapiteln unterteilt und etwa 5000 Zeichen lang sein. \n" +
                "Sie soll eine Mischung aus Humor, Abenteuer und Emotionen enthalten, sodass Kinder sich mit den Charakteren identifizieren können. \n\n" +

                "Die Geschichte beinhaltet folgende Elemente:\n" +
                "Hauptcharakter: " + gameObjectives.getMainCharacter().getDisplayName() + ", \n" +
                "Nebencharakter: " + gameObjectives.getSupportingCharacter().getDisplayName() + ", \n" +
                "Spielort: " + gameObjectives.getPlaces().getDisplayName() + ", \n" +
                "Universum: " + gameObjectives.getUniverse().getDisplayName() + ", \n" +
                "Ein zentrales magisches oder wichtiges Objekt: " + gameObjectives.getObjectives().getDisplayName() + ".\n\n" +

                "Die Geschichte kann (muss aber nicht komplett) folgende Fragen beantworten:\n" +
                "- Was macht den Hauptcharakter besonders? " + gameObjectives.getMainCharacter().getDisplayDetails() + "\n" +
                "- Welche lustigen oder außergewöhnlichen Eigenschaften hat der Nebencharakter? " + gameObjectives.getSupportingCharacter().getDisplayDetails() + "\n" +
                "- Welche geheimnisvollen oder magischen Elemente gibt es im Universum? " + gameObjectives.getUniverse().getDisplayDetails() + "\n" +
                "- Welche Herausforderungen oder Überraschungen gibt es am Ort der Handlung? " + gameObjectives.getPlaces().getDisplayDetails() + "\n" +
                "- Welche besondere Bedeutung hat das Objekt und wie beeinflusst es die Handlung? " + gameObjectives.getObjectives().getDisplayDetails() + "\n\n" +

                "Die Erzählweise sollte bildhaft, kindgerecht (ohne lange Schachtelsätze) und spannend sein, mit abwechslungsreichen Sätzen, direkter Rede und humorvollen Elementen. \n" +
                "Achte darauf, dass die Geschichte eine runde Handlung mit einer spannenden Einleitung, einer abenteuerlichen Mitte und einem schönen Abschluss hat, der sich gut als Gute-Nacht-Geschichte eignet.";

        if (Localization.getInstance().getLocale().equals(Locale.ENGLISH)) {
            this.prompt += "\n Schreibe die Gesichte in einem freundlichen Englisch.";
        } else if (Localization.getInstance().getLocale().equals(Locale.FRENCH)) {
            this.prompt += "\n Schreibe die Gesichte in einem freundlichen Französisch.";
        } else if (Localization.getInstance().getLocale().equals(Locale.ITALIAN)) {
            this.prompt += "\n Schreibe die Gesichte in einem freundlichen Italienisch.";
        } else if (Localization.getInstance().getLocale().getLanguage().equals("es")) {
            this.prompt += "\n Schreibe die Gesichte in einem freundlichen spanisch.";
        } else if (Localization.getInstance().getLocale().getLanguage().equals("tr")) {
            this.prompt += "\n Schreibe die Gesichte in einem freundlichen türkisch.";
        }

        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_");
        String formattedDate = currentDateTime.format(myFormatObj);

        this.fileNameMP3 = formattedDate + gameObjectives.getMainCharacter().getDisplayName() + "_" + gameObjectives.getSupportingCharacter().getDisplayName() + "_" + gameObjectives.getUniverse().getDisplayName()+".mp3";
        this.fileNameTxt = formattedDate + gameObjectives.getMainCharacter().getDisplayName() + "_" + gameObjectives.getSupportingCharacter().getDisplayName() + "_" + gameObjectives.getUniverse().getDisplayName()+".txt";
    }
}
