package fi.sangre.renesans.application.client;

import fi.sangre.media.rest.client.MediaServerRestClient;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "maedia-service-client", url = "${fi.sangre.renesans.media-service.url}")
public interface FeignMediaClient extends MediaServerRestClient {
}
