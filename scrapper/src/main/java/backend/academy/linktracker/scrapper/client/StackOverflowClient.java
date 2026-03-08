package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.client.dto.StackOverflowResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = "application/json")
public interface StackOverflowClient {

    @GetExchange("/questions/{id}?site=stackoverflow&key={key}&access_token={accessToken}")
    StackOverflowResponse fetchQuestion(@PathVariable("id") Long id);
}
