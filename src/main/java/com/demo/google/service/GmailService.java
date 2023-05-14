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
import java.util.logging.Level;
import java.util.logging.Logger;

public class GmailService {

    Logger logger;
    private final Gmail service;
    private String pageToken = null;

    public GmailService(Credential credential) {
        this.logger = Logger.getLogger(GmailService.class.getName());
        service = new Gmail.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                .setApplicationName("Test Scraper")
                .build();
        logger.log(Level.INFO, "GmailService instantiated");
    }

    private ListMessagesResponse getListMessagesResponse(String pageToken) throws IOException {
        String user = "me";
        logger.log(Level.INFO, String.format("Getting emails from %s", pageToken == null ? "first page" : "pageToken: " + pageToken));
        return service.users().messages().list(user).setPageToken(pageToken).execute();
    }

    public List<Message> getMessagesFromFirstPage() throws IOException {
        return getMessagesFromPageToken(null);
    }

    public List<Message> getMessagesFromPageToken(String token) throws IOException {
        ListMessagesResponse response = getListMessagesResponse(token);
        pageToken = response.getNextPageToken();
        logger.log(Level.INFO, String.format("Returning emails from %s", pageToken == null ? "first page" : "pageToken: " + pageToken));
        return response.getMessages();
    }

    public List<Message> getAllMessages() throws IOException {
        pageToken = null;
        List<Message> messages = new ArrayList<>();
        do {
            messages.addAll(getMessagesFromPageToken(pageToken));
        } while (pageToken != null);

        logger.log(Level.INFO, "Returning all emails");
        return messages;
    }

    public Set<String> getSenders(List<Message> messages) throws IOException {
        Set<String> senders = new HashSet<>();
        long counter = 0;
        final int total = messages.size();
        logger.log(Level.INFO, "Begin getting senders from emails");
        for (Message message : messages) {
            counter++;
            Message fullMessage = service.users().messages().get("me", message.getId()).execute();
            String sender = fullMessage.getPayload().getHeaders().stream()
                    .filter(header -> header.getName().equals("From"))
                    .findFirst()
                    .map(MessagePartHeader::getValue)
                    .orElse("Unknown Sender");
            senders.add(sender);
            if ((counter % 50) == 0) {
                logger.log(java .util.logging.Level.INFO, String.format("Processed %d emails of %d", counter, total));
            }
        }
        return senders;
    }

    public Set<String> getAllSenders() {

        try {
            return new HashSet<>(getSenders(getAllMessages()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
