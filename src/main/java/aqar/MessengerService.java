package aqar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
class MessengerService {

    private static final String MESSAGE = "{\"recipient\": {\"phone_number\": \"%s\"},\"message\": {\"text\": \"%s\"}}";

    @Value("${aqarme.messenger.url}")
    private String url;

    private RestTemplate restTemplate;

    public MessengerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    void send(String recipient, String str) {
        try{
            String message = String.format(MESSAGE, recipient, str);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

            HttpEntity<String> entity = new HttpEntity<>(message, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK){
                log.error(response.toString());
            }else{
                log.info("Ad: {}, response: {}", str, response.getStatusCode());
            }
        }catch (Exception ex){
            log.error("exception happens when sending Ad, exception: {}, Ad: {}", ex.getMessage(), str);
        }
    }
}
