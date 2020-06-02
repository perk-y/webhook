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
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SheetsQuickstart {
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

  /**
   * Prints the names and majors of students in a sample spreadsheet:
   * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
   */
  public static void main(String... args) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
    final String range = "Class Data!A2:E";
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
    List<List<Object>> values = response.getValues();
    if (values == null || values.isEmpty()) {
      System.out.println("No data found.");
    } else {
      System.out.println("Name, Major");
      for (List row : values) {
        // Print columns A and E, which correspond to indices 0 and 4.
        System.out.printf("%s, %s\n", row.get(0), row.get(4));
      }
    }
  }

  public AppendValuesResponse updateValues(
      String spreadsheetId, String valueInputOption, ArrayList issueInfo)
      throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> values =
        Arrays.asList(
            issueInfo
            // Additional rows ...
            );

    ValueRange body = new ValueRange().setValues(values);
    AppendValuesResponse result =
        service
            .spreadsheets()
            .values()
            .append(spreadsheetId, "A2:D", body)
            .setValueInputOption(valueInputOption)
            .setInsertDataOption("INSERT_ROWS")
            .execute();
    System.out.printf("%d cells updated.", result.getUpdates().getUpdatedCells());
    return result;
  }

  public AppendValuesResponse updateAssignees(
      String spreadsheetId, String valueInputOption, ArrayList assigneeList, int issueNumber)
      throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> values =
        Arrays.asList(
            assigneeList
            // Additional rows ...
            );
    List<List<Object>> a =
        service.spreadsheets().values().get(spreadsheetId, "A:A").execute().getValues();
    ArrayList list =
        (ArrayList) a.stream().flatMap(value -> value.stream()).collect(Collectors.toList());
    int rowNumber;
    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;

      ValueRange body = new ValueRange().setValues(values);
      AppendValuesResponse result =
          service
              .spreadsheets()
              .values()
              .append(spreadsheetId, "F" + rowNumber, body)
              .setValueInputOption(valueInputOption)
              .setInsertDataOption("OVERWRITE")
              .execute();
      System.out.printf("%d cells updated.", result.getUpdates().getUpdatedCells());
      return result;
    }
    System.out.println(" Requested Resource Not Found");
    return new AppendValuesResponse();
  }

  public void changeIssueState(String spreadSheetId, int issueNumber, String state)
      throws IOException, GeneralSecurityException {

    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Sheets service =
        new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    List<List<Object>> values = Arrays.asList(Arrays.asList(state));
    List<List<Object>> a =
        service.spreadsheets().values().get(spreadSheetId, "A:A").execute().getValues();
    ArrayList list =
        (ArrayList) a.stream().flatMap(value -> value.stream()).collect(Collectors.toList());
    int rowNumber;
    if (list.contains(String.valueOf(issueNumber))) {
      rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;
      ValueRange body = new ValueRange().setValues(values);
      AppendValuesResponse result =
          service
              .spreadsheets()
              .values()
              .append(spreadSheetId, "D" + rowNumber, body)
              .setValueInputOption("RAW")
              .setInsertDataOption("OVERWRITE")
              .execute();
      System.out.printf("%d cells updated.", result.getUpdates().getUpdatedCells());
    }
  }

    public void updateIssueTitle(String spreadSheetId, int issueNumber, String issueTitle) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        List<List<Object>> values = Arrays.asList(Arrays.asList(issueTitle));
        List<List<Object>> a =
                service.spreadsheets().values().get(spreadSheetId, "A:A").execute().getValues();
        ArrayList list =
                (ArrayList) a.stream().flatMap(value -> value.stream()).collect(Collectors.toList());
        int rowNumber;
        if (list.contains(String.valueOf(issueNumber))) {
            rowNumber = list.indexOf(String.valueOf(issueNumber)) + 1;
            ValueRange body = new ValueRange().setValues(values);
            AppendValuesResponse result =
                    service
                            .spreadsheets()
                            .values()
                            .append(spreadSheetId, "B" + rowNumber, body)
                            .setValueInputOption("RAW")
                            .setInsertDataOption("OVERWRITE")
                            .execute();
            System.out.printf("%d cells updated.", result.getUpdates().getUpdatedCells());
    }
}
}
