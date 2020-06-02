package com.lattice.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Events: Issue opened, edited, deleted, transferred, pinned, unpinned, closed, reopened, assigned,
 * unassigned, labeled, unlabeled, milestoned, demilestoned, locked, or unlocked.
 * Handled Issues:
 * 1.Opened - Done
 * 2.Closed - Done
 * 3.assigned - Done
 * 4.unassigned - Done TODO: Need to test this case
 * 5.reopened - Done
 * 6.Edited - Done
 */
@RestController
public class WebhookController {

  String spreadSheetId = "1IQ6cMyDz123GuJEUYWysNt73_UGoYxaBG0idq6xoV3U";

  @PostMapping(value = "/webhook")
  public ResponseEntity<String> webhook(@RequestBody JsonNode jsonNode)
      throws IOException, GeneralSecurityException {
    System.out.println("Success");
    String action = jsonNode.get("action").asText();
    JsonNode issue = jsonNode.get("issue");
    ArrayList<String> issueInfo = new ArrayList<>();
    SheetsQuickstart sheetsQuickstart = new SheetsQuickstart();

    switch (action) {
      case "opened":
        {
          issueInfo.add(issue.get("number").asText());
          issueInfo.add(issue.get("title").asText());
          issueInfo.add(issue.get("user").get("login").asText());
          issueInfo.add(issue.get("state").asText());
          issueInfo.add(issue.get("url").asText());
          sheetsQuickstart.updateValues(
              "1IQ6cMyDz123GuJEUYWysNt73_UGoYxaBG0idq6xoV3U", "RAW", issueInfo);
          return ResponseEntity.ok("Updated Successfully");
        }

      case "assigned":

      case "unassigned":
        {
          ArrayList<String> assigneeNames = new ArrayList<>();
          int issueNumber = jsonNode.get("issue").get("number").asInt();
          JsonNode assignees = jsonNode.get("issue").get("assignees");
          assignees.forEach(assignee -> assigneeNames.add(assignee.get("login").textValue()));
          sheetsQuickstart.updateAssignees(
              "1IQ6cMyDz123GuJEUYWysNt73_UGoYxaBG0idq6xoV3U", "RAW", assigneeNames, issueNumber);
          return ResponseEntity.ok("Updated Successfully");
        }

      case "reopened":

      case "closed":
        {
          int issueNumber = jsonNode.get("issue").get("number").asInt();
          String issueStatus = issue.get("state").asText();
          sheetsQuickstart.changeIssueState(spreadSheetId, issueNumber, issueStatus);
          return ResponseEntity.ok("Updated Successfully");
        }

        case "edited"  : {

            int issueNumber = jsonNode.get("issue").get("number").asInt();
            String issueTitle = issue.get("title").asText();
            sheetsQuickstart.updateIssueTitle(spreadSheetId, issueNumber, issueTitle);
            return ResponseEntity.ok("Updated Successfully");

        }
    }
    return ResponseEntity.ok("Something went wrong ");
  }
}
