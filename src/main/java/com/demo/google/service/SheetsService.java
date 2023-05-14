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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            values = cleanValues(values);
            values.get(0).add(0, "Sender");
            // Append values to the specified range, starting at row 1.
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
        AppendValuesResponse appendValuesResponse = appendValues(
                spreadsheet.getSpreadsheetId(), "A1", "USER_ENTERED", List.of(senders)
        );
        logger.log(Level.INFO, String.format("Wrote %s cells to sheet.", appendValuesResponse.getUpdates().getUpdatedCells()));
        return appendValuesResponse;
    }

    private List<List<Object>> cleanValues(List<List<Object>> values) {
        logger.log(Level.INFO, "Cleaning 'From' values to extract only the email addresses.");
        List<Object> senders = new ArrayList<>(values.get(0));
        Object[] newList = senders.stream().map(sender -> {
            Pattern pattern = Pattern.compile("<(.*?)>");
            Matcher matcher = pattern.matcher((String) sender);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return sender;
        }).toArray();
        logger.log(Level.INFO, "Return cleaned values.");
        return List.of(new ArrayList<>(Arrays.asList(newList)));
    }

}
