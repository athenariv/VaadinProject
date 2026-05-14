package fi.company.companyapp.config;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

@Component
public class AppI18NProvider implements I18NProvider {

    private static final List<Locale> SUPPORTED_LOCALES = List.of(
        new Locale("fi"),
        Locale.ENGLISH
    );

    @Override
    public List<Locale> getProvidedLocales() {
        return SUPPORTED_LOCALES;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("i18n/translations", locale);
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle("i18n/translations", Locale.ENGLISH);
        }

        String pattern;
        try {
            pattern = bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }

        if (params.length == 0) {
            return pattern;
        }
        return new MessageFormat(pattern, locale).format(params);
    }
}
