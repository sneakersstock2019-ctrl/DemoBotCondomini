package it.sd.lucrezia.ai.service.voice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import it.sd.lucrezia.ai.bean.VoiceContext;

@Service
public class VoiceCallContextRegistry {

    private final Map<String, VoiceContext> contexts = new ConcurrentHashMap<>();

    public void put(String callSid, VoiceContext context) {
        contexts.put(callSid, context);
    }

    public VoiceContext get(String callSid) {
        return contexts.get(callSid);
    }

    public void remove(String callSid) {
        contexts.remove(callSid);
    }
}