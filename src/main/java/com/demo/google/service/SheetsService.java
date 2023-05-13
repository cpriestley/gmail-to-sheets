package com.demo.google.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SheetsService {

    Logger logger;
    private final Sheets service;

    public SheetsService(Credential credential) {
        this.logger = Logger.getLogger(SheetsService.class.getName());
        service = new Sheets.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                .setApplicationName("Test Scraper")
                .build();
        logger.log(Level.INFO, "SheetsService instantiated");
    }

    public Spreadsheet createSpreadsheet(String title) throws IOException {
        logger.log(Level.INFO, String.format("Creating new spreadsheet: %s", title));
        Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(title));
        spreadsheet = service.spreadsheets().create(spreadsheet).setFields("spreadsheetId").execute();
        String spreadsheetId = spreadsheet.getSpreadsheetId();
        logger.log(Level.INFO, String.format("Returning spreadsheet: %s %s", title, spreadsheetId));
        return spreadsheet;
    }

    private AppendValuesResponse appendValues(String spreadsheetId,
                                                    String range,
                                                    String valueInputOption,
                                                    List<List<Object>> values)
            throws IOException {

        AppendValuesResponse result = null;
        try {
            // Append values to the specified range.
            ValueRange body = new ValueRange().setMajorDimension("COLUMNS").setValues(values);
            logger.log(Level.INFO, "Writing values to spreadsheet");
            result = service.spreadsheets().values().append(spreadsheetId, range, body)
                    .setValueInputOption(valueInputOption)
                    .execute();
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                logger.log(Level.WARNING, String.format("Spreadsheet not found with id '%s'.\n", spreadsheetId));
            } else {
                throw e;
            }
        }
        logger.log(Level.INFO, "Returning append values response");
        return result;
    }

    public AppendValuesResponse writeSendersToSheet(Spreadsheet spreadsheet, List<Object> senders) throws IOException {
        return appendValues(spreadsheet.getSpreadsheetId(), "A1", "USER_ENTERED", List.of(senders));
    }


}
