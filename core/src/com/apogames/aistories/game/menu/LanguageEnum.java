package com.apogames.aistories.game.menu;

import com.apogames.common.Localization;
import com.apogames.entity.DropdownOption;

import java.util.Locale;

public enum LanguageEnum implements DropdownOption {
    DE(Locale.GERMAN, "button_language_de", "language_name_de"),
    EN(Locale.ENGLISH, "button_language_en", "language_name_en"),
    ES(new Locale("es", "ES"), "button_language_es", "language_name_es"),
    FR(Locale.FRENCH, "button_language_fr", "language_name_fr"),
    IT(Locale.ITALIAN, "button_language_it", "language_name_it"),
    TR(new Locale("tr", "TR"), "button_language_tr", "language_name_tr");

    private final Locale locale;
    private final String languageKey;
    private final String nameKey;

    LanguageEnum(Locale locale, String languageKey, String nameKey) {
        this.locale = locale;
        this.languageKey = languageKey;
        this.nameKey = nameKey;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return languageKey;
    }

    @Override
    public String getDropdownLabel() {
        return Localization.getInstance().getCommon().get(nameKey);
    }

    @Override
    public String getSelectedLabel() {
        return languageKey;
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
