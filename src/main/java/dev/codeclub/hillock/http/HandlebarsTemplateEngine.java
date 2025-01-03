package dev.codeclub.hillock.http;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

public class HandlebarsTemplateEngine {

    private final Handlebars handlebars;

    /**
     * Konstruktor inicjalizujący Handlebars z szablonami w katalogu "templates".
     *
     * @param resourceRoot Katalog z szablonami.
     */
    public HandlebarsTemplateEngine(String resourceRoot) {
        TemplateLoader templateLoader = new ClassPathTemplateLoader();
        templateLoader.setPrefix(resourceRoot);
        templateLoader.setSuffix(".hbm");      // Sufiks plików szablonów
        this.handlebars = new Handlebars(templateLoader);
    }

    /**
     * Renderuje szablon Handlebars z podanymi danymi.
     *
     * @param viewName Nazwa szablonu (bez sufiksu).
     * @param model    Dane do wstrzyknięcia w szablon.
     * @return Wyrenderowany szablon jako String.
     */
    public String render(String viewName, Map<String, Object> model) {
        try {
            Template template = handlebars.compile(viewName);
            return template.apply(model);
        } catch (IOException e) {
            throw new RuntimeException("Nie udało się wyrenderować szablonu: " + viewName, e);
        }
    }
}