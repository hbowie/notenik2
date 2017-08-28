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
	import com.powersurgepub.psutils2.notenik.*;
  import com.powersurgepub.psutils2.prefs.*;
	import com.powersurgepub.psutils2.ui.*;

  import java.io.*;

 	import javafx.event.*;
 	import javafx.scene.control.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;
/**
 Display and save user prefs for syncing with a Notational Velocity style folder.

 @author Herb Bowie
 */
public class FolderSyncPrefs 
    implements 
      PrefSet {
  
  public static final String SYNC_FOLDER = "sync-folder";
  
  private     Notenik           notenik;
  private     Window            ownerWindow;
  
  private FolderSyncPrefsData   folderSyncPrefsData = new FolderSyncPrefsData();
  
  private FileSpec              collection = null;
  
  private     FXUtils           fxUtils;
  private     GridPane          folderSyncPane;
  private     Label             folderLabel;
  private     Label             folderFiller;
  private     Button            folderBrowseButton;
  private     TextField         syncFolderTextField;
  private     Label             collectionLabel;
  private     Label             collectionTextField;
  private     Label             prefixLabel;
  private     TextField         prefixTextField;
  private     Label             syncLabel;
  private     CheckBox          syncCheckBox;
  private     Button            saveButton;
  
  public FolderSyncPrefs(Notenik notenik, Window ownerWindow) {
    this.notenik = notenik;
    this.ownerWindow = ownerWindow;
    buildUI();
    syncFolderTextField.setText(folderSyncPrefsData.getSyncFolder());
  }

  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		folderSyncPane = new GridPane();
		fxUtils.applyStyle(folderSyncPane);

		folderLabel = new Label("Folder:");
		folderSyncPane.add(folderLabel, 0, rowCount, 1, 1);
		folderLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(folderLabel, Priority.SOMETIMES);

		folderFiller = new Label();
		folderSyncPane.add(folderFiller, 1, rowCount, 1, 1);
		folderFiller.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(folderFiller, Priority.ALWAYS);

		folderBrowseButton = new Button("Browse…");
    folderBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Specify Common Folder for Synchronization");
        File chosen = chooser.showDialog(ownerWindow);
        if (chosen != null) {
          setSyncFolder(chosen);
        } // end if user chose a folder
		  } // end handle method
		}); // end event handler
		folderSyncPane.add(folderBrowseButton, 2, rowCount, 1, 1);
		folderBrowseButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(folderBrowseButton, Priority.SOMETIMES);

		rowCount++;

		syncFolderTextField = new TextField();
    syncFolderTextField.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        folderSyncPrefsData.setSyncFolder(syncFolderTextField.getText());
		  } // end handle method
		}); // end event handler
		folderSyncPane.add(syncFolderTextField, 0, rowCount, 3, 1);
		syncFolderTextField.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(syncFolderTextField, Priority.ALWAYS);

		rowCount++;

		collectionLabel = new Label("Collection:");
		folderSyncPane.add(collectionLabel, 0, rowCount, 1, 1);
		collectionLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(collectionLabel, Priority.SOMETIMES);

		rowCount++;

		collectionTextField = new Label();
		folderSyncPane.add(collectionTextField, 0, rowCount, 3, 1);
		collectionTextField.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(collectionTextField, Priority.ALWAYS);

		rowCount++;

		prefixLabel = new Label("File Name Prefix for this Collection:");
		folderSyncPane.add(prefixLabel, 0, rowCount, 3, 1);
		prefixLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(prefixLabel, Priority.SOMETIMES);

		rowCount++;

		prefixTextField = new TextField();
    prefixTextField.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {

		  } // end handle method
		}); // end event handler
		folderSyncPane.add(prefixTextField, 0, rowCount, 3, 1);
		prefixTextField.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(prefixTextField, Priority.ALWAYS);

		rowCount++;

		syncLabel = new Label("Sync this Collection?");
		folderSyncPane.add(syncLabel, 0, rowCount, 1, 1);
		syncLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(syncLabel, Priority.SOMETIMES);

		rowCount++;

		syncCheckBox = new CheckBox("Yes");
    syncCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        collection.setSync (syncCheckBox.isSelected());
		  } // end handle method
		}); // end event handler
		folderSyncPane.add(syncCheckBox, 0, rowCount, 1, 1);
		syncCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(syncCheckBox, Priority.SOMETIMES);

		rowCount++;

		saveButton = new Button("Save");
    saveButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        folderSyncPrefsData.setSyncFolder(syncFolderTextField.getText());
        folderSyncPrefsData.setSync(syncCheckBox.isSelected());
        collection.setSync(syncCheckBox.isSelected());
        folderSyncPrefsData.setSyncPrefix(prefixTextField.getText());
        collection.setSyncPrefix(prefixTextField.getText());
        if ((! folderSyncPrefsData.getSync())
            && syncCheckBox.isSelected()) {
          notenik.syncWithFolder();
          folderSyncPrefsData.setSync(true);
        }
		  } // end handle method
		}); // end event handler
		folderSyncPane.add(saveButton, 0, rowCount, 1, 1);
		saveButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(saveButton, Priority.SOMETIMES);

		rowCount++;
  } // end method buildUI

    public FolderSyncPrefsData getFolderSyncPrefsData() {
    return folderSyncPrefsData;
  }
  
  public boolean getSync() {
    return folderSyncPrefsData.getSync();
  }
  
  public String getSyncFolder() {
    return folderSyncPrefsData.getSyncFolder();
  }
  
  public String getSyncPrefix() {
    return folderSyncPrefsData.getSyncPrefix();
  }
  
  public void savePrefs() {
    folderSyncPrefsData.savePrefs();
  }
  
  public void setFileSpec(FileSpec collection) {
    this.collection = collection;
    folderSyncPrefsData.setCollection(collection);
    if (collection != null) {
      collectionTextField.setText(collection.getPath());
      prefixTextField.setText(folderSyncPrefsData.getSyncPrefix());
      String collectionPrefix = folderSyncPrefsData.getSyncPrefix();
      if (collectionPrefix.length() > 0) {
        prefixTextField.setText(collection.getSyncPrefix());
      }
    } 
    syncCheckBox.setSelected(folderSyncPrefsData.getSync());
  }
  
  private void setSyncFolder (File folder) {
    try {
      String folderString = folder.getCanonicalPath();
      setSyncFolder(folderString);
    } catch (java.io.IOException e) {
      setSyncFolder(folder.toString());
    }
    
  }
  
  private void setSyncFolder (String folderString) {
    syncFolderTextField.setText(folderString);
    folderSyncPrefsData.setSyncFolder(folderString);
  }
  
  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle() {
    return "Folder Sync";
  }
  
  /**
   Get a JavaFX Pane presenting all the preferences in this set to the user. 
  
   @return The JavaFX Pane containing Controls allowing the user to update
           all the preferences in this set. 
  */
  public Pane getPane() {
    return folderSyncPane;
  }
  
  /**
   Save all of these preferences to disk, so that they can be restored
   for the user at a later time. 
  */
  public void save() {
    folderSyncPrefsData.savePrefs();
  }

}
