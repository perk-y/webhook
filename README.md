# webhook

API used:
1. Google Sheets API
2. GitHub Webhooks

**What it does?**

 A webhook sends a event request whenever any changes happen in a any repository. This repository responds to those events and update them in a SpreadSheet using google sheets API.
  
  Handled events(for github issues):
  
  1.Opened
  
  2.Closed 
  
  3.assigned 
  
  4.unassigned
  
  5.reopened 
  
  6.Edited
  
  
  **How to use**
  
 1. [Turn on google sheets API](https://developers.google.com/sheets/api/quickstart/java) and download credentials.json provided.
 2. Place credentials.json in src/main/resources directory.
 3. Change the spreadsheetId field in Webhook controller class to spreadsheet id that you want to update data in.
 4. Finally, execute the Program by using *gradle bootRun*
 
  
  
