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

    public SayadClient(RestTemplate restTemplate, @Value("${sayad.api.base-url}") String sayadBaseUrl) {
        this.restTemplate = restTemplate;
        this.sayadBaseUrl = sayadBaseUrl;
    }

    public ResponseEntity<Void> registerCheque() {
        String url = sayadBaseUrl + "/register";
        return restTemplate.postForEntity(url, null, Void.class);
    }

    public ResponseEntity<Void> presentCheque() {
        String url = sayadBaseUrl + "/present";
        return restTemplate.postForEntity(url, null, Void.class);
    }
}