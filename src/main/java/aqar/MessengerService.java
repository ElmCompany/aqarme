package aqar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MessengerService {

    private static final String MESSAGE = "{\"recipient\": {\"phone_number\": \"%s\"},\"message\": {\"text\": \"%s\"}}";

    @Value("${aqarme.messenger.url}")
    private String url;

    private RestTemplate restTemplate;

    public MessengerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void send(String recipient, String str) {

        String message = String.format(MESSAGE, recipient, str);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(message, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK){
            System.out.println(response);
        }else{
            System.err.printf("Ad: %s, response: %s\n", str, response.getStatusCode());
        }
    }
}
