package it.sd.lucrezia.ai.voice.filter;

public final class SpeechFilter {

    private SpeechFilter() {
    }

    public static boolean isNoiseOrFiller(String text) {

        if (text == null) {
            return true;
        }

        String value = normalize(text);

        if (value.isBlank()) {
            return true;
        }

        return value.equals("eh")
                || value.equals("e")
                || value.equals("mmm")
                || value.equals("mh")
                || value.equals("uh")
                || value.equals("uh huh")
                || value.equals("ok")
                || value.equals("okay")
                || value.equals("ciao")
                || value.equals("buongiorno")
                || value.equals("buonasera")
                || value.equals("lucrezia");
    }

    public static boolean isMeaningfulInterrupt(String text) {

        if (text == null) {
            return false;
        }

        String value = normalize(text);

        if (isNoiseOrFiller(value)) {
            return false;
        }

        return value.contains("aspetta")
                || value.contains("fermati")
                || value.contains("scusa")
                || value.contains("no")
                || value.contains("volevo")
                || value.contains("dimmi")
                || value.contains("ripeti")
                || value.contains("non ho capito")
                || value.contains("la seconda")
                || value.contains("la terza")
                || value.contains("quella")
                || value.contains("segnalazione");
    }

    private static String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zàèéìòù0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}