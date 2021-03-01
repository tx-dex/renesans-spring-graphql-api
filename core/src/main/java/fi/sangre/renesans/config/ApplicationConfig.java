package fi.sangre.renesans.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import feign.Logger;
import fi.sangre.renesans.application.model.TranslationMap;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.config.properties.StatisticsProperties;
import fi.sangre.renesans.service.TranslationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j

@Configuration
@EnableAsync
@EnableFeignClients(basePackages = "fi.sangre.renesans.application.client")
@EnableConfigurationProperties({StatisticsProperties.class})
public class ApplicationConfig {
    public static final String ASYNC_EXECUTOR_NAME = "async-executor";
    public static final String DAO_EXECUTOR_NAME = "dao-executor";

    @Bean
    Logger.Level feignLoggerLevel() {
        try {
            return Logger.Level.FULL;
        } catch (final Exception ex) {
            log.warn("Invalid feign logging level: {}. Should be one of: NONE, BASIC, HEADERS, FULL", "FULL");
            return Logger.Level.NONE;
        }
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        final CommonsRequestLoggingFilter filter
                = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST: ");
        return filter;
    }

    @Bean(ASYNC_EXECUTOR_NAME)
    public ThreadPoolTaskExecutor asyncExecutor() {
        //TODO: configure from properties file
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("async-executor-");
        executor.setCorePoolSize(12);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(2000);

        return executor;
    }

    @Bean(DAO_EXECUTOR_NAME)
    public ThreadPoolTaskExecutor daoExecutor() {
        //TODO: configure from properties file
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("dao-executor-");
        executor.setCorePoolSize(12);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(2000);

        return executor;
    }

    @Bean
    public TranslationService translationService(@NonNull final ResourcePatternResolver resolver,
                                                 @NonNull final ObjectMapper objectMapper,
                                                 @NonNull final MultilingualUtils multilingualUtils) throws IOException {
        final List<Resource> translations = ImmutableList.copyOf(resolver.getResources("classpath:translations/texts/*.json"));

        final ImmutableMap.Builder<String, TranslationMap> builder = ImmutableMap.builder();
        for (final Resource translation : translations) {
            final String languageTag = Objects.requireNonNull(FilenameUtils.removeExtension(translation.getFilename()));
            final TranslationMap map = objectMapper.readValue(translation.getInputStream(), TranslationMap.class);
            builder.put(languageTag, map);
        }

        return new TranslationService(builder.build(), multilingualUtils);
    }
}
