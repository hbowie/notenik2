/*
 * Copyright 2014 - 2017 Herb Bowie
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
  import com.powersurgepub.psutils2.prefs.*;
  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;

  import javafx.event.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
 Display and save user prefs for saving HTML versions of notes to a separate
 folder. 

 @author Herb Bowie
 */
public class HTMLPrefs 
    implements 
      PrefSet {
  
  private     Notenik           notenik;
  private     Window            ownerWindow;
  
  private FileSpec              collection = null;
  
  private boolean syncSetting = false;
  
  private     FXUtils             fxUtils;
  private     GridPane htmlGenPane;
  private     Label folderLabel;
  private     Label folderFiller;
  private     Button folderBrowseButton;
  private     TextField htmlFolderTextField;
  private     Button saveButton;
  
  public HTMLPrefs(Notenik notenik, Window ownerWindow) {
    this.notenik = notenik;
    this.ownerWindow = ownerWindow;
    buildUI();
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		htmlGenPane = new GridPane();
		fxUtils.applyStyle(htmlGenPane);

		folderLabel = new Label("Folder:");
		htmlGenPane.add(folderLabel, 0, rowCount, 1, 1);
		folderLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(folderLabel, Priority.SOMETIMES);

		folderFiller = new Label();
		htmlGenPane.add(folderFiller, 1, rowCount, 1, 1);
		folderFiller.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(folderFiller, Priority.ALWAYS);

		folderBrowseButton = new Button("Browse…");
    folderBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Specify Folder to Use For Generated HTML");
        chooser.setInitialDirectory(collection.getFile().getParentFile());
        File chosen = chooser.showDialog(ownerWindow);
        if (chosen != null) {
          setHTMLFolder(chosen);
        } // end if user chose a folder
		  } // end handle method
		}); // end event handler
		htmlGenPane.add(folderBrowseButton, 2, rowCount, 1, 1);
		folderBrowseButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(folderBrowseButton, Priority.SOMETIMES);

		rowCount++;

		htmlFolderTextField = new TextField();
    htmlFolderTextField.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        savePrefs();
		  } // end handle method
		}); // end event handler
		htmlGenPane.add(htmlFolderTextField, 0, rowCount, 3, 1);
		htmlFolderTextField.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(htmlFolderTextField, Priority.ALWAYS);

		rowCount++;

		saveButton = new Button("Save");
    saveButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        savePrefs();
		  } // end handle method
		}); // end event handler
		htmlGenPane.add(saveButton, 0, rowCount, 1, 1);
		saveButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(saveButton, Priority.SOMETIMES);

		rowCount++;
  } // end method buildUI

  public void setCollection(FileSpec collection) {
    this.collection = collection;
    htmlFolderTextField.setText("");
    if (collection != null) {
      htmlFolderTextField.setText(collection.getHTMLFolder());
    } // end if collection not null
  }
  
  private void setHTMLFolder (File folder) {
    try {
      String folderString = folder.getCanonicalPath();
      setHTMLFolder(folderString);
    } catch (java.io.IOException e) {
      setHTMLFolder(folder.toString());
    }
  }
  
  private void setHTMLFolder (String folderString) {
    htmlFolderTextField.setText(folderString);
    savePrefs();
  }
  
  public String getHTMLFolder() {
    return htmlFolderTextField.getText();
  }
  
  public void savePrefs() {
    File htmlFolder = new File(htmlFolderTextField.getText());
    if (htmlFolderTextField.getText().trim().length() == 0
        || (htmlFolder.exists() 
          && htmlFolder.canRead()
          && htmlFolder.canWrite())) {
      collection.setHTMLFolder(htmlFolderTextField.getText());
    } 
  }
  
  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle() {
    return "HTML Gen";
  }
  
  /**
   Get a JavaFX Pane presenting all the preferences in this set to the user. 
  
   @return The JavaFX Pane containing Controls allowing the user to update
           all the preferences in this set. 
  */
  public Pane getPane() {
    return htmlGenPane;
  }
  
  /**
   Save all of these preferences to disk, so that they can be restored
   for the user at a later time. 
  */
  public void save() {
    savePrefs();
  }

}
