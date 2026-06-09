package it.sd.demo.bot.condomini.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import it.sd.demo.bot.condomini.service.TwilioCallService;

@RestController
@RequestMapping("/demo")
public class DemoCallController {

    @Autowired
    private TwilioCallService twilioCallService;

    @GetMapping("/call-ticket")
    public String callTicket(@RequestParam String telefono,
                             @RequestParam(required = false) Long idTicket) {

        twilioCallService.notifyTicketCreated("+" + telefono);

        return "Chiamata inviata";
    }
}