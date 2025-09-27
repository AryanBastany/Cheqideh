package cheqideh;

import cheqideh.client.SayadClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;


@RestClientTest(SayadClient.class)
public class SayadClientTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplate restTemplate(RestTemplateBuilder builder) {
            return builder.build();
        }
    }

    @Autowired
    private SayadClient sayadClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Value("${sayad.api.base-url}")
    private String sayadBaseUrl;

    @BeforeEach
    public void setUp() {
        mockServer.reset();
    }

    @Test
    @DisplayName("Test registerCheque() sends correct POST request")
    public void registerCheque_SendsPostRequestToRegisterUrl() {
        String expectedUrl = sayadBaseUrl + "/register";
        mockServer.expect(ExpectedCount.once(), requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK));

        sayadClient.registerCheque();

        mockServer.verify();
    }

    @Test
    @DisplayName("Test presentCheque() sends correct POST request")
    public void presentCheque_SendsPostRequestToPresentUrl() {
        String expectedUrl = sayadBaseUrl + "/present";
        mockServer.expect(ExpectedCount.once(), requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK));

        sayadClient.presentCheque();

        mockServer.verify();
    }
}