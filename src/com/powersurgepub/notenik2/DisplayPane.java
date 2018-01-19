/*
 * Copyright 2003 - 2017 Herb Bowie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powersurgepub.notenik2;

  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.tags.*;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;

  import java.text.*;

  import javafx.scene.layout.*;

/**
 A panel to display a single note.

 @author Herb Bowie
 */
public class DisplayPane {
  
  private DisplayPrefs  displayPrefs;
  
  private String        endSpecialTag = "";
  
  private DateFormat    dateFormat = new SimpleDateFormat ("EEEE MMMM d, yyyy");
  
  private WebPane       webPane;
  
  /** Creates new form DisplayTab */
  public DisplayPane(DisplayPrefs displayPrefs) {
    this.displayPrefs = displayPrefs;
    webPane = new WebPane();
  }
  
  /**
   Get the pane containing the webview. 
  
   @return The pane containing the webview. 
  */
  public Pane getPane() {
    return webPane.getPane();
  }
  
  /**
    Prepares the tab for processing of newly opened file.
   */
  public void filePrep () {
    // No file information used on the About Tab
  }
  
  public void initItems() {

  }
  
  public void startDisplay() {
    webPane.initPage();
    webPane.appendLine ("<html>");
    webPane.appendLine("<head>");
    webPane.appendLine("<style>");
    webPane.appendLine("body { color: #" 
        + StringUtils.colorToHexString(displayPrefs.getDisplayTextColor())
        + "; "
        + " background-color: #"
        + StringUtils.colorToHexString(displayPrefs.getDisplayBackgroundColor())
        + "; "
        + getFontFamily()
        + getFontSize(0)
        + "}");
    webPane.appendLine("</style>");
    webPane.appendLine("</head>");
    webPane.appendLine("<body>");
  }
  
  public void displayTags(Tags tags) {
    String tagsString = "";
    if (tags.hasData()) {
      tagsString = tags.toString();
    }
    appendParagraph ("em", 0, "", "", tagsString);
  }
  
  public void displayTitle(String title) {
    // Display Title in bold and increased size
    appendParagraph ("strong", 1, "", "", title);
  }
  
  public void displayBody(String body) {
    // Display Body, if there is any
    webPane.appendMarkdown(body);
  }
  
  /**
   Display a horizontal rule to serve as a divider
  */
  public void displayDivider() {
    
    webPane.appendLine ("<br /><hr />");
  }
  
  public void displayAuthor(Author author) {
    // Display Author, if any
    String authorCompleteName = author.getCompleteName();
    if (authorCompleteName.length() > 0) {
      int numberOfAuthors = author.getNumberOfAuthors();
      if (author.isCompound()) {
        startParagraph ("", 0, "Authors");
        for (int i = 0; i < numberOfAuthors; i++) {
          Author nextAuthor = author.getAuthor (i);
          appendItem (
              nextAuthor.getALink(), 
              nextAuthor.getCompleteName(),  
              nextAuthor.getWikiquoteLink(),
              "Wikiquote",
              i, 
              numberOfAuthors);
        }
      } else {
        startParagraph ("", 0, "Author");
        appendItem (
            author.getALink(), 
            authorCompleteName,  
            author.getWikiquoteLink(),
            "Wikiquote",
            0, 1);
      }
      String authorInfo = author.getAuthorInfo();
      if (authorInfo.length() > 0) {
        appendItem ("", ", " + authorInfo, 0, 1);
      }
      endParagraph();
    }
  }
  
