package it.sd.lucrezia.ai.tool;

import it.sd.lucrezia.ai.bean.VoiceContext;

public interface LucreziaTool {

    String getName();

    String execute(String arguments, VoiceContext context);
}