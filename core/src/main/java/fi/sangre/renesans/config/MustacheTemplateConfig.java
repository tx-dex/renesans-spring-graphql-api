package fi.sangre.renesans.config;

import com.github.mustachejava.DefaultMustacheFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MustacheTemplateConfig {
    @Bean
    @Scope("prototype")
    public DefaultMustacheFactory mustacheFactory() {
        return new DefaultMustacheFactory();
    }
}
