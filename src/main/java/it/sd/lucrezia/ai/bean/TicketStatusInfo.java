package it.sd.lucrezia.ai.bean;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TicketStatusInfo {

    private Long id;
    private String categoria;
    private String priorita;
    private String descrizione;

    private String statoCodice;
    private String statoDescrizione;

    private LocalDateTime dataUltimoAggiornamento;
    private LocalDateTime dataInterventoPrevista;

    private String nomeFornitore;
}