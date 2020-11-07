package fi.sangre.renesans.service;

import com.netflix.loadbalancer.Server;
import fi.sangre.renesans.config.AthenaPdfConfig;
import fi.sangre.renesans.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j

@Service
@RibbonClient(name = AthenaPdfConfig.SERVICE_NAME, configuration = AthenaPdfConfig.class)
public class AthenaPdfService {
    //TODO: should be less imo, at least connect timeout
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 30000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 30000;

    private final ExecutorService executorService;
    private final OkHttpClient client;
    private final LoadBalancerClient loadBalancer;

    @Value("${fi.sangre.pdf.shared.secret}")
    private String converterSecret;

    @Value("${fi.sangre.pdf.retry_count}")
    private int retryCount;

    @Autowired
    public AthenaPdfService(final ExecutorService athenaPdfExecutorService, final LoadBalancerClient loadBalancer) {
        this.executorService = athenaPdfExecutorService;
        this.loadBalancer = loadBalancer;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build();
    }

    public InputStream execute(final URL reportUrl) {
        final URL url = buildUrl(reportUrl);
        final Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            final okhttp3.Response response = call(request);

            return Objects.requireNonNull(response.body()).byteStream();
        } catch (IOException e) {
            log.warn("Cannot execute athena pdf call", e);
            throw new InternalServerErrorException();
        }
    }

    public CompletableFuture<InputStream> executeAsync(final URL reportUrl, final Function<InputStream, InputStream> callback) {
        final CompletableFuture<InputStream> task = CompletableFuture.supplyAsync(() -> execute(reportUrl), executorService);

        return task.thenApply(callback);
    }

    private Response call(final Request request) throws IOException {
        int retry = 0;
        Response response = null;
        log.debug("Calling athena pdf for the first time");
        while (retry < retryCount) {
            response = client.newCall(request).execute();
            retry++;

            if (response.code() == HttpStatus.OK.value()) {
                log.debug("Successfully executed athena pdf call.");
                break;
            } else if (retry < retryCount) {
                log.warn("Athena pdf call failed. Retrying {} out of {} in 1 second...", retry, retryCount);
                closeResponse(response);
                sleep();
            } else {
                log.warn("Athena pdf error, code:{}, message:{}", response.code(), response.message());
                closeResponse(response);
                throw new InternalServerErrorException();
            }
        }
        return response;
    }

    private URL buildUrl(final URL reportUrl) {
        final HttpUrl  serviceUrl;
        final ServiceInstance serviceInstance = loadBalancer.choose(AthenaPdfConfig.SERVICE_NAME);

        if (serviceInstance instanceof RibbonLoadBalancerClient.RibbonServer) {
            final Server serverInstance = ((RibbonLoadBalancerClient.RibbonServer) serviceInstance).getServer();

            serviceUrl = new HttpUrl.Builder()
                    .scheme(serverInstance.getScheme())
                    .host(serverInstance.getHost())
                    .port(serverInstance.getPort())
                    .build();
        } else {
            serviceUrl = Objects.requireNonNull(HttpUrl.get(serviceInstance.getUri()));
        }

        log.debug("Building request for service instance: {}", serviceUrl);

        return serviceUrl.newBuilder()
                .addPathSegment("convert")
                .addQueryParameter("auth", converterSecret)
                .addQueryParameter("url", reportUrl.toString())
                .build()
                .url();
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (final Exception ignore) {
        }
    }

    private void closeResponse(final Response response) {
        if (response.body() != null) {
            response.close();
        }
    }
}
