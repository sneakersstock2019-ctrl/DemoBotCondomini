package it.sd.demo.bot.condomini.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utente {

    private Long id;

    private String nome;
    private String cognome;

    private String email;
    private String telefono;

    private String ruolo;

    private Long idCondominio;
    private String nomeCondominio;

    public String getNomeCompleto() {
        return (nome != null ? nome : "")
                + (cognome != null ? " " + cognome : "");
    }
}