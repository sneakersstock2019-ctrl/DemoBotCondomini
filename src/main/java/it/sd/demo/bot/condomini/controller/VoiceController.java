package it.sd.demo.bot.condomini.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voice")
public class VoiceController {

    @PostMapping(value = "/incoming", produces = "application/xml")
    public String incomingCall() {
        return """
            <Response>
                <Say language="it-IT" voice="alice">
                    Buongiorno, sono Lucrezia, l'assistente virtuale del condominio.
                    Dopo il segnale acustico descriva il problema. Aprirò una segnalazione per lei.
                </Say>
                <Record action="/voice/recording"
                        method="POST"
                        maxLength="60"
                        playBeep="true"
                        trim="trim-silence"/>
            </Response>
            """;
    }

    @PostMapping(value = "/recording", produces = "application/xml")
    public String recording(@RequestParam("RecordingUrl") String recordingUrl,
                            @RequestParam(value = "From", required = false) String from) {

        System.out.println("Chiamata da: " + from);
        System.out.println("Audio registrato: " + recordingUrl);

        // step successivo:
        // 1. scaricare recordingUrl + ".mp3"
        // 2. trascrivere con OpenAI
        // 3. aprire ticket
        // 4. restituire conferma vocale

        return """
            <Response>
                <Say language="it-IT" voice="alice">
                    Grazie, ho ricevuto la sua segnalazione.
                    A breve verrà aperto un ticket e riceverà aggiornamenti.
                </Say>
            </Response>
            """;
    }
}