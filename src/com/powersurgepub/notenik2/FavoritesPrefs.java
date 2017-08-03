/*
 * Copyright 2009 - 2017 Herb Bowie
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

  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.prefs.*;
	import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.widgets.*;

  import java.io.*;

  import javafx.beans.value.*;
 	import javafx.event.*;
  import javafx.scene.control.*;
  import javafx.scene.control.Alert.*;
 	import javafx.scene.layout.*;
  import javafx.scene.text.*;

/**
 Preferences for publishing a web page of favorite links. 

 @author Herb Bowie
 */
public class FavoritesPrefs 
    implements
      PrefSet {
  
  // Prefs explicitly set by the user in this window
  public static final String LIST_TAB_SELECTED            = "list-tab-sel";
  public static final String OPEN_STARTUP_AT_LAUNCH       = "open-startup";
  public static final String FAVORITES_TAGS               = "favorites-tags";
  public static final String FAVORITES_COLUMNS            = "favorites-columns";
  public static final String FAVORITES_ROWS               = "favorites-rows";
  public static final String FAVORITES_HOME               = "favorites-home";

  // Other prefs used by URL Union
  public static final String PREFS_LEFT    = "left";
  public static final String PREFS_TOP     = "top";
  public static final String PREFS_WIDTH   = "width";
  public static final String PREFS_HEIGHT  = "height";
  public static final String LAST_FILE     = "last-file";
  public static final String BACKUP_FOLDER = "backup-folder";

  private ProgramVersion    programVersion = ProgramVersion.getShared();
  
  private     FXUtils             fxUtils;
  private     GridPane favoritesPanel;
  private     Label openStartupLabel;
  private     CheckBox openStartupCheckBox;
  private     Label favoritesTagsLabel;
  private     TextField favoritesTagsText;
  private     Label favoritesColumnCountLabel;
  private     Slider favoritesColumnCountSlider;
  private     Label favoriteRowCountLabel;
  private     Slider favoritesRowCountSlider;
  private     Label favoritesHomeLinkLabel;
  private     TextField favoritesHomeLinkText;

  private boolean           setupComplete = false;
  
  public FavoritesPrefs() {
    buildUI();
    String lastFileString
        = UserPrefs.getShared().getPref (LAST_FILE, "");
    File lastFile;
    String defaultFilePath = "";
    if (lastFileString.length() > 0) {
      lastFile = new File (lastFileString);
      if (lastFile.exists()
          && lastFile.isFile()
          && lastFile.canRead()) {
        File parent = lastFile.getParentFile();
        defaultFilePath = parent.getPath();
      }
    }
    
    openStartupCheckBox.setSelected
        (UserPrefs.getShared().getPrefAsBoolean(OPEN_STARTUP_AT_LAUNCH, true));
    
    favoritesTagsText.setText
        (UserPrefs.getShared().getPref(FAVORITES_TAGS, "Favorites"));
    
    favoritesHomeLinkText.setText 
        (UserPrefs.getShared().getPref(FAVORITES_HOME, "../index.html"));

    favoritesColumnCountSlider.setValue
        (UserPrefs.getShared().getPrefAsInt(FAVORITES_COLUMNS, 4));

    favoritesRowCountSlider.setValue
        (UserPrefs.getShared().getPrefAsInt(FAVORITES_ROWS, 30));

    setupComplete = true;
  }  
  
  private void warnRelaunch() {
    Alert alert = new Alert(AlertType.WARNING);
    alert.setTitle("Relaunch Warning");
    alert.setContentText("You may need to Quit and relaunch "
            + Home.getShared().getProgramName()
            + " for your preferences to take effect.");
    alert.showAndWait();
  }
  
  public boolean isOpenStartup() {
    return openStartupCheckBox.isSelected();
  }
  
  public String getFavoritesTags() {
    return favoritesTagsText.getText();
  }
  
  public String getFavoritesHome() {
    return favoritesHomeLinkText.getText();
  }

  public int getFavoritesColumns () {
    return (int)favoritesColumnCountSlider.getValue();
  }

  public int getFavoritesRows () {
    return (int)favoritesRowCountSlider.getValue();
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		favoritesPanel = new GridPane();
		fxUtils.applyStyle(favoritesPanel);

		openStartupLabel = new Label("Open Startup Tags?");
		openStartupLabel.setTextAlignment(TextAlignment.RIGHT);
		favoritesPanel.add(openStartupLabel, 0, rowCount, 1, 1);
		openStartupLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(openStartupLabel, Priority.SOMETIMES);

		openStartupCheckBox = new CheckBox("Open Startup Tags at Program Launch?");
    openStartupCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        UserPrefs.getShared().setPref
          (OPEN_STARTUP_AT_LAUNCH, openStartupCheckBox.isSelected());
		  } // end handle method
		}); // end event handler
		favoritesPanel.add(openStartupCheckBox, 1, rowCount, 1, 1);
		openStartupCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(openStartupCheckBox, Priority.SOMETIMES);

		rowCount++;

		favoritesTagsLabel = new Label("Favorites Tags:");
		favoritesTagsLabel.setTextAlignment(TextAlignment.RIGHT);
		favoritesPanel.add(favoritesTagsLabel, 0, rowCount, 1, 1);
		favoritesTagsLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(favoritesTagsLabel, Priority.SOMETIMES);

		favoritesTagsText = new TextField();
    favoritesTagsText.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        UserPrefs.getShared().setPref
          (FAVORITES_TAGS, favoritesTagsText.getText());
		  } // end handle method
		}); // end event handler
		favoritesPanel.add(favoritesTagsText, 1, rowCount, 1, 1);
		favoritesTagsText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(favoritesTagsText, Priority.ALWAYS);

		rowCount++;

		favoritesHomeLinkLabel = new Label("Home Link:");
		favoritesHomeLinkLabel.setTextAlignment(TextAlignment.RIGHT);
		favoritesPanel.add(favoritesHomeLinkLabel, 0, rowCount, 1, 1);
		favoritesHomeLinkLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(favoritesHomeLinkLabel, Priority.SOMETIMES);

		favoritesHomeLinkText = new TextField("../index.html");
    favoritesHomeLinkText.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        UserPrefs.getShared().setPref
          (FAVORITES_HOME, favoritesHomeLinkText.getText());
		  } // end handle method
		}); // end event handler
		favoritesPanel.add(favoritesHomeLinkText, 1, rowCount, 1, 1);
		favoritesHomeLinkText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(favoritesHomeLinkText, Priority.ALWAYS);

		rowCount++;

		favoritesColumnCountLabel = new Label("Favorites Columns:");
		favoritesColumnCountLabel.setTextAlignment(TextAlignment.RIGHT);
		favoritesPanel.add(favoritesColumnCountLabel, 0, rowCount, 1, 1);
		favoritesColumnCountLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(favoritesColumnCountLabel, Priority.SOMETIMES);

		favoritesColumnCountSlider = new Slider(1, 6, 4);
    Tooltip columnsTip = new Tooltip
        ("Set the number of columns to appear on the favorites page");
    Tooltip.install(favoritesColumnCountSlider, columnsTip);
    favoritesColumnCountSlider.setShowTickLabels(true);
    favoritesColumnCountSlider.setShowTickMarks(true);
    favoritesColumnCountSlider.setMajorTickUnit(1);
    // favoritesColumnCountSlider.setMinorTickCount(3);
    favoritesColumnCountSlider.setBlockIncrement(1);
    favoritesColumnCountSlider.setSnapToTicks(true);
    favoritesColumnCountSlider.valueProperty().addListener(this::columnCountChanged);
		favoritesPanel.add(favoritesColumnCountSlider, 1, rowCount, 1, 1);
		favoritesColumnCountSlider.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(favoritesColumnCountSlider, Priority.ALWAYS);

		rowCount++;

		favoriteRowCountLabel = new Label("Favorites Rows:");
		favoriteRowCountLabel.setTextAlignment(TextAlignment.RIGHT);
		favoritesPanel.add(favoriteRowCountLabel, 0, rowCount, 1, 1);
		favoriteRowCountLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(favoriteRowCountLabel, Priority.SOMETIMES);

		favoritesRowCountSlider = new Slider(20, 50, 25);
    Tooltip rowsTip = new Tooltip
        ("Set the maximum number of rows to appear in each column on the favorites page");
    Tooltip.install(favoritesRowCountSlider, rowsTip);
    favoritesRowCountSlider.setShowTickLabels(true);
    favoritesRowCountSlider.setShowTickMarks(true);
    favoritesRowCountSlider.setMajorTickUnit(5);
    favoritesRowCountSlider.setMinorTickCount(1);
    favoritesRowCountSlider.setBlockIncrement(1);
    favoritesRowCountSlider.setSnapToTicks(true);
    favoritesRowCountSlider.valueProperty().addListener(this::rowCountChanged);

		favoritesPanel.add(favoritesRowCountSlider, 1, rowCount, 1, 1);
		favoritesRowCountSlider.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(favoritesRowCountSlider, Priority.ALWAYS);

		rowCount++;
  } // end method buildUI

  /**
   Update the column count prefs. 
  
   @param prop     The property being updated. 
   @param oldValue The old value. 
   @param newValue The new value. 
  */
	private void columnCountChanged(ObservableValue<? extends Number> prop, 
	                    Number oldValue, 
	                    Number newValue) {
		if (! favoritesColumnCountSlider.isValueChanging()) {
      UserPrefs.getShared().setPref(FAVORITES_COLUMNS, 
          favoritesColumnCountSlider.getValue());
    }
	}
  
  /**
   Update row count prefs. 
  
   @param prop     The property being updated. 
   @param oldValue The old value.
   @param newValue The new value
  */
	private void rowCountChanged(ObservableValue<? extends Number> prop, 
	                    Number oldValue, 
	                    Number newValue) {
		if (! favoritesRowCountSlider.isValueChanging()) {
      UserPrefs.getShared().setPref(FAVORITES_ROWS, 
          favoritesRowCountSlider.getValue());
    }
	}
  
  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle() {
    return "Favorites";
  }
  
  /**
   Get a JavaFX Pane presenting all the preferences in this set to the user. 
  
   @return The JavaFX Pane containing Controls allowing the user to update
           all the preferences in this set. 
  */
  public Pane getPane() {
    return favoritesPanel;
  }
  
  /**
   Save all of these preferences to disk, so that they can be restored
   for the user at a later time. 
  */
  public void save() {
    
  }
}
