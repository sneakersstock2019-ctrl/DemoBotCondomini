package it.sd.demo.bot.condomini.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TicketDao {

    private final DataSource dataSource;

    public boolean hasTicketApertiByUtente(Long idUtente) {

        String sql = """
            SELECT COUNT(*)
            FROM ticket t
            JOIN stati_ticket st ON st.id = t.id_stato
            WHERE t.id_utente_apertura = ?
              AND st.codice NOT IN ('RISOLTO', 'CHIUSO')
            """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public Long insertTicket(Long idCondominio,
                             Long idUtenteApertura,
                             String categoria,
                             String priorita,
                             String canale,
                             String descrizione) {

        String sql = """
            INSERT INTO ticket (
                id_condominio,
                id_utente_apertura,
                id_stato,
                categoria,
                priorita,
                canale,
                descrizione,
                data_ultimo_aggiornamento
            )
            VALUES (
                ?,
                ?,
                (SELECT id FROM stati_ticket WHERE codice = 'APERTO'),
                ?,
                ?,
                ?,
                ?,
                CURRENT_TIMESTAMP
            )
            """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setLong(1, idCondominio);
            ps.setLong(2, idUtenteApertura);
            ps.setString(3, categoria);
            ps.setString(4, priorita);
            ps.setString(5, canale);
            ps.setString(6, descrizione);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long idTicket = rs.getLong(1);
                    insertStorico(conn, idTicket, "APERTO", idUtenteApertura, "Ticket aperto da " + canale);
                    return idTicket;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void insertStorico(Connection conn,
                               Long idTicket,
                               String codiceStato,
                               Long idUtente,
                               String nota) throws Exception {

        String sql = """
            INSERT INTO ticket_storico (
                id_ticket,
                id_stato,
                id_utente,
                nota
            )
            VALUES (
                ?,
                (SELECT id FROM stati_ticket WHERE codice = ?),
                ?,
                ?
            )
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTicket);
            ps.setString(2, codiceStato);

            if (idUtente != null) {
                ps.setLong(3, idUtente);
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }

            ps.setString(4, nota);
            ps.executeUpdate();
        }
    }
}