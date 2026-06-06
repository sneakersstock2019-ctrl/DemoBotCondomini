package it.sd.demo.bot.condomini.util;

import org.springframework.stereotype.Component;

@Component
public class PhoneUtils {

    public String normalizePhone(String phone) {

        if (phone == null || phone.isBlank()) {
            return null;
        }

        phone = phone.trim();
        phone = phone.replace("+", "");

        if (phone.startsWith("0039")) {
            phone = phone.substring(4);
        }

        if (phone.startsWith("39") && phone.length() > 10) {
            phone = phone.substring(2);
        }

        return phone;
    }
}