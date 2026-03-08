package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.client.GithubClient;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class GithubClientConfiguration {

    @Bean
    public GithubClient githubClient(RestClient.Builder builder, GithubProperties properties) {
        RestClient restClient = builder.baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getToken())
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(GithubClient.class);
    }
}
