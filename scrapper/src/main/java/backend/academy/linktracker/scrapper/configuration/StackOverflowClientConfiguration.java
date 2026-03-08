package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class StackOverflowClientConfiguration {

    @Bean
    public StackOverflowClient stackOverflowClient(RestClient.Builder builder, StackoverflowProperties properties) {
        RestClient restClient = builder.baseUrl(properties.getBaseUrl())
                .defaultUriVariables(Map.of(
                        "key", properties.getKey(),
                        "accessToken", properties.getAccessToken()))
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(StackOverflowClient.class);
    }
}
