package com.demo.google.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.api.services.gmail.GmailScopes.GMAIL_READONLY;
import static com.google.api.services.gmail.GmailScopes.GMAIL_SEND;
import static com.google.api.services.sheets.v4.SheetsScopes.SPREADSHEETS;

public class CredentialsService {
    Logger logger;

    public CredentialsService() {
        this.logger = Logger.getLogger(CredentialsService.class.getName());
        logger.log(Level.INFO, "CredentialsService instantiated");
    }

    public Credential getCredentials()
            throws IOException, GeneralSecurityException {
        logger.log(Level.INFO, "Getting credentials");
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        InputStream in = GmailService.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + "/credentials.json");
        }

        logger.log(Level.INFO, "Getting client secrets");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        //Create request initializer to increase the timeout
        HttpRequestInitializer requestInitializer = (httpRequest) -> httpRequest.setReadTimeout(30000);

        logger.log(Level.INFO, "Building flow and triggering user authorization request");
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, Set.of(GMAIL_READONLY, GMAIL_SEND, SPREADSHEETS))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .setRequestInitializer(requestInitializer)
                .build();

        logger.log(Level.INFO, "Getting local server receiver.");
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


}
