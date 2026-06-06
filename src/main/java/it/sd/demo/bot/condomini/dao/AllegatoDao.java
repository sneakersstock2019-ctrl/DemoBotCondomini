package it.sd.demo.bot.condomini.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AllegatoDao {

    private final DataSource dataSource;

    public void insertAllegato(Long idTicket,
                               String tipo,
                               String nomeFile,
                               String url,
                               String contentType,
                               String canaleOrigine) {

        String sql = """
            INSERT INTO allegati (
                id_ticket,
                tipo,
                nome_file,
                url,
                content_type,
                canale_origine
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, idTicket);
            ps.setString(2, tipo);
            ps.setString(3, nomeFile);
            ps.setString(4, url);
            ps.setString(5, contentType);
            ps.setString(6, canaleOrigine);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}