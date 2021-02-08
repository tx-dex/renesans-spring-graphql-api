package fi.sangre.renesans.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j

@Service
public class TemplateService {
    private final DefaultMustacheFactory mustacheFactory;

    @NonNull
    public String templateBody(@NonNull final String bodyTemplate, @NonNull final Map<String, Object> parameters) {
        final Mustache body = mustacheFactory.compile(new StringReader(bodyTemplate), "body-template");

        final StringWriter writer = new StringWriter();
        body.execute(writer, parameters);

        return writer.toString();
    }
}
