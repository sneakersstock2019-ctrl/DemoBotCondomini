package it.sd.demo.bot.condomini.bean;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketStorico {

    private Long id;

    private Long idTicket;

    private String stato;

    private Long idUtente;

    private String nota;

    private LocalDateTime dataEvento;
}