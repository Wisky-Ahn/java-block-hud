package io.github.wiskyahn.blockhud.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 다국어 메시지 제공. 원본 {@code Localization.lua} + {@code Languages/*.inc} 대체. (DESIGN.md §8.3 language)
 *
 * <p>리소스 번들 {@code i18n/messages_{ko,en}.properties}를 사용한다.
 * Java 9+의 {@link ResourceBundle}은 .properties를 UTF-8로 읽으므로 한글 키도 그대로 동작한다.
 */
public final class LocalizationService {

    private static final String BUNDLE = "i18n.messages";

    private ResourceBundle bundle;
    private Locale locale;

    public LocalizationService(Locale locale) {
        setLocale(locale);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        this.bundle = ResourceBundle.getBundle(BUNDLE, locale);
    }

    public Locale locale() {
        return locale;
    }

    /** 키에 대한 메시지(없으면 키 자체를 반환). */
    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /** {0}, {1} ... 플레이스홀더 치환 (원본 모달의 applyPlaceholders 대응). */
    public String format(String key, Object... args) {
        return new MessageFormat(get(key), locale).format(args);
    }
}
