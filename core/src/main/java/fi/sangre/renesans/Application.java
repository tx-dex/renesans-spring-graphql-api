package fi.sangre.renesans;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@PropertySource("file:${RENESANS_ETC:etc}/application.properties")
@EnableCaching
public class Application {

	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
