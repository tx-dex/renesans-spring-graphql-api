package fi.sangre.renesans.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
@EnableAsync
public class ApplicationConfig {
    public static final String ASYNC_EXECUTOR_NAME = "async-executor";
    public static final String DAO_EXECUTOR_NAME = "dao-executor";

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
}
