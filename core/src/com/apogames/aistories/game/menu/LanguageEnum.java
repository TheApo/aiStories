package com.apogames.aistories.game.menu;

import java.util.Locale;

public enum LanguageEnum {
    DE(Locale.GERMAN, "button_language_de"),
    EN(Locale.ENGLISH, "button_language_en"),
    ES(new Locale("es", "ES"), "button_language_es"),
    FR(Locale.FRENCH, "button_language_fr"),
    IT(Locale.ITALIAN, "button_language_it"),
    TR(new Locale("tr", "TR"), "button_language_tr");

    private Locale locale;
    private String language;

    private LanguageEnum(Locale locale, String language) {
        this.locale = locale;
        this.language = language;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return language;
    }

    public LanguageEnum getNext(int add) {
        int index = 0;
        for (LanguageEnum language : values()) {
            if (this.equals(language)) {
                if (index + add >= values().length) {
                    return values()[index + add - values().length];
                } else if (index + add < 0) {
                    return values()[index + add + values().length];
                } else {
                    return values()[index + add];
                }
            }
            index += 1;
        }
        return this;
    }
}
