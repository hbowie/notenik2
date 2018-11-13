/*
 * Copyright 2018 - 2018 Herb Bowie
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

  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.notenik.*;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;
  import com.powersurgepub.psutils2.widgets.*;

  import java.io.*;
  import java.util.*;

  import javafx.event.ActionEvent;
  import javafx.event.EventHandler;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.scene.text.TextAlignment;
  import javafx.stage.Modality;
  import javafx.stage.Stage;
  import javafx.stage.StageStyle;

  import javax.swing.*;

/**
 A user interface to help the user manage lists of notes.

 @author Herb Bowie
 */
public class ListWrangler
    implements
      DateWidgetOwner,
      WindowToManage {

  public static final String LISTS_FOLDER                 = "lists";

  private     File                listsFolder = null;
  private     boolean             folderOK    = false;

  private     Stage               mainStage;
  private     Notenik             notenik;
  private     NoteCollectionModel model;

  private     FXUtils             fxUtils;
  private     Stage               addListStage;
  private     Scene               addListScene;
  private     GridPane            addListPane;

  private     Label               listsLabel = new Label("Select a list: ");
  private     ChoiceBox<String>   lists = new ChoiceBox<>();

  private     Label               titleLabel = new Label("Add to front of each Title: ");
  private     TextField           titleField = new TextField();

  private     Label               seqLabel = new Label("Add to front of each Seq: ");
  private     TextField           seqField = new TextField();

  private     Label               dateLabel = new Label("Starting Date: ");
  private     DateWidget          dateField = new DateWidget();

  private     Label               tagsLabel = new Label("Add to back of each Tag: ");
  private     TextField           tagsField = new TextField();

  private     Button              cancelButton  = new Button("Cancel");
  private     Button              addListButton = new Button("Add");


  /**
   * Create the wrangler.
   */
  public ListWrangler(Notenik notenik) {
    this.notenik = notenik;
    buildUI();
  }


  /**
   Build the user interface
   */
  private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

    addListStage = new Stage(StageStyle.UTILITY);
    addListStage.setTitle(getTitle());
    addListStage.initModality(Modality.APPLICATION_MODAL);

    addListPane = new GridPane();
    fxUtils.applyStyle(addListPane);

    addListPane.add(listsLabel, 0, rowCount, 1, 1);
    addListPane.add(lists, 1, rowCount, 1, 1);
    lists.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(lists, Priority.ALWAYS);

    rowCount++;

    addListPane.add(titleLabel, 0, rowCount, 1, 1);
    addListPane.add(titleField, 1, rowCount, 1, 1);

    rowCount++;

    addListPane.add(seqLabel, 0, rowCount, 1, 1);
    addListPane.add(seqField, 1, rowCount, 1, 1);

    rowCount++;

    dateField.setOwner(this);
    dateField.setStage(addListStage);
    addListPane.add(dateLabel, 0, rowCount, 1, 1);
    addListPane.add(dateField, 1, rowCount, 1, 1);

    rowCount++;

    addListPane.add(tagsLabel, 0, rowCount, 1, 1);
    addListPane.add(tagsField, 1, rowCount, 1, 1);

    rowCount++;

    cancelButton.setOnAction(e -> cancelAdd());
    addListButton.setOnAction(e -> addNow());
    addListPane.add(cancelButton, 0, rowCount, 1, 1);
    addListPane.add(addListButton, 1, rowCount, 1, 1);

    rowCount++;

    addListScene = new Scene(addListPane);
    addListStage.setScene(addListScene);

  } // end method buildUI

  /**
   * Set the latest notes folder.
   *
   * @param notesFolder The folder containing notes.
   *
   * @return True if we found a good lists folder, false otherwise.
   */
  public boolean setNotesFolder(File notesFolder, Menu openListMenu) {

    folderOK = false;

    if (notesFolder == null) {
      listsFolder = null;
    } else {
      listsFolder = new File(notesFolder, LISTS_FOLDER);
    }

    while (lists.getItems().size() > 0) {
      lists.getItems().remove(0);
    }

    while (openListMenu.getItems().size() > 0) {
      openListMenu.getItems().remove(0);
    }

    if (listsFolder != null &&
        listsFolder.exists() &&
        listsFolder.isDirectory() &&
        listsFolder.canRead()) {
      String[] fileNames = listsFolder.list();
      for (String fileName : fileNames){
        File candidate = new File (listsFolder, fileName);
        if (candidate.isDirectory() && candidate.canRead()) {
          lists.getItems().add(fileName);
          MenuItem newListItem = new MenuItem(fileName);
          newListItem.setOnAction(e -> openList(e));
          openListMenu.getItems().add(newListItem);
          folderOK = true;
        }
      } // end for each file in the folder
    }

    return folderOK;
  }

  private void openList(ActionEvent event) {
    MenuItem source = (MenuItem)event.getSource();
    MenuItem target = (MenuItem)event.getTarget();
    notenik.openList(source.getText());
  }

  /**
   * Show the user a window that will allow him to add a list of notes to the current collection.
   *
   * @param mainStage The main stage for the application.
   *
   * @return True if we had a good lists folder, false otherwise.
   */
  public boolean addList(Stage mainStage, NoteCollectionModel model) {

    this.mainStage = mainStage;
    this.model = model;
    if (listsFolder != null && folderOK) {
      setVisible(true);
    } else {
      noListsFolder(mainStage);
    }
    return folderOK;
  }

  /**
   * Cancel the list add.
   */
  private void cancelAdd() {
    setVisible(false);
  }

  /**
   * Add a list now.
   */
  private void addNow() {
    String selectedList = lists.getSelectionModel().getSelectedItem();
    if (selectedList == null || selectedList.length() == 0) {
      PSOptionPane.showMessageDialog(addListStage,
          "Please select a list to be added",
          "List Selection Problem",
          JOptionPane.WARNING_MESSAGE);
    } else {
      File listFolder = new File(listsFolder, selectedList);
      notenik.preImport();
      NoteIO listIO = new NoteIO (
          listFolder,
          NoteParms.DEFINED_TYPE,
          model.getRecDef());
      String[] fileNames = listFolder.list();
      for (String fileName : fileNames){
        File candidate = new File (listFolder, fileName);
        if (NoteIO.isInterestedIn(candidate)) {
          try {
            Note listNote = listIO.getNote(candidate, "");

            String titlePrefix = titleField.getText();
            if (titlePrefix != null && titlePrefix.length() > 0) {
              listNote.setTitle(titlePrefix + " " + listNote.getTitle());
            }

            String seqPrefix = seqField.getText();
            if (seqPrefix != null && seqPrefix.length() > 0 && listNote.hasSeq()) {
              String oldSeq = listNote.getSeq();
              String newSeq;
              if (oldSeq.length() > 2 && oldSeq.startsWith("a.")) {
                newSeq = seqPrefix + oldSeq.substring(1);
              } else {
                newSeq = seqPrefix + oldSeq;
              }
              listNote.setSeq(newSeq);
            }

            if (listNote.hasDate()) {
              String dateIncStr = listNote.getDate().toString();
              try {
                int dateInc = Integer.parseInt(dateIncStr);
                Date startDate = dateField.getDate();
                Calendar calDate = Calendar.getInstance();
                calDate.setTime(startDate);
                calDate.add(Calendar.DATE, dateInc);
                listNote.setDate(calDate.getTime());
              } catch (NumberFormatException e) {
                // do nothing if bad integer
              }
            }

            if (listNote.hasTags() && tagsField.getText().length() > 0) {
              String newTags = listNote.getTagsAsString() + "." + tagsField.getText();
              listNote.setTags(newTags);
            }

            model.add(listNote);

          } catch (IOException e) {
            PSOptionPane.showMessageDialog(addListStage,
                "I/O Error reading " + candidate.toString(),
                "Note I/O Error",
                javax.swing.JOptionPane.WARNING_MESSAGE);
          }
        } // end if this looks like a note
      } // end for each file in the folder
      notenik.postImport();
    } // End if we have a selected list
    setVisible(false);
  }

  private void noListsFolder(Stage mainStage) {
    PSOptionPane.showMessageDialog(mainStage,
        "No usable lists folder found",
        "No Lists",
        javax.swing.JOptionPane.WARNING_MESSAGE);
  }

  /**
   Get the title for pane.

   @return The title for this pane.
   */
  public String getTitle() {
    return "Add a List";
  }

  /**
   * Either show or hide the add list window.
   *
   * @param visible True to show, false to hide.
   */
  public void setVisible (boolean visible) {
    if (visible) {
      addListStage.show();
    } else {
      addListStage.close();
    }
  }

  /**
   * Bring the Add List window to the front.
   */
  public void toFront() {
    addListStage.show();
    addListStage.toFront();
  }

  /**
   * Return the width of the Add List window.
   *
   * @return The width of the window.
   */
  public double getWidth() {
    return addListStage.getWidth();

  }

  /**
   * Return the height of the Add List window.
   *
   * @return The height of the window.
   */
  public double getHeight() {
    return addListStage.getHeight();
  }

  /**
   * Set the location of the upper left corner of the Add List window.
   *
   * @param x The horizontal position from the left side.
   * @param y The vertical position from the top.
   */
  public void setLocation(double x, double y) {
    addListStage.setX(x);
    addListStage.setY(y);
  }

  /**
   To be called whenever the date is modified by DateWidget.
   */
  public void dateModified (String date) {
    // No need to do anything
  }

  /**
   Does this date have an associated rule for recurrence?
   */
  public boolean canRecur() {
    return false;
  }

  /**
   Provide a text string describing the recurrence rule, that can
   be used as a tool tip.
   */
  public String getRecurrenceRule() {
    return "";
  }

  /**
   Apply the recurrence rule to the date.

   @param date Starting date.

   @return New date.
   */
  public String recur (StringDate date) {
    return "";
  }

  /**
   Apply the recurrence rule to the date.

   @param date Starting date.

   @return New date.
   */
  public String recur (String date) {
    return dateField.getText();
  }

  public boolean newList(Stage mainStage, Notenik notenik, NoteCollectionModel model) {
    this.mainStage = mainStage;
    this.notenik = notenik;
    this.model = model;
    boolean newListOK = false;
    if (listsFolder != null) {
      if (! listsFolder.exists()) {
        listsFolder.mkdir();
      }
      if (listsFolder.exists() &&
          listsFolder.isDirectory() &&
          listsFolder.canRead()) {
        TextInputDialog listNameDialog = new TextInputDialog("");
        listNameDialog.setTitle("Create a New List");
        listNameDialog.setHeaderText(null);
        listNameDialog.setContentText("Enter Name of New List: ");
        listNameDialog.getEditor().setPrefColumnCount(40);
        Optional<String> listName = listNameDialog.showAndWait();
        if (listName.isPresent()) {
          File newListFolder = new File(listsFolder, listName.get());
          if (! newListFolder.exists()) {
            newListOK = newListFolder.mkdir();
            if (newListOK) {
              File templateFile = model.getTemplateFile();
              if (templateFile != null) {
                String templateFileName = templateFile.getName();
                File newTemplateFile = new File(newListFolder, templateFileName);
                FileUtils.copyFile(templateFile, newTemplateFile);
                NoteIO listIO = new NoteIO (
                    newListFolder,
                    NoteParms.DEFINED_TYPE,
                    model.getRecDef());
                Note firstNote = new Note(listIO.getRecDef());
                firstNote.setTitle("Starter Note");
                try {
                  listIO.save(firstNote, true);
                } catch (IOException e) {
                  newListOK = false;
                }
              }
            }
          }
        }
      }
      return newListOK;
    }

    if (listsFolder != null && folderOK) {
      setVisible(true);
    } else {
      noListsFolder(mainStage);
    }
    return folderOK;
  }
}
