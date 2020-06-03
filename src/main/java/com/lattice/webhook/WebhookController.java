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
 * 1.Opened - Done - Tested
 * 2.Closed - Done - Tested
 * 3.assigned - Done - Tested
 * 4.unassigned - Done -Tested
 * 5.reopened - Done -Tested
 * 6.Edited - Done -Tested
 * 7.milestoned - Done
 * 8.demilestoned - Done
 * 9.labeled - Done
 * 10.unlabeled - Done
 */
@RestController
public class WebhookController {

  private String spreadSheetId = "1IQ6cMyDz123GuJEUYWysNt73_UGoYxaBG0idq6xoV3U";

  @PostMapping(value = "/webhook")
  public ResponseEntity<String> webhook(@RequestBody JsonNode jsonNode)
      throws IOException, GeneralSecurityException {
    String action = jsonNode.get("action").asText();
    JsonNode issue = jsonNode.get("issue");
    ArrayList<String> issueInfo = new ArrayList<>();
    SheetsQuickstart sheetsQuickstart = new SheetsQuickstart();

    switch (action) {
      case "opened":
        {
          issueInfo.add(issue.get("number").asText());
          issueInfo.add("=HYPERLINK(\" "+ issue.get("html_url").asText() + ",\""
                  + issue.get("title").asText()  +"\")");
          issueInfo.add(issue.get("user").get("login").asText());
          issueInfo.add(issue.get("state").asText());
          sheetsQuickstart.updateValues(
              spreadSheetId, issueInfo);
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
              spreadSheetId, assigneeNames, issueNumber);
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
            String issueTitle = "=HYPERLINK(\" "+ issue.get("html_url").asText() + ",\""
                    + issue.get("title").asText()  +"\")";
            sheetsQuickstart.updateIssueTitle(spreadSheetId, issueNumber, issueTitle);
            return ResponseEntity.ok("Updated Successfully");

        }

      case "milestoned" :{
        int issueNumber = jsonNode.get("issue").get("number").asInt();
          String milestoneTitle =
              "=HYPERLINK(\" "+ issue.get("milestone").get("html_url").asText() + ",\""
                      + issue.get("milestone").get("title").asText()  +"\")"
                  ;
        sheetsQuickstart.updateMilestone(spreadSheetId, issueNumber, milestoneTitle);
        return ResponseEntity.ok("Updated Successfully");
      }

      case "demilestoned" : {

        int issueNumber = jsonNode.get("issue").get("number").asInt();
        sheetsQuickstart.demilestone(spreadSheetId, issueNumber);
        return ResponseEntity.ok("Updated Successfully");

      }

      case "unlabeled" :
      case "labeled" :  {

        ArrayList<String> labelsList = new ArrayList<>();
        int issueNumber = jsonNode.get("issue").get("number").asInt();
        JsonNode labels = jsonNode.get("issue").get("labels");
        labels.forEach(label -> labelsList.add(label.get("name").textValue()));
        sheetsQuickstart.updateLabels(
                spreadSheetId, labelsList, issueNumber);
        return ResponseEntity.ok("Updated Successfully");


      }

    }
    return ResponseEntity.ok("Something went wrong ");
  }
}
