package fi.sangre.renesans.config;

import com.google.common.collect.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;

@Configuration
public class ReportGenerationConfig {

    @Value("${fi.sangre.report.generator.zip.thread_pool_size}")
    int threadPoolSize;

    @Bean
    ExecutorService zipExecutorService() {
        checkArgument(Range.closed(1, 50).contains(threadPoolSize), "Thread pool size must be between 1 and 50");

        return Executors.newFixedThreadPool(threadPoolSize);
    }
}
