package com.demo.google;


import com.demo.google.service.CredentialsService;
import com.demo.google.service.GmailService;
import com.demo.google.service.SheetsService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp {

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(MainApp.class.getName());
        logger.log(Level.INFO, "Starting application");
        Credential credential = (new CredentialsService()).getCredentials();

        GmailService gmailService = new GmailService(credential);
        List<Object> senders = new ArrayList<>(gmailService.getAllSenders());

        SheetsService sheetsService = new SheetsService(credential);
        Spreadsheet spreadsheet = sheetsService.createSpreadsheet("Gmail Unique Senders");
        sheetsService.writeSendersToSheet(spreadsheet, senders);
        logger.log(Level.INFO, "Application finished");
    }
}
