package it.sd.demo.bot.condomini.bean;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Allegato {

    private Long id;

    private Long idTicket;

    private String tipo;

    private String nomeFile;

    private String url;

    private String contentType;

    private String canaleOrigine;

    private LocalDateTime dataCreazione;
}