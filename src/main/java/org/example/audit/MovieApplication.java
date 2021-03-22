package org.example.audit;

import ch.qos.logback.access.tomcat.LogbackValve;
import io.micrometer.core.instrument.MeterRegistry;
import org.example.audit.model.Movie;
import org.example.audit.model.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@SpringBootApplication
public class MovieApplication implements CommandLineRunner {

	@Autowired
	MovieRepository movieRepository;

	@Bean
	public DynamoDbClient dynamoDbClient() {
		Region region = Region.US_WEST_2;
		DynamoDbClient ddb = DynamoDbClient.builder()
				.region(region)
				.build();

		return ddb;
	}

	@Bean
	public TomcatServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		LogbackValve logbackValve = new LogbackValve();
		logbackValve.setFilename("logback-access.xml");
		tomcat.addContextValves(logbackValve);
		return tomcat;
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user =
				User.withDefaultPasswordEncoder()
						.username("gabbar")
						.password("password")
						.roles("USER")
						.build();

		return new InMemoryUserDetailsManager(user);
	}

	@Bean
	MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
		return registry -> registry.config().commonTags("app-id", "my-audit-logger");
	}

    public static void main(String[] args) {
        SpringApplication.run(MovieApplication.class, args);
    }

	@Override
	public void run(String... args) throws Exception {

		addMovie("Sholay");
		addMovie("Batman");
		addMovie("Star Wars");
		addMovie("Spider Man");

	}

	private void addMovie(String name) {
		Movie movie1 = new Movie();
		movie1.setName(name);
		movieRepository.save(movie1);
	}
}
