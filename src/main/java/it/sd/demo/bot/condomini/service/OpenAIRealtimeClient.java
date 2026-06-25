package it.sd.demo.bot.condomini.service;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OpenAIRealtimeClient {

    @Value("${openai.api-key}")
    private String openAiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketClient createVoiceClient(String callSid,
                                             OpenAIRealtimeAudioListener listener) throws Exception {

        URI uri = new URI("wss://api.openai.com/v1/realtime?model=gpt-realtime");

        WebSocketClient client = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("OPENAI REALTIME VOICE CONNECTED - CALL SID = " + callSid);

                try {
                    sendSessionUpdate(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(String message) {
                try {
                    JsonNode root = objectMapper.readTree(message);
                    String type = root.path("type").asText();

                    if ("error".equals(type)) {
                        System.out.println("OPENAI REALTIME ERROR EVENT RAW = " + message);
                        listener.onError(message);
                        return;
                    }

                    if ("session.updated".equals(type)) {
                        System.out.println("OPENAI REALTIME VOICE SESSION UPDATED");
                        return;
                    }

                    if ("input_audio_buffer.speech_started".equals(type)) {
                        System.out.println("UTENTE HA INIZIATO A PARLARE");
                        return;
                    }

                    if ("input_audio_buffer.speech_stopped".equals(type)) {
                        System.out.println("UTENTE HA FINITO DI PARLARE");
                        return;
                    }

                    if ("response.output_audio.delta".equals(type)) {
                        String audioDelta = root.path("delta").asText();

                        if (audioDelta != null && !audioDelta.isBlank()) {
                            listener.onAudioDelta(audioDelta);
                        }

                        return;
                    }

                    if ("response.output_audio_transcript.delta".equals(type)) {
                        String delta = root.path("delta").asText();

                        if (delta != null && !delta.isBlank()) {
                            listener.onAssistantTranscriptDelta(delta);
                        }

                        return;
                    }

                    if ("response.output_audio_transcript.done".equals(type)) {
                        String transcript = root.path("transcript").asText();

                        if (transcript != null && !transcript.isBlank()) {
                            listener.onAssistantTranscriptDone(transcript);
                        }

                        return;
                    }

                    if ("response.done".equals(type)) {
                        System.out.println("OPENAI RESPONSE DONE");
                        return;
                    }

                    System.out.println("OPENAI REALTIME TYPE = " + type);

                } catch (Exception e) {
                    System.out.println("OPENAI REALTIME RAW MESSAGE = " + message);
                }
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                System.out.println("OPENAI REALTIME BINARY MESSAGE ricevuto");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("OPENAI REALTIME CLOSED - code=" + code + ", reason=" + reason);
            }

            @Override
            public void onError(Exception ex) {
                System.out.println("OPENAI REALTIME ERROR: " + ex.getMessage());
                ex.printStackTrace();
            }
        };

        client.addHeader("Authorization", "Bearer " + openAiApiKey);

        return client;
    }

    private void sendSessionUpdate(WebSocketClient client) throws Exception {

        Map<String, Object> event = Map.of(
                "type", "session.update",
                "session", Map.of(
                        "type", "realtime",
                        "model", "gpt-realtime",
                        "output_modalities", new String[]{"audio"},
                        "instructions", """
                            Sei Lucrezia, assistente vocale del condominio.

                            Parli al telefono con un condomino.
                            Rispondi sempre in italiano.
                            Usa tono gentile, naturale e professionale.
                            Frasi brevi, adatte a una chiamata telefonica.
                            Non dire mai che sei un'intelligenza artificiale.
                            Se non capisci, chiedi di ripetere.
                            Per ora siamo in modalità test: rispondi solo per verificare che la voce realtime funzioni.
                            """,
                        "audio", Map.of(
                                "input", Map.of(
                                        "format", Map.of(
                                                "type", "audio/pcmu"
                                        ),
                                        "turn_detection", Map.of(
                                                "type", "server_vad",
                                                "threshold", 0.5,
                                                "prefix_padding_ms", 300,
                                                "silence_duration_ms", 500
                                        )
                                ),
                                "output", Map.of(
                                        "format", Map.of(
                                                "type", "audio/pcmu"
                                        ),
                                        "voice", "marin"
                                )
                        )
                )
        );

        client.send(objectMapper.writeValueAsString(event));
    }

    public void sendAudio(WebSocketClient client, String twilioBase64Payload) {

        if (client == null || !client.isOpen()) {
            return;
        }

        try {
            Map<String, Object> event = Map.of(
                    "type", "input_audio_buffer.append",
                    "audio", twilioBase64Payload
            );

            client.send(objectMapper.writeValueAsString(event));

        } catch (org.java_websocket.exceptions.WebsocketNotConnectedException e) {
            System.out.println("OPENAI REALTIME non connesso, audio ignorato.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}