package it.sd.demo.bot.condomini.bean;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    private Long id;

    private Long idCondominio;
    private Long idUtenteApertura;
    private Long idFornitore;

    private String categoria;
    private String priorita;
    private String stato;

    private String canale;
    private String descrizione;

    private LocalDateTime dataApertura;
    private LocalDateTime dataUltimoAggiornamento;
}