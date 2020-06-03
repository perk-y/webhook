package com.lattice.webhook;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

class SheetsQuickstart {
  private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
   * Global instance of the scopes required by this quickstart. If modifying these scopes, delete
   * your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.DRIVE);

  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  void updateValues(String spreadsheetId, ArrayList issueInfo)
      throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> values = Collections.singletonList(issueInfo);

    ValueRange body = new ValueRange().setValues(values);
    AppendValuesResponse result =
        service
            .spreadsheets()
            .values()
            .append(spreadsheetId, "A2:D", body)
            .setValueInputOption("USER_ENTERED")
            .execute();
    System.out.printf("%d cells updated.", result.getUpdates().getUpdatedCells());
  }

  void updateAssignees(String spreadsheetId, String assigneeList, int issueNumber)
      throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> a =
        service.spreadsheets().values().get(spreadsheetId, "A:A").execute().getValues();
    ArrayList list =
        (ArrayList) a.stream().flatMap(Collection::stream).collect(Collectors.toList());
    int rowNumber;

    ClearValuesRequest requestBody = new ClearValuesRequest();

    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;

      Sheets.Spreadsheets.Values.Clear request =
          service
              .spreadsheets()
              .values()
              .clear(spreadsheetId, "F" + rowNumber, requestBody);
      request.execute();
    }

    List<List<Object>> values = Collections.singletonList(Collections.singletonList(assigneeList));

    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;

      ValueRange body = new ValueRange().setValues(values);
      UpdateValuesResponse result =
          service
              .spreadsheets()
              .values()
              .update(spreadsheetId, "F" + rowNumber, body)
              .setValueInputOption("RAW")
              .execute();
      System.out.printf("%d cells updated.", result.getUpdatedCells());
      return;
    }
    System.out.println(" Requested Resource Not Found");
    new UpdateValuesResponse();
  }

  void changeIssueState(String spreadSheetId, int issueNumber, String state)
      throws IOException, GeneralSecurityException {

    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> values = Collections.singletonList(Collections.singletonList(state));
    List<List<Object>> a =
        service.spreadsheets().values().get(spreadSheetId, "A:A").execute().getValues();
    ArrayList list =
        (ArrayList) a.stream().flatMap(Collection::stream).collect(Collectors.toList());
    int rowNumber;
    System.out.println(Arrays.toString(list.toArray()));
    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;
      System.out.println("Row Number" + rowNumber);
      ValueRange body = new ValueRange().setValues(values);
      UpdateValuesResponse result =
          service
              .spreadsheets()
              .values()
              .update(spreadSheetId, "D" + rowNumber, body)
              .setValueInputOption("RAW")
              .execute();
      System.out.printf("%d cells updated.", result.getUpdatedCells());
    }
    System.out.println(" Requested Resource Not Found");
    new UpdateValuesResponse();
  }

  void updateIssueTitle(String spreadSheetId, int issueNumber, String issueTitle)
      throws GeneralSecurityException, IOException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> values = Collections.singletonList(Collections.singletonList(issueTitle));
    List<List<Object>> a =
        service.spreadsheets().values().get(spreadSheetId, "A:A").execute().getValues();
    ArrayList list =
        (ArrayList) a.stream().flatMap(Collection::stream).collect(Collectors.toList());
    int rowNumber;
    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;
      ValueRange body = new ValueRange().setValues(values);
      UpdateValuesResponse result =
          service
              .spreadsheets()
              .values()
              .update(spreadSheetId, "B" + rowNumber, body)
              .setValueInputOption("USER_ENTERED")
              .execute();
      System.out.printf("%d cells updated.", result.getUpdatedCells());
    }
    System.out.println(" Requested Resource Not Found");
    new UpdateValuesResponse();
  }

  void updateMilestone(String spreadSheetId, int issueNumber, String milestoneTitle)
      throws GeneralSecurityException, IOException {

    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> values =
        Collections.singletonList(Collections.singletonList(milestoneTitle));
    List<List<Object>> a =
        service.spreadsheets().values().get(spreadSheetId, "A:A").execute().getValues();
    ArrayList list =
        (ArrayList) a.stream().flatMap(Collection::stream).collect(Collectors.toList());
    int rowNumber;
    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;
      ValueRange body = new ValueRange().setValues(values);
      UpdateValuesResponse result =
          service
              .spreadsheets()
              .values()
              .update(spreadSheetId, "E" + rowNumber, body)
              .setValueInputOption("USER_ENTERED")
              .execute();
      System.out.printf("%d cells updated.", result.getUpdatedCells());
    }
    System.out.println(" Requested Resource Not Found");
    new UpdateValuesResponse();
  }

  void demilestone(String spreadSheetId, int issueNumber)
      throws GeneralSecurityException, IOException {

    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> a =
        service.spreadsheets().values().get(spreadSheetId, "A:A").execute().getValues();
    ArrayList list =
        (ArrayList) a.stream().flatMap(Collection::stream).collect(Collectors.toList());
    int rowNumber;

    ClearValuesRequest requestBody = new ClearValuesRequest();

    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;

      Sheets.Spreadsheets.Values.Clear request =
          service.spreadsheets().values().clear(spreadSheetId, "E" + rowNumber, requestBody);
      request.execute();
    }
    System.out.println(" Requested Resource Not Found");
    new UpdateValuesResponse();
  }

  void updateLabels(String spreadSheetId, String labels, int issueNumber)
      throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> a =
        service.spreadsheets().values().get(spreadSheetId, "A:A").execute().getValues();
    ArrayList list =
        (ArrayList) a.stream().flatMap(Collection::stream).collect(Collectors.toList());
    int rowNumber;

    ClearValuesRequest requestBody = new ClearValuesRequest();

    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;
      Sheets.Spreadsheets.Values.Clear request =
          service.spreadsheets().values().clear(spreadSheetId, "G" + rowNumber, requestBody);
      request.execute();
    }

    List<List<Object>> values = Collections.singletonList(Collections.singletonList(labels));

    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;

      ValueRange body = new ValueRange().setValues(values);
      UpdateValuesResponse result =
          service
              .spreadsheets()
              .values()
              .update(spreadSheetId, "G" + rowNumber, body)
              .setValueInputOption("RAW")
              .execute();
      System.out.printf("%d cells updated.", result.getUpdatedCells());
      return;
    }
    System.out.println(" Requested Resource Not Found");
    new UpdateValuesResponse();
  }
}
