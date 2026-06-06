package it.sd.demo.bot.condomini.bean;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllegatoTemporaneo {

    private Long id;
    private String telefono;
    private String tipo;
    private String mediaId;
    private String contentType;
    private String nomeFile;
    private LocalDateTime dataCreazione;
}