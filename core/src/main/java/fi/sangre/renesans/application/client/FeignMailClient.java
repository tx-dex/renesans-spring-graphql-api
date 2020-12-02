package fi.sangre.renesans.application.client;

import com.sangre.mail.MailClient;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "mail-service-client", url = "${fi.sangre.renesans.mail-service.url}")
public interface FeignMailClient extends MailClient {
}
