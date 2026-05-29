package it.sd.demo.bot.condomini.controller;

import it.sd.demo.bot.condomini.bean.TicketView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("adminName", "Renato Zaino");
        model.addAttribute("selectedCondominio", "Tutti");

        model.addAttribute("ticketAperti", 8);
        model.addAttribute("ticketUrgenti", 2);
        model.addAttribute("daApprovare", 3);
        model.addAttribute("fornitoriAttivi", 5);

        model.addAttribute("tickets", List.of(
                new TicketView("Mario Rossi", "Via Europa", "Perdita acqua nel vano scale", "idraulico", "P1", "In attesa", "oggi 09:42"),
                new TicketView("Laura Bianchi", "Via Puglia", "Luce scale non funzionante", "elettricista", "P2", "Da assegnare", "oggi 10:15"),
                new TicketView("Michele Verdi", "Via Europa", "Richiesta installazione pannelli fotovoltaici", "amministrazione", "P3", "Da assemblea", "ieri 18:20")
        ));

        return "dashboard";
    }
}