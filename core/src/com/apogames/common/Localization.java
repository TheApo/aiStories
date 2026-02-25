package com.apogames.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

import static com.badlogic.gdx.utils.I18NBundle.createBundle;

/**
 * Holds instances of the resource bundles and manages the locale.
 */
public final class Localization {
    private static final Localization INSTANCE = new Localization();

    private Locale locale;
    private I18NBundle common;

    private Localization() {
        this.locale = Locale.ENGLISH;
        initialize();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Localization getInstance() {
        return INSTANCE;
    }

    private void initialize() {
        FileHandle internal = Gdx.files.internal("i18n/aistories");
        common = createBundle(internal, locale);
    }

    /**
     * Gets locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets locale.
     *
     * @param locale the locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
        initialize();
    }

    /**
     * Gets common.
     *
     * @return the common
     */
    public I18NBundle getCommon() {
        return common;
    }
}
