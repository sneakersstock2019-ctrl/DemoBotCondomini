package it.sd.lucrezia.ai.service.twilio;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TwilioService {

    @Value("${TWILIO_ACCOUNT_SID}")
    private String accountSid;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String authToken;

    public File downloadRecording(String recordingUrl) throws Exception {

        String mp3Url = recordingUrl + ".mp3";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        String auth = accountSid + ":" + authToken;

        byte[] encodedAuth = Base64.getEncoder()
                .encode(auth.getBytes(StandardCharsets.UTF_8));

        headers.set("Authorization",
                "Basic " + new String(encodedAuth));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response =
                restTemplate.exchange(
                        mp3Url,
                        HttpMethod.GET,
                        request,
                        byte[].class);

        File file = File.createTempFile("twilio-", ".mp3");

        Files.write(file.toPath(), response.getBody());

        return file;
    }
}