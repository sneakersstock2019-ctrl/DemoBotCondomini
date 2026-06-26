package it.sd.demo.bot.condomini.realtime.tool;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import it.sd.demo.bot.condomini.bean.VoiceContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LucreziaToolDispatcher {

    private final List<LucreziaTool> toolList;

    private Map<String, LucreziaTool> tools;

    public String execute(String name, String arguments, VoiceContext context) {

        if (tools == null) {
            tools = toolList.stream()
                    .collect(Collectors.toMap(LucreziaTool::getName, t -> t));
        }

        LucreziaTool tool = tools.get(name);

        if (tool == null) {
            return """
                {"errore":true,"messaggio":"Tool non riconosciuto."}
                """;
        }

        return tool.execute(arguments, context);
    }
}