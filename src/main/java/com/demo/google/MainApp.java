package com.demo.google;


import com.demo.google.service.CredentialsService;
import com.demo.google.service.GmailService;
import com.demo.google.service.SheetsService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class MainApp {

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(MainApp.class.getName());
        logger.log(java.util.logging.Level.INFO, "Starting application");
        Credential credential = (new CredentialsService()).getCredentials();
        GmailService gmailService = new GmailService(credential);
        SheetsService sheetsService = new SheetsService(credential);

        List<Message> messages = gmailService.getMessages();

        Set<String> senders = gmailService.getSenders(messages);

        Spreadsheet spreadsheet = sheetsService.createSpreadsheet("Gmail Unique Senders");

        AppendValuesResponse appendValuesResponse =  sheetsService.writeSendersToSheet(spreadsheet, senders);

        logger.log(java.util.logging.Level.INFO, String.format("Wrote %s cells to sheet.", appendValuesResponse.getUpdates().getUpdatedCells()));

    }
}
