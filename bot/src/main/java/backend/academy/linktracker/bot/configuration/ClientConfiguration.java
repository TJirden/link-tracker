package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.client.ScrapperClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfiguration {

    @Value("${scrapper.base-url:http://localhost:8080}")
    private String scrapperBaseUrl;

    @Bean
    public ScrapperClient scrapperClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(scrapperBaseUrl).build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(ScrapperClient.class);
    }
}
