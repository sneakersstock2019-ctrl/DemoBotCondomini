package it.sd.demo.bot.condomini.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Condominio {

    private Long id;

    private String nome;

    private String indirizzo;

    private String codiceFiscale;
}