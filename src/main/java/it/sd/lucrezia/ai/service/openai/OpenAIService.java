package it.sd.lucrezia.ai.service.openai;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.sd.lucrezia.ai.bean.OpenAIRequest;
import it.sd.lucrezia.ai.bean.OpenAIRequestMessage;
import it.sd.lucrezia.ai.bean.OpenAIResponse;
import it.sd.lucrezia.ai.bean.WhatsAppAiResponse;

@Service
public class OpenAIService {

    @Value("${openai.api-key}")
    private String apiKey;
    
    private static final String OPENAI_MODEL= "gpt-4.1-mini";
    private static final String OPENAI_API= "https://api.openai.com/v1/chat/completions";
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public WhatsAppAiResponse askLucrezia(List<OpenAIRequestMessage> messaggiOpenAIRequestMessage) {
    	OpenAIRequest openAIRequest = null;
    	HttpHeaders httpHeaders = null;
    	HttpEntity<OpenAIRequest> httpEntity = null;
    	ResponseEntity<OpenAIResponse> response = null;
    	String responseString = null;
    	
        try {
            openAIRequest = new OpenAIRequest();
            openAIRequest.setModel(OPENAI_MODEL);
            openAIRequest.setMessages(messaggiOpenAIRequestMessage);
            openAIRequest.setTemperature(0.8);

            httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setBearerAuth(apiKey);

            httpEntity = new HttpEntity<>(openAIRequest, httpHeaders);

            System.out.println("Invoco Api OpenAI Messages (POST): " + OPENAI_API);
            response = restTemplate.postForEntity(
            				OPENAI_API,
                            httpEntity,
                            OpenAIResponse.class
                    );

            responseString = response.getBody()
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            System.out.println("Response Api OpenAI:");
            System.out.println(responseString);

            return objectMapper.readValue(responseString, WhatsAppAiResponse.class);

        } catch (Exception e) {
            e.printStackTrace();

            WhatsAppAiResponse error = new WhatsAppAiResponse();
            error.setReply(
                    "Mi dispiace, al momento non riesco a elaborare la richiesta."
            );

            return error;
        }
    }
    
    
    
    
}