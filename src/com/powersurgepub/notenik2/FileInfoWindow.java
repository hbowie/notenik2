/*
 * Copyright 2015 - 2015 Herb Bowie
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

	import com.powersurgepub.psutils2.basic.*;
	import com.powersurgepub.psutils2.clubplanner.*;
	import com.powersurgepub.psutils2.elements.*;
	import com.powersurgepub.psutils2.env.*;
	import com.powersurgepub.psutils2.excel.*;
	import com.powersurgepub.psutils2.files.*;
	import com.powersurgepub.psutils2.index.*;
	import com.powersurgepub.psutils2.links.*;
	import com.powersurgepub.psutils2.list.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.markup.*;
	import com.powersurgepub.psutils2.mkdown.*;
	import com.powersurgepub.psutils2.notenik.*;
	import com.powersurgepub.psutils2.prefs.*;
	import com.powersurgepub.psutils2.publish.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.script.*;
	import com.powersurgepub.psutils2.strings.*;
	import com.powersurgepub.psutils2.strtext.*;
	import com.powersurgepub.psutils2.tabdelim.*;
	import com.powersurgepub.psutils2.tags.*;
	import com.powersurgepub.psutils2.textio.*;
	import com.powersurgepub.psutils2.textmerge.*;
	import com.powersurgepub.psutils2.txbio.*;
	import com.powersurgepub.psutils2.txbmodel.*;
	import com.powersurgepub.psutils2.txmin.*;
	import com.powersurgepub.psutils2.ui.*;
	import com.powersurgepub.psutils2.values.*;
	import com.powersurgepub.psutils2.widgets.*;

  import java.io.*;

 	import javafx.application.*;
 	import javafx.beans.value.*;
 	import javafx.collections.*;
 	import javafx.event.*;
 	import javafx.geometry.*;
 	import javafx.scene.*;
 	import javafx.scene.control.*;
 	import javafx.scene.control.Alert.*;
	import javafx.scene.image.*;
	import javafx.scene.input.*;
 	import javafx.scene.layout.*;
	import javafx.scene.text.*;
	import javafx.scene.web.*;
 	import javafx.stage.*;

/**
  Display info about a file. 

  @author Herb Bowie
 */
public class FileInfoWindow 
    implements 
        WindowToManage {
  
  public static final String      WINDOW_TITLE = "File Info";
  
  private     FXUtils             fxUtils;
  private     Stage               fileInfoStage;
  private     Scene               fileInfoScene;
  private     GridPane            fileInfoPane;
  private     Label               folderLabel;
  private     Label               folderText;
  private     Label               folderExistsLabel;
  private     Label               fileLabel;
  private     Label               fileText;
  private     Label               fileExistsLabel;
  private     TableView           filesTable;
  private     Button              selectFileButton;
  private     Button              browseForFileButton;
  
  private Notenik notenik;
  
  private File folder = null;
  private File file = null;
  
  private FileTable files = new FileTable();
  
  public FileInfoWindow(Notenik notenik) {
    this.notenik = notenik;
    buildUI();
  }
  
    /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;
    
    fileInfoStage = new Stage(StageStyle.UTILITY);
    fileInfoStage.setTitle(WINDOW_TITLE);

		fileInfoPane = new GridPane();
		fxUtils.applyStyle(fileInfoPane);

		folderLabel = new Label("Folder:");
		fileInfoPane.add(folderLabel, 0, rowCount, 1, 1);

		rowCount++;

		folderText = new Label();
		fileInfoPane.add(folderText, 0, rowCount, 1, 1);

		rowCount++;

		folderExistsLabel = new Label("Folder exists.");
		fileInfoPane.add(folderExistsLabel, 0, rowCount, 1, 1);

		rowCount++;

		fileLabel = new Label("File:");
		fileInfoPane.add(fileLabel, 0, rowCount, 1, 1);

		rowCount++;

		fileText = new Label();
		fileInfoPane.add(fileText, 0, rowCount, 1, 1);

		rowCount++;

		fileExistsLabel = new Label("File exists.");
		fileInfoPane.add(fileExistsLabel, 0, rowCount, 1, 1);

		rowCount++;

		filesTable = new TableView();
		fileInfoPane.add(filesTable, 0, rowCount, 1, 1);
		filesTable.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(filesTable, Priority.ALWAYS);

		rowCount++;

		selectFileButton = new Button("Replace Link with Selection");
    selectFileButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        ObservableList<FileBean> rows 
            = filesTable.getSelectionModel().getSelectedItems();
        if (rows.size() > 0) {
          notenik.setLink(rows.get(0).getFile());
          setVisible(false);
        }
		  } // end handle method
		}); // end event handler
		fileInfoPane.add(selectFileButton, 0, rowCount, 1, 1);

		rowCount++;

		browseForFileButton = new Button("Browse for File…");
    browseForFileButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        FileChooser chooser = new FileChooser();
        if (folder != null && folder.exists()) {
          chooser.setInitialDirectory(folder);
        }
        File newFile = chooser.showOpenDialog(fileInfoStage);
        if (newFile != null) {
          notenik.setLink(newFile);
          setVisible(false);
        }
		  } // end handle method
		}); // end event handler
		fileInfoPane.add(browseForFileButton, 0, rowCount, 1, 1);

		rowCount++;
    
    fileInfoScene = new Scene(fileInfoPane);
    fileInfoStage.setScene(fileInfoScene);
    fileInfoStage.setMinWidth(720);
    fileInfoStage.setMinHeight(240);
  } // end method buildUI

  public void setFile(String fileStr) {

    folder = null;
    file = null;
    files = new FileTable();
    FileName fileName = new FileName(fileStr);
    folderText.setText(fileName.getPath());
    folder = new File (fileName.getPath());
    boolean folderExists = folder.exists();
    if (folderExists) {
      folderExistsLabel.setText("Folder exists.");
    } else {
      folderExistsLabel.setText("Folder does not exist.");
    }
    fileText.setText(fileName.getFileName());
    fileExistsLabel.setText("File does not exist");
    if (folderExists) {
      file = fileName.getFile();
      if (file.exists()) {
        fileExistsLabel.setText("File exists.");
      }
      files.add(file);
      String fileNameStr = file.getName();
      int minMatch = fileNameStr.length() - 15;
      if (minMatch > 5) {
        String[] dirEntries = folder.list();
        for (int i = 0; i < dirEntries.length; i++) {
          String dirEntry = dirEntries[i];
          if (! dirEntry.equals(fileNameStr)) {
            int j = 0;
            while (j < minMatch
                && j < dirEntry.length()
                && j < fileNameStr.length()
                && fileNameStr.charAt(j) == dirEntry.charAt(j)) {
              j++;
            }
            if (j >= minMatch) {
              File similarFile = new File(folder, dirEntry);
              files.add(similarFile);
            }
          } // end if not the same file
        } // end for each dir entry
      } // end if we have enough of a file name to match
    } // end if folder exists
    filesTable = files.getTable();
    filesTable.getSelectionModel().clearSelection();
  } // end method setFile
  
  public String getTitle() {
    return WINDOW_TITLE;
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      fileInfoStage.show();
    } else {
      fileInfoStage.close();
    }
  }
  
  public void toFront() {
    fileInfoStage.show();
    fileInfoStage.toFront();
  }
  
  public double getWidth() {
    return fileInfoStage.getWidth();
    
  }
  
  public double getHeight() {
    return fileInfoStage.getHeight();
  }
  
  public void setLocation(double x, double y) {
    fileInfoStage.setX(x);
    fileInfoStage.setY(y);
  }
  
}
