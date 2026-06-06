package it.sd.demo.bot.condomini.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.sd.demo.bot.condomini.bean.AIResponse;
import it.sd.demo.bot.condomini.bean.ChatMessage;
import it.sd.demo.bot.condomini.bean.UserSession;
import it.sd.demo.bot.condomini.bean.Utente;
import it.sd.demo.bot.condomini.dao.TicketDao;
import it.sd.demo.bot.condomini.dao.UtenteDao;
import it.sd.demo.bot.condomini.util.PhoneUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WhatsAppService {

    @Value("${whatsapp.token}")
    private String token;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    private static final String STEP_SCELTA_TICKET = "SCELTA_TICKET";
    private static final String STEP_NUOVA_SEGNALAZIONE = "NUOVA_SEGNALAZIONE";

    private final OpenAIService openAIService;
    private final UtenteDao utenteDao;
    private final TicketDao ticketDao;
    private final PhoneUtils phoneUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private final String urlApiMetaMessages = "https://graph.facebook.com/v25.0/{}/messages";

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public void elaboraMessaggio(String body) {
        try {
            JsonNode jsonRoot = objectMapper.readTree(body);

            JsonNode messageNode = jsonRoot.path("entry")
                    .get(0)
                    .path("changes")
                    .get(0)
                    .path("value");

            if (!messageNode.has("messages")) {
                System.out.println("Nessun messaggio da leggere");
                return;
            }

            JsonNode message = messageNode.path("messages").get(0);

            String from = phoneUtils.normalizePhone(message.path("from").asText());
            String type = message.path("type").asText();

            if (!"text".equals(type)) {
                invioMessaggio(from, "Al momento posso gestire solo messaggi di testo. Puoi descrivermi il problema con un messaggio?");
                return;
            }

            String testoMessaggio = message.path("text").path("body").asText();

            System.out.println("Processo Messaggio da " + from + ": " + testoMessaggio);

            processaMessaggio(from, testoMessaggio);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processaMessaggio(String from, String testoMessaggio) {

        Utente utente = utenteDao.findCondominoByTelefono(from);

        if (utente == null) {
            System.err.println("Numero " + from + " non autorizzato.");
            invioMessaggio(from, "Numero non autorizzato.");
            return;
        }

        String nomeUtente = utente.getNome();

        UserSession userSession = sessions.getOrDefault(from, new UserSession());
        sessions.putIfAbsent(from, userSession);

        userSession.nome = nomeUtente;

        boolean haTicketAperti = ticketDao.hasTicketApertiByUtente(utente.getId());
        userSession.haTicketAperti = haTicketAperti;

        if (userSession.step == null && haTicketAperti) {
            userSession.step = STEP_SCELTA_TICKET;

            invioMessaggio(from,
                    "Ciao " + nomeUtente + ", sono Lucrezia, l'assistente virtuale del condominio 😊\n\n" +
                            "Vedo che hai già una o più segnalazioni aperte.\n\n" +
                            "Vuoi:\n" +
                            "1️⃣ conoscere lo stato dei ticket aperti\n" +
                            "2️⃣ aprire una nuova segnalazione?"
            );
            return;
        }

        if (STEP_SCELTA_TICKET.equals(userSession.step)) {
            gestisciSceltaTicket(from, testoMessaggio, nomeUtente, userSession);
            return;
        }

        if (STEP_NUOVA_SEGNALAZIONE.equals(userSession.step)) {
            userSession.step = null;
        }

        AIResponse aiResponse = openAIService.askLucrezia(testoMessaggio, userSession);

        String rispostaPerUtente = aiResponse.getReply();

        if (rispostaPerUtente == null || rispostaPerUtente.isBlank()) {
            rispostaPerUtente = "Mi dispiace, al momento non riesco a elaborare la richiesta.";
        }

        salvaConversazione(userSession, testoMessaggio, rispostaPerUtente);

        if (aiResponse.isOpen_ticket()) {

            Long idTicket = ticketDao.insertTicket(
                    utente.getIdCondominio(),
                    utente.getId(),
                    aiResponse.getCategory(),
                    aiResponse.getPriority(),
                    "WHATSAPP",
                    testoMessaggio
            );

            if (idTicket == null) {
                invioMessaggio(from,
                        "Mi dispiace, ho capito la segnalazione ma non sono riuscita ad aprire il ticket. Riprova tra poco."
                );
                return;
            }

            rispostaPerUtente += """

                    
                    Ticket aperto correttamente ✅

                    Numero ticket: #%d

                    Monitora qui:
                    https://demo-condomini.it/ticket/%d
                    """.formatted(idTicket, idTicket);

            resetSessioneDopoTicket(userSession);

            invioMessaggio(from, rispostaPerUtente);
            return;
        }

        userSession.tentativiComprensione++;

        if (userSession.tentativiComprensione >= 10) {

            Long idTicket = ticketDao.insertTicket(
                    utente.getIdCondominio(),
                    utente.getId(),
                    "generico",
                    "media",
                    "WHATSAPP",
                    testoMessaggio
            );

            if (idTicket == null) {
                invioMessaggio(from,
                        "Mi dispiace, non sono riuscita ad aprire la segnalazione generica. Riprova tra poco."
                );
                return;
            }

            rispostaPerUtente =
                    "Grazie per le informazioni 😊\n\n" +
                            "Per non farti perdere altro tempo, ho aperto una segnalazione generica riportando la descrizione che mi hai fornito.\n\n" +
                            "Ticket aperto correttamente ✅\n" +
                            "Numero ticket: #" + idTicket + "\n\n" +
                            "Puoi monitorarlo qui:\n" +
                            "https://demo-condomini.it/ticket/" + idTicket;

            resetSessioneDopoTicket(userSession);
        }

        invioMessaggio(from, rispostaPerUtente);
    }

    private void gestisciSceltaTicket(String from,
                                      String testoMessaggio,
                                      String nomeUtente,
                                      UserSession userSession) {

        String msg = testoMessaggio.toLowerCase();

        if (msg.contains("1") || msg.contains("stato") || msg.contains("ticket")) {
            invioMessaggio(from,
                    "Certo 😊\n" +
                            "Puoi monitorare lo stato delle tue segnalazioni da qui:\n\n" +
                            "https://demo-condomini.it/ticket?telefono=" + from
            );

            userSession.step = null;
            return;
        }

        if (msg.contains("2") || msg.contains("nuova") || msg.contains("segnalazione")) {
            userSession.step = STEP_NUOVA_SEGNALAZIONE;
            userSession.tentativiComprensione = 0;
            userSession.cronologiaMessaggi.clear();
            userSession.primoMessaggio = false;

            invioMessaggio(from,
                    "Va bene " + nomeUtente + " 😊\n" +
                            "Descrivimi pure il nuovo problema e ti aiuterò ad aprire la segnalazione."
            );
            return;
        }

        invioMessaggio(from,
                "Puoi rispondermi con:\n" +
                        "1 per conoscere lo stato dei ticket aperti\n" +
                        "2 per aprire una nuova segnalazione"
        );
    }

    private void salvaConversazione(UserSession userSession,
                                    String testoMessaggio,
                                    String rispostaPerUtente) {

        userSession.cronologiaMessaggi.add(new ChatMessage("user", testoMessaggio));
        userSession.cronologiaMessaggi.add(new ChatMessage("assistant", rispostaPerUtente));

        userSession.primoMessaggio = false;

        if (userSession.cronologiaMessaggi.size() > 20) {
            userSession.cronologiaMessaggi =
                    userSession.cronologiaMessaggi.subList(
                            userSession.cronologiaMessaggi.size() - 20,
                            userSession.cronologiaMessaggi.size()
                    );
        }
    }

    private void resetSessioneDopoTicket(UserSession userSession) {
        userSession.haTicketAperti = true;
        userSession.step = null;
        userSession.tentativiComprensione = 0;
        userSession.cronologiaMessaggi.clear();
        userSession.primoMessaggio = false;
    }

    private void invioMessaggio(String to, String testoMessaggio) {
        try {
            Map<String, Object> text = Map.of("body", testoMessaggio);

            Map<String, Object> payload = Map.of(
                    "messaging_product", "whatsapp",
                    "to", to,
                    "type", "text",
                    "text", text
            );

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setBearerAuth(token);

            HttpEntity<Map<String, Object>> httpEntity =
                    new HttpEntity<>(payload, httpHeaders);

            String url = urlApiMetaMessages.replace("{}", phoneNumberId);

            System.out.println("Invoco Api Meta Messages (POST): " + url);
            System.out.println("Payload: " + payload);

            restTemplate.postForEntity(url, httpEntity, String.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}