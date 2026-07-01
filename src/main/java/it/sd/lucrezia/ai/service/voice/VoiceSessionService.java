package it.sd.lucrezia.ai.service.voice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import it.sd.lucrezia.ai.bean.UserSession;
import it.sd.lucrezia.ai.bean.VoiceSessionStep;

@Service
public class VoiceSessionService {

    private final Map<String, UserSession> sessions =
            new ConcurrentHashMap<>();

    public UserSession getOrCreateVoiceSession(String phoneNumber) {

        UserSession session = sessions.get(phoneNumber);

        if (session == null) {

            session = new UserSession();

            session.setVoiceSessionStep(VoiceSessionStep.NEW_TICKET);
            session.primoMessaggio = true;

            sessions.put(phoneNumber, session);
        }

        return session;
    }

    public void removeSession(String phoneNumber) {
        sessions.remove(phoneNumber);
    }
}