package it.sd.demo.bot.condomini.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.sd.demo.bot.condomini.service.OpenAIService;
import it.sd.demo.bot.condomini.service.TwilioService;

@RestController
@RequestMapping("/voice")
public class VoiceController {
	
	@Autowired
	private TwilioService twilioService;

	@Autowired
	private OpenAIService openAIService;

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
                            @RequestParam(value = "From", required = false) String from) throws Exception {

        System.out.println("Chiamata da: " + from);
        System.out.println("Audio registrato: " + recordingUrl);

        File audioFile =
                twilioService.downloadRecording(recordingUrl);

        String trascrizione =
                openAIService.transcribeAudio(audioFile);

        System.out.println("########################");
        System.out.println("TRASCRIZIONE:");
        System.out.println(trascrizione);
        System.out.println("########################");

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