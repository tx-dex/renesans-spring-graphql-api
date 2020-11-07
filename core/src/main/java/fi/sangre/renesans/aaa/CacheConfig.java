package fi.sangre.renesans.aaa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    public static final String AUTH_CUSTOMER_IDS_CACHE = "auth.customerIds";
    public static final String AUTH_RESPONDENT_GROUP_IDS_CACHE = "auth.respondentGroupIds";

    @Value("${spring.cache.caffeine.spec}")
    private String caffeineSpec;

    @Bean
    public CacheManager authorizationCacheManager() {
        final CaffeineCacheManager cacheManager = new CaffeineCacheManager(AUTH_CUSTOMER_IDS_CACHE, AUTH_RESPONDENT_GROUP_IDS_CACHE);
        cacheManager.setAllowNullValues(false);
        cacheManager.setCacheSpecification(caffeineSpec);
        return cacheManager;
    }

}
