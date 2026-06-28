package it.sd.demo.bot.condomini.realtime.tool;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.sd.demo.bot.condomini.bean.VoiceContext;
import it.sd.demo.bot.condomini.service.WhatsAppService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LucreziaToolDispatcher {

    private final List<LucreziaTool> toolList;
    private final WhatsAppService whatsAppService;

    private Map<String, LucreziaTool> tools;

    public String execute(String name, String arguments, VoiceContext context) {

        if (tools == null) {
            tools = toolList.stream()
                    .collect(Collectors.toMap(LucreziaTool::getName, t -> t));
        }

        LucreziaTool tool = tools.get(name);

        if (tool == null) {
            return """
                    {"errore":true,"messaggio":"Tool non riconosciuto."}
                    """;
        }

        String result = tool.execute(arguments, context);

        /*
         * Caso speciale:
         * endCall serve solo a impostare context.endCallRequested = true.
         * Non dobbiamo mandare function output a OpenAI, altrimenti genera
         * un'altra risposta parlata dopo il saluto finale.
         */
        if ("endCall".equals(name)) {
            System.out.println("END CALL TOOL ESEGUITO - nessun output verso OpenAI");
            return null;
        }

        handlePostToolActions(name, result, context);

        return result;
    }

    private void handlePostToolActions(String toolName,
                                       String resultJson,
                                       VoiceContext context) {

        if (!"createTicket".equals(toolName)) {
            return;
        }

        try {
            JsonNode root = new ObjectMapper().readTree(resultJson);

            if (!"OK".equals(root.path("esito").asText())) {
                return;
            }

            boolean richiediFoto = root.path("richiedi_foto").asBoolean(false);

            if (!richiediFoto) {
                return;
            }

            Long ticketId = root.path("ticket_id").asLong();

            whatsAppService.inviaRichiestaFotoPostChiamata(
                    context.getPhone(),
                    context.getNome(),
                    ticketId
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}