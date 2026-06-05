package it.sd.demo.bot.condomini.controller;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbTestController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/db-test")
    public String test() {

        try (Connection conn = dataSource.getConnection()) {

            return "OK - "
                    + conn.getMetaData().getDatabaseProductName()
                    + " - "
                    + conn.getMetaData().getDatabaseProductVersion();

        } catch (Exception e) {

            e.printStackTrace();

            return "ERRORE: " + e.getMessage();
        }
    }
}