  /**
   Display source of text, if any. 
  
   @param sourceObj The source of the text.
  */
  public void displaySource(Work sourceObj, String pages) {
    // Display Source, if any

    String sourceType = sourceObj.getTypeLabel();
    String source = sourceObj.toString();
    String url = sourceObj.getALink();
    String minorTitle = sourceObj.getMinorTitle();
    if ((source.length() > 0 
          && (! source.equalsIgnoreCase (Work.UNKNOWN)))
        || (minorTitle.length() > 0)) {

      startParagraph ("", 0, "Source");

      if (sourceType.length() > 0
          && (! sourceType.equalsIgnoreCase(Work.UNKNOWN))) {
        webPane.append ("the " + sourceObj.getTypeLabel().toLowerCase() + ", ");
      }

      if (source.length() > 0 
          && (! source.equalsIgnoreCase (Work.UNKNOWN))) {
        webPane.append("<cite>");
        appendItem (url, source, 0, 1);
        webPane.append("</cite>");
        if (minorTitle.length() > 0) {
          webPane.append(", ");
        }
      }
      if (minorTitle.length() > 0) {
        webPane.append("\"" + minorTitle + "\"");
      }
      if (pages.length() > 0) {
        StringBuilder pagesLabel = new StringBuilder("page");
        if (pages.indexOf("-") > 0) {
          pagesLabel.append("s");
        }
        webPane.append(", " + pagesLabel + " " + pages);
      }

      endParagraph();

      /*
      if (url.length() > 0) {
        appendParagraph ("", 0, url, "Source", source);
      } else {
        appendParagraph ("", 0, "", "Source", source);
      }
      if (td.displayType) {
        appendParagraph ("", 0, "",  "Type", item.getSourceTypeLabel());
      }
      if (item.getPages().length() > 0) {
        appendParagraph ("", 0, "", "Page(s)", item.getPages());
      }
       */
      StringBuilder publisher = new StringBuilder(sourceObj.getCity());
      if (publisher.length() > 0) {
        publisher.append (": ");
      }
      publisher.append (sourceObj.getPublisher());
      if (publisher.length() > 0) {
        appendParagraph ("", 0, "", "Publisher", publisher.toString());
      }

    }

    /*
    if (minorTitle.length() > 0) {
      appendParagraph ("", 0, "", "Minor Title", minorTitle);
    }
     */
    
  }
  
  /**
   Display copyright, or other rights, and related info. 
  
   @param rights The rights owned, such as Copyright. 
   @param year   The year (at a minimum) of first publication of the work.
   @param rightsOwner The name of the person or company owning the rights. 
  */
  public void displayRights(String rights, String year, String rightsOwner) {
    // Display Rights / Publication Year
    String yearRightsLabel = "First Published";
    StringBuilder yearRights = new StringBuilder();

    if (rights.length() > 0
        && rights.startsWith ("Copyright")) {
      yearRights.append ("Copyright &copy;");
    } else {
      yearRights.append (rights);
    }
    
    if (yearRights.length() > 0) {
      yearRightsLabel = "Rights";
    }
    
    if (! year.equals("")) {
      if (yearRights.length() > 0) {
        yearRights.append (" ");
      }
      yearRights.append (year);
    }
    
    if (rightsOwner.length() > 0) {
      if (yearRights.length() > 0) {
        yearRights.append (" by ");
      }
      yearRights.append (rightsOwner);
      yearRightsLabel = "Rights";
    }
    if (yearRights.length() > 0) {
      appendParagraph ("", 0, "", yearRightsLabel, yearRights.toString());
    }
  }
  
  /**
   Display the priority / rating if anything unusual. 
  
   @param priority An integer / priority in the range 1 - 5. 
   @param label    The display value. 
  */
  public void displayRating(int priority, String label) {

    if (priority != 3) {
      appendParagraph ("", 0, "", "Rating", 
          String.valueOf (priority) + " " + label);
    }
  }
  
  public void finishDisplay() {
    webPane.appendLine ("</body>");
    webPane.appendLine ("</html>");
    webPane.loadPage();
  }
  
  public void displayDateAdded(String dateAdded) {
    // Display Date Added

    appendParagraph ("", 0, "", "Added", dateAdded);

  }
  
  /**
   Display Item ID.
  
   @param id A number uniquely identifying the item being displayed. 
  */
  public void displayItemID (int id) {

    appendParagraph ("", 0,
        // item.getItemIDLink(td.collectionWindow),
        "", "ID",
        String.valueOf (id));
  }
  
  public void displayField(String label, String value) {
    if (value != null && value.length() > 0) {
      appendParagraph("", 0, "", label, value);
    }
  }
  
  public void displayCode(String label, String value) {
     appendParagraph("", 0, "", label, "");
     webPane.append("<pre><code>");
     webPane.append(value);
     webPane.append("</code></pre>");
  }
  
  public void displayLink(WebLauncher launcher, String label, String value, String link) {
    String val = value;
    if (val.length() == 0) {
      if (link.startsWith("http://")) {
        val = link.substring(7);
      }
      else
      if (link.startsWith("https://")) {
        val = link.substring(8);
      }
      else
      if (link.startsWith("mailto:")) {
        val = link.substring(7);
      } else {
        val = link;
      }
    }
    appendParagraph ("", 0, link, label, val);
    if (launcher == null) {
      webPane.setLaunchLink(link);
    } else {
      webPane.setLauncher(link, "Open Collection", launcher);
    }
  }
  
