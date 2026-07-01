package it.sd.lucrezia.ai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CondominioAiDao {

    private final DataSource dataSource;

    public String getContestoAiByCondominio(Long idCondominio) {

        String sql = """
            SELECT tipo, titolo, contenuto
            FROM documenti_condominio_ai
            WHERE id_condominio = ?
              AND attivo = true
            ORDER BY data_creazione DESC
            """;

        StringBuilder contesto = new StringBuilder();

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, idCondominio);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contesto.append("\n--- ")
                            .append(rs.getString("tipo"))
                            .append(" - ")
                            .append(rs.getString("titolo"))
                            .append(" ---\n");

                    contesto.append(rs.getString("contenuto"))
                            .append("\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return contesto.toString();
    }
}