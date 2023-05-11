package com.demo.google.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class GmailService {

    Logger logger;
    private final Gmail service;

    public GmailService(Credential credential) {
        this.logger = Logger.getLogger(GmailService.class.getName());
        service = new Gmail.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                .setApplicationName("Test Scraper")
                .build();
        logger.log(java.util.logging.Level.INFO, "GmailService instantiated");
    }

    public List<Message> getMessages() throws IOException {
        // Get the messages from the user's mailbox
        String user = "me";
        String pageToken = null;

        logger.log(java.util.logging.Level.INFO, "Getting emails from gmail");
        List<Message> messages = new ArrayList<>();
        do {
            logger.log(java.util.logging.Level.INFO, String.format("Getting emails from pageToken: %s", pageToken));
            ListMessagesResponse response = service.users().messages().list(user).setPageToken(pageToken).execute();
            messages.addAll(response.getMessages());
            pageToken = response.getNextPageToken();
        } while (pageToken != null);

        logger.log(java.util.logging.Level.INFO, "Returning emails");
        return messages;
    }

    public Set<String> getSenders(List<Message> messages) throws IOException {
        Set<String> senders = new HashSet<>();
        long counter = 0;
        final int total = messages.size();
        logger.log(java.util.logging.Level.INFO, "Begin getting senders from emails");
        for (Message message : messages) {
            counter++;
            Message fullMessage = service.users().messages().get("me", message.getId()).execute();
            String sender = fullMessage.getPayload().getHeaders().stream()
                    .filter(header -> header.getName().equals("From"))
                    .findFirst()
                    .map(MessagePartHeader::getValue)
                    .orElse("Unknown Sender");
            senders.add(sender);
            if ((counter % 100) == 0) {
                logger.log(java .util.logging.Level.INFO, String.format("Processed %d emails of %d", counter, total));
            }
        }
        return senders;
    }

}
