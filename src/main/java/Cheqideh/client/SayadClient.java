package Cheqideh.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SayadClient {

    private final RestTemplate restTemplate;
    private final String sayadBaseUrl;

    public SayadClient(@Value("${sayad.api.base-url}") String sayadBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.sayadBaseUrl = sayadBaseUrl;
    }

    public void registerCheque() {
        String url = sayadBaseUrl + "/register";
            ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);
    }

    public void presentCheque() {
        String url = sayadBaseUrl + "/present";
            restTemplate.postForEntity(url, null, Void.class);
    }
}