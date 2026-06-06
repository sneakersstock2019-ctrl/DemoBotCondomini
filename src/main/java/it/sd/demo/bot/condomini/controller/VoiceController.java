package it.sd.demo.bot.condomini.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.sd.demo.bot.condomini.bean.AIResponse;
import it.sd.demo.bot.condomini.bean.ChatMessage;
import it.sd.demo.bot.condomini.bean.UserSession;
import it.sd.demo.bot.condomini.bean.Utente;
import it.sd.demo.bot.condomini.dao.TicketDao;
import it.sd.demo.bot.condomini.dao.UtenteDao;
import it.sd.demo.bot.condomini.service.OpenAIService;
import it.sd.demo.bot.condomini.service.VoiceSessionService;
import it.sd.demo.bot.condomini.util.PhoneUtils;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/voice")
@RequiredArgsConstructor
public class VoiceController {

    private static final String TWILIO_VOICE = "Polly.Bianca-Neural";

    private final OpenAIService openAIService;
    private final VoiceSessionService voiceSessionService;
    private final UtenteDao utenteDao;
    private final TicketDao ticketDao;
    private final PhoneUtils phoneUtils;

    @RequestMapping(
            value = "/incoming",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = "application/xml"
    )
    public String incomingCall(@RequestParam(value = "From", required = false) String from) {

        String phone = phoneUtils.normalizePhone(from);

        System.out.println("############################");
        System.out.println("TWILIO INCOMING CALL");
        System.out.println("FROM ORIGINALE = " + from);
        System.out.println("FROM NORMALIZZATO = " + phone);
        System.out.println("############################");

        Utente utente = utenteDao.findCondominoByTelefono(phone);

        if (utente == null) {
            return buildSayResponse(
                    "Buongiorno, sono Lucrezia. Il numero da cui sta chiamando non risulta abilitato al servizio. La invito a contattare l'amministratore."
            );
        }

        UserSession session = voiceSessionService.getOrCreateVoiceSession(phone);
        session.nome = utente.getNome();
        session.step = "VOICE";
        session.primoMessaggio = true;
        session.tentativiComprensione = 0;
        session.cronologiaMessaggi.clear();

        String saluto =
                "Buongiorno " + safe(utente.getNome(), "") +
                ", sono Lucrezia. " +
                "Vedo che sta chiamando per il condominio " + safe(utente.getNomeCondominio(), "") + ". " +
                "Mi descriva pure il problema e la aiuterò ad aprire una segnalazione.";

        return buildGatherResponse(saluto);
    }

