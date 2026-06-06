package it.sd.demo.bot.condomini.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {

    private String reply;

    @JsonProperty("open_ticket")
    private boolean openTicket;

    private String category;

    private String priority;

    @JsonProperty("common_area")
    private Boolean commonArea;

    @JsonProperty("private_area")
    private Boolean privateArea;

    @JsonProperty("needs_attachment")
    private Boolean needsAttachment;

    @JsonProperty("attachment_request")
    private String attachmentRequest;

    @JsonProperty("ticket_description")
    private String ticketDescription;

    public boolean isOpen_ticket() {
        return openTicket;
    }
}