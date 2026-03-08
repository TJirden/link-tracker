package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.dto.ApiErrorResponse;
import backend.academy.linktracker.bot.client.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ScrapperClientConfiguration {

    @Value("${scrapper.base-url:http://localhost:8081}")
    private String scrapperBaseUrl;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ScrapperClient scrapperClient(RestClient.Builder builder, ObjectMapper objectMapper) {
        RestClient restClient = builder.baseUrl(scrapperBaseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    ApiErrorResponse apiErrorResponse =
                            objectMapper.readValue(response.getBody(), ApiErrorResponse.class);

                    throw new ApiException(apiErrorResponse);
                })
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(ScrapperClient.class);
    }
}