    @PostMapping(value = "/gather", produces = "application/xml")
    public String gather(@RequestParam(value = "SpeechResult", required = false) String speechResult,
                         @RequestParam(value = "From", required = false) String from) {

        String phone = phoneUtils.normalizePhone(from);

        System.out.println("############################");
        System.out.println("TWILIO SPEECH RESULT");
        System.out.println("FROM ORIGINALE = " + from);
        System.out.println("FROM NORMALIZZATO = " + phone);
        System.out.println("SpeechResult = " + speechResult);
        System.out.println("############################");

        Utente utente = utenteDao.findCondominoByTelefono(phone);

        if (utente == null) {
            voiceSessionService.removeSession(phone);

            return buildSayResponse(
                    "Mi dispiace, il numero da cui sta chiamando non risulta abilitato al servizio. La invito a contattare l'amministratore."
            );
        }

        UserSession session = voiceSessionService.getOrCreateVoiceSession(phone);
        session.nome = utente.getNome();

        if (speechResult == null || speechResult.isBlank()) {
            session.tentativiComprensione++;

            if (session.tentativiComprensione >= 3) {
                voiceSessionService.removeSession(phone);

                return buildSayResponse(
                        "Mi dispiace, non sono riuscita a capire bene la richiesta. La invito a richiamare più tardi oppure a inviare un messaggio su WhatsApp."
                );
            }

            return buildGatherResponse(
                    "Mi scusi, non ho capito bene. Può ripetere il problema con poche parole?"
            );
        }

        session.cronologiaMessaggi.add(new ChatMessage("user", speechResult));

        AIResponse aiResponse = openAIService.askLucrezia(speechResult, session);

        String reply = aiResponse.getReply();

        if (reply == null || reply.isBlank()) {
            reply = "Mi scusi, ho avuto un problema nel capire la richiesta. Può ripetere?";
        }

        session.cronologiaMessaggi.add(new ChatMessage("assistant", reply));
        session.primoMessaggio = false;

        trimHistory(session);

        if (!aiResponse.isOpen_ticket()) {
            session.tentativiComprensione++;

            if (session.tentativiComprensione >= 3) {

                Long idTicket = ticketDao.insertTicket(
                        utente.getIdCondominio(),
                        utente.getId(),
                        "generico",
                        "media",
                        "TELEFONO",
                        buildDescrizioneDaCronologia(session, speechResult)
                );

                voiceSessionService.removeSession(phone);

                if (idTicket == null) {
                    return buildSayResponse(
                            "Mi dispiace, ho raccolto le informazioni principali ma non sono riuscita ad aprire la segnalazione. La invito a riprovare più tardi."
                    );
                }

                return buildSayResponse(
                        "Grazie, ho raccolto le informazioni principali. Per non farle perdere altro tempo, ho aperto una segnalazione generica. Il numero ticket è " +
                        idTicket +
                        ". Verrà ricontattato se serviranno ulteriori dettagli."
                );
            }

            return buildGatherResponse(reply);
        }

        Long idTicket = ticketDao.insertTicket(
                utente.getIdCondominio(),
                utente.getId(),
                safe(aiResponse.getCategory(), "generico"),
                safe(aiResponse.getPriority(), "media"),
                "TELEFONO",
                buildDescrizioneDaCronologia(session, speechResult)
        );

        voiceSessionService.removeSession(phone);

        if (idTicket == null) {
            return buildSayResponse(
                    "Mi dispiace, ho capito la segnalazione ma non sono riuscita ad aprire il ticket. La invito a riprovare più tardi."
            );
        }

        String categoria = safe(aiResponse.getCategory(), "generico");
        String priorita = safe(aiResponse.getPriority(), "media");

        return buildSayResponse(
                "Perfetto, grazie " + safe(utente.getNome(), "") +
                ". Ho aperto il ticket numero " + idTicket +
                " per il condominio " + safe(utente.getNomeCondominio(), "") +
                ". La categoria è " + categoria +
                " e la priorità è " + priorita +
                ". Riceverà aggiornamenti appena possibile."
        );
    }

    private String buildGatherResponse(String message) {

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Gather input="speech"
                        language="it-IT"
                        speechTimeout="auto"
                        timeout="7"
                        action="/voice/gather"
                        method="POST">
                    <Say language="it-IT" voice="%s">
                        %s
                    </Say>
                </Gather>

                <Say language="it-IT" voice="%s">
                    Non ho sentito nessuna risposta. Può richiamarmi quando vuole.
                </Say>
            </Response>
            """.formatted(
                TWILIO_VOICE,
                escapeXml(message),
                TWILIO_VOICE
        );
    }

    private String buildSayResponse(String message) {

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Say language="it-IT" voice="%s">
                    %s
                </Say>
            </Response>
            """.formatted(
                TWILIO_VOICE,
                escapeXml(message)
        );
    }

    private String buildDescrizioneDaCronologia(UserSession session, String ultimoMessaggio) {

        StringBuilder sb = new StringBuilder();

        if (session != null && session.cronologiaMessaggi != null && !session.cronologiaMessaggi.isEmpty()) {
            for (ChatMessage chatMessage : session.cronologiaMessaggi) {
                if ("user".equals(chatMessage.getRole())) {
                    sb.append(chatMessage.getContent()).append(" ");
                }
            }
        }

        if (sb.isEmpty() && ultimoMessaggio != null) {
            sb.append(ultimoMessaggio);
        }

        return sb.toString().trim();
    }

    private String safe(String value, String defaultValue) {

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    private void trimHistory(UserSession session) {

        if (session.cronologiaMessaggi != null && session.cronologiaMessaggi.size() > 20) {
            session.cronologiaMessaggi =
                    session.cronologiaMessaggi.subList(
                            session.cronologiaMessaggi.size() - 20,
                            session.cronologiaMessaggi.size()
                    );
        }
    }

    private String escapeXml(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}