package it.sd.demo.bot.condomini.realtime.tool;

import it.sd.demo.bot.condomini.bean.VoiceContext;

public interface LucreziaTool {

    String getName();

    String execute(String arguments, VoiceContext context);
}