  public void displayLabelOnly(
      String label) {
    
    startParagraph("", 0, label);
    endParagraph();
  }
  
  public void appendParagraph (
      String specialTag, 
      int fontVariance, 
      String href,
      String label, 
      String body) {
    
    startParagraph (specialTag, fontVariance, label);

    if ((href != null && href.length() > 0)
        || (body != null && body.length() > 0)) {
      appendItem (href, body, 0, 1);
    }
    
    endParagraph();
  }
  
  public void startParagraph (
      String specialTag, 
      int fontVariance, 
      String label) {

    String startSpecialTag = "";
    endSpecialTag = "";
    if (specialTag.length() > 0) {
      startSpecialTag = "<" + specialTag + ">";
      endSpecialTag = "</" + specialTag + ">";
    }
    String intro = "";
    if (label.length() > 0) {
      intro = label + ": ";
    }
    webPane.append ("<p>");
    // startFont(fontVariance);
    webPane.append (
        startSpecialTag +
        intro);
  }

  public void startFont (int fontVariance) {
    webPane.append (
        "<span style=\" " +
        getFontFamily() +
        getFontSize(fontVariance) +
        "\">");
  }
  
  /**
   Get the user's preferred font-family, formatted as a CSS rule. 
  
   @return CSS for user's preferred font-family. 
  */
  public String getFontFamily() {
    return 
        "font-family: '" + 
        displayPrefs.getDisplayFont() + 
        "', Verdana, Arial, sans-serif; ";
  }
  
  /**
   Get the user's preferred font-size, formatted as a CSS rule. 
  
   @param fontVariance Amount to add to the normal size. 
  
   @return CSS for user's preferred font-size. 
  */
  public String getFontSize(int fontVariance) {
    int fontSize = displayPrefs.getDisplayNormalFontSize() + fontVariance;
    return 
        "font-size: " +
        String.valueOf (fontSize) +
        "; ";
  }

  /**
   Append another item to a list of items, and optionally specify a link. 
  
   @param href An optional link. 
   @param body The text of the item. 
   @param listPosition This item's position in the list. 
   @param listLength The total length of the list. 
  */
  public void appendItem (
      String href, 
      String body,
      int listPosition,
      int listLength) {
    
    appendItem (href, body, "", "", listPosition, listLength);
  }
  
  /**
   Append another item to a list of items, and optionally specify a link and a
   parenthetical expression following the item. 
  
   @param href An optional link. 
   @param body The text of the item. 
   @param parenHref An optional link for a parenthetical expression. 
   @param parenthetical An optional parenthetical expression. 
   @param listPosition This item's position in the list. 
   @param listLength The total length of the list. 
  */
  public void appendItem (
      String href, 
      String body,
      String parenHref,
      String parenthetical,
      int listPosition,
      int listLength) {
    
    String startLink = "";
    String endLink = ""; 
    String listSuffix = "";
    if (href != null && href.length() > 0) {
      startLink = "<a href=\"" + href + "\">";
      endLink = "</a>";
    }
    if (listLength > 1) {
      int listEndProximity = listLength - listPosition - 1;
      if (listEndProximity == 1) {
        listSuffix = " and ";
      }
      else
      if (listEndProximity > 1) {
        listSuffix = ", ";
      }
    }
    String parenPrefix = "";
    String parenSuffix = "";
    String parenStartLink = "";
    String parenEndLink = "";
    if (parenthetical.length() > 0) {
      parenPrefix = " (";
      parenSuffix = ")";
      if (parenHref.length() > 0) {
        parenStartLink = "<a href=\"" + parenHref + "\">";
        parenEndLink = "</a>";
      }
    }
    webPane.append (
        startLink +
        body +
        endLink +
        parenPrefix +
        parenStartLink +
        parenthetical +
        parenEndLink +
        parenSuffix +
        listSuffix);
  }
  
  public void endParagraph () {
    webPane.append (endSpecialTag);
    webPane.appendLine ("</p>");
  }

  public void endFont () {
    webPane.append ("</span>");
  }
  
  /**
   Modifies the td.item if anything on the screen changed. 
   
   @return True if any of the data changed on this tab. 
   */
  public boolean modIfChanged () {
    return false;
  } // end method

}
