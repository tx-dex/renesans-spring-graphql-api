package fi.sangre.renesans.config;

import com.google.common.collect.Range;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.ServerListSubsetFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;

@Configuration
public class AthenaPdfConfig {
    public static final String SERVICE_NAME = "athena-pdf";

    @Value("${fi.sangre.pdf.thread_pool.size}")
    int threadPoolSize;

    @Bean
    public ServerListSubsetFilter serverListFilter() {
        return new ServerListSubsetFilter();
    }

    @Bean
    public IRule ribbonRule() {
        return new RoundRobinRule();
    }

    @Bean
    ExecutorService athenaPdfExecutorService() {
        checkArgument(Range.closed(1, 50).contains(threadPoolSize), "Thread pool size must be between 1 and 50");

        return Executors.newFixedThreadPool(threadPoolSize);
    }
}
