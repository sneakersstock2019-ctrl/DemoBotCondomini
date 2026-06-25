package it.sd.demo.bot.condomini.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.sd.demo.bot.condomini.bean.TicketStatusInfo;
import it.sd.demo.bot.condomini.dao.TicketDao;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LucreziaRealtimeToolService {

    private final TicketDao ticketDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getOpenTicketsJson(Long idUtente) {

        try {
            List<TicketStatusInfo> tickets =
                    ticketDao.findOpenTicketsByUtente(idUtente);

            return objectMapper.writeValueAsString(
                    Map.of(
                            "numero_ticket_aperti", tickets.size(),
                            "ticket", tickets
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();

            try {
                return objectMapper.writeValueAsString(
                        Map.of(
                                "errore", true,
                                "messaggio", "Non sono riuscita a recuperare le segnalazioni aperte."
                        )
                );
            } catch (Exception ex) {
                return "{\"errore\":true}";
            }
        }
    }
}