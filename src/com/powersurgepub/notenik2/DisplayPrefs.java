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

	import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.prefs.*;
	import com.powersurgepub.psutils2.strings.*;
	import com.powersurgepub.psutils2.ui.*;
	import com.powersurgepub.psutils2.widgets.*;

  import java.util.*;

 	import javafx.beans.value.*;
 	import javafx.event.*;
 	import javafx.scene.control.*;
  import javafx.scene.paint.*;
 	import javafx.scene.layout.*;
	import javafx.scene.text.*;

/**
 Preferences pane for controlling aspects of the note display. 

 @author Herb Bowie
 */
public class DisplayPrefs 
    implements
      PrefSet {
  
  public static final String DISPLAY_BACKGROUND_COLOR_KEY  = "displaybackcolor";
  public static final String DISPLAY_TEXT_COLOR_KEY        = "displaytextcolor";
  public static final String DISPLAY_NORMAL_FONT_SIZE_KEY  = "displayfontsize";
  public static final String DISPLAY_FONT_NAME_KEY         = "displayfontname";
  public static final String DISPLAY_METHOD_KEY            = "displaymethod";
  public static final String DISPLAY_SECONDS_KEY           = "displayseconds";
  public static final String DISPLAY_TITLE_KEY             = "displaytitle";
  public static final String DISPLAY_SOURCE_KEY            = "displaysource";
  public static final String DISPLAY_TYPE_KEY              = "displaytype";
  public static final String DISPLAY_ADDED_KEY             = "displayadded";
  public static final String DISPLAY_ID_KEY                = "displayid";
  
  private DisplayWindow     displayWindow = null;
  private DisplayPrefs      displayPrefs;
  
  private ProgramVersion    programVersion = ProgramVersion.getShared();
  
  private boolean           setupComplete = false;
  
  private   List<String>    fontList;
  
  private   Color           displayBackgroundColor   = Color.rgb (255, 255, 255);
  private   Color           displayTextColor         = Color.rgb (0, 0, 0);
  private   String          displayFont              = "Verdana";
  private   int             displayNormalFontSize    = 3;
  private   int             displayBigFontSize       = 4;
  
  private     StringBuilder textStyle;
  
  private     FXUtils       fxUtils;
  private     GridPane      displayPrefsPane;
  private     Label         displayFontLabel;
  private     ComboBoxWidget displayFontComboBox;
  private     Label         displayFontSizeLabel;
  private     Slider        displayFontSizeSlider;
  private     Label         displayBackgroundColorLabel;
  private     ColorPicker   displayBackgroundColorPicker;
  private     Label         displayTextColorLabel;
  private     ColorPicker   displayTextColorPicker;
  private     TextArea      displayTextSample;
  
  /** Creates new form DisplayPrefs */
  public DisplayPrefs(DisplayWindow displayWindow) {
    
    this.displayWindow = displayWindow;
    displayPrefs = this;
    
    buildUI();
    
    displayTextColor = StringUtils.hexStringToColor 
        (UserPrefs.getShared().getPref (DISPLAY_TEXT_COLOR_KEY, "000000"));
    
    displayBackgroundColor = StringUtils.hexStringToColor 
        (UserPrefs.getShared().getPref (DISPLAY_BACKGROUND_COLOR_KEY, "FFFFFF"));
    
    displayNormalFontSize 
        = UserPrefs.getShared().getPrefAsInt (DISPLAY_NORMAL_FONT_SIZE_KEY,  12);
    
    displayFontSizeSlider.setValue(displayNormalFontSize);

    displayBigFontSize = displayNormalFontSize + 4;


    displayFont = UserPrefs.getShared().getPref (DISPLAY_FONT_NAME_KEY, "Verdana");
    
    fontList = Font.getFamilies();
    for (int i = 0; i < fontList.size(); i++) {
      displayFontComboBox.getItems().add (fontList.get(i));
      if (displayFont.equals (fontList.get(i))) {
        displayFontComboBox.getSelectionModel().select(i);
      }
    }

    displaySampleText();
    
    setupComplete = true;
  }

  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		displayPrefsPane = new GridPane();
		fxUtils.applyStyle(displayPrefsPane);

		displayFontLabel = new Label("Display Font:");
		displayPrefsPane.add(displayFontLabel, 0, rowCount, 1, 1);
		displayFontLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(displayFontLabel, Priority.SOMETIMES);

		rowCount++;

		displayFontComboBox = new ComboBoxWidget();
    displayFontComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (setupComplete) {
          displayFont = displayFontComboBox.getSelectedString();
          UserPrefs.getShared().setPref(DISPLAY_FONT_NAME_KEY, displayFont);
          displaySampleText();
          displayWindow.displayPrefsUpdated(displayPrefs);
        }
		  } // end handle method
		}); // end event handler
		displayPrefsPane.add(displayFontComboBox, 0, rowCount, 2, 1);
		displayFontComboBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(displayFontComboBox, Priority.ALWAYS);

		rowCount++;

		displayFontSizeLabel = new Label("Display Font Size:");
		displayPrefsPane.add(displayFontSizeLabel, 0, rowCount, 1, 1);
		displayFontSizeLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(displayFontSizeLabel, Priority.SOMETIMES);

		displayFontSizeSlider = new Slider(8, 36, 12);
    displayFontSizeSlider.setShowTickLabels(true);
    displayFontSizeSlider.setShowTickMarks(true);
    displayFontSizeSlider.setMajorTickUnit(4);
    displayFontSizeSlider.setMinorTickCount(1);
    displayFontSizeSlider.setBlockIncrement(2);
    displayFontSizeSlider.setSnapToTicks(true);
    displayFontSizeSlider.valueProperty().addListener(this::fontSizeChanged);
		displayPrefsPane.add(displayFontSizeSlider, 1, rowCount, 1, 1);
		displayFontSizeSlider.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(displayFontSizeSlider, Priority.ALWAYS);

		rowCount++;

		displayBackgroundColorLabel = new Label("Display Background:");
		displayPrefsPane.add(displayBackgroundColorLabel, 0, rowCount, 1, 1);
		displayBackgroundColorLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(displayBackgroundColorLabel, Priority.SOMETIMES);

		displayBackgroundColorPicker = new ColorPicker();
    displayBackgroundColorPicker.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        displayBackgroundColor = displayBackgroundColorPicker.getValue();
        UserPrefs.getShared().setPref(DISPLAY_BACKGROUND_COLOR_KEY,
          StringUtils.colorToHexString(displayBackgroundColor));
        displaySampleText();
        displayWindow.displayPrefsUpdated(displayPrefs);
		  } // end handle method
		}); // end event handler
		displayPrefsPane.add(displayBackgroundColorPicker, 1, rowCount, 1, 1);
		displayBackgroundColorPicker.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(displayBackgroundColorPicker, Priority.ALWAYS);

		rowCount++;

		displayTextColorLabel = new Label("Display Text:");
		displayPrefsPane.add(displayTextColorLabel, 0, rowCount, 1, 1);
		displayTextColorLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(displayTextColorLabel, Priority.SOMETIMES);

		displayTextColorPicker = new ColorPicker();
    displayTextColorPicker.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        displayTextColor = displayTextColorPicker.getValue();
        UserPrefs.getShared().setPref(DISPLAY_TEXT_COLOR_KEY,
          StringUtils.colorToHexString(displayTextColor));
        displaySampleText();
        displayWindow.displayPrefsUpdated(displayPrefs);
		  } // end handle method
		}); // end event handler
		displayPrefsPane.add(displayTextColorPicker, 1, rowCount, 1, 1);
		displayTextColorPicker.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(displayTextColorPicker, Priority.ALWAYS);

		rowCount++;

		displayTextSample = new TextArea();
		displayPrefsPane.add(displayTextSample, 0, rowCount, 2, 1);
		displayTextSample.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(displayTextSample, Priority.ALWAYS);
		displayTextSample.setMaxHeight(Double.MAX_VALUE);
		displayTextSample.setPrefRowCount(100);
		GridPane.setVgrow(displayTextSample, Priority.ALWAYS);
		displayTextSample.setPrefRowCount(100);
		displayTextSample.setWrapText(true);

		rowCount++;
  } // end method buildUI
  
  /**
   Update the column count prefs. 

   @param prop     The property being updated. 
   @param oldValue The old value. 
   @param newValue The new value. 
  */
  private void fontSizeChanged(ObservableValue<? extends Number> prop, 
        Number oldValue, 
        Number newValue) {
    if (! displayFontSizeSlider.isValueChanging()) {
      displayNormalFontSize = (int)displayFontSizeSlider.getValue();
      UserPrefs.getShared().setPref(DISPLAY_NORMAL_FONT_SIZE_KEY, displayNormalFontSize);
      displaySampleText();
      displayWindow.displayPrefsUpdated(this);
    }
  }

  public void displaySampleText () {
    textStyle = new StringBuilder();
    appendStyle("fill", displayTextColor);
    appendStyle("background-color", displayBackgroundColor);
    appendStyle("font-family", displayFont);
    appendStyle("font-size", displayNormalFontSize);
    displayTextSample.setText ("There is nothing worse than a brilliant image of a fuzzy concept.");
  }  
  
  private void appendStyle(String type, String value) {
    textStyle.append("-fx-");
    textStyle.append(type);
    textStyle.append(": ");
    textStyle.append(value);
    textStyle.append("; ");
  }
  
  private void appendStyle(String type, Color value) {
    textStyle.append("-fx-");
    textStyle.append(type);
    textStyle.append(": ");
    textStyle.append(StringUtils.colorToHexString(value));
    textStyle.append("; ");
  }
  
  private void appendStyle(String type, int value) {
    textStyle.append("-fx-");
    textStyle.append(type);
    textStyle.append(": ");
    textStyle.append(String.valueOf(value));
    textStyle.append("; ");
  }
  
  public Color getDisplayBackgroundColor() {
    return displayBackgroundColor;
  }
  
  public Color getDisplayTextColor() {
    return displayTextColor;
  }
  
  public String getDisplayFont() {
    return displayFont;
  }
  
  public int getDisplayNormalFontSize() {
    return displayNormalFontSize;
  }
  
  public int getDisplayBigFontSize() {
    return displayBigFontSize;
  }
  
  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle() {
    return "Display";
  }
  
  /**
   Get a JavaFX Pane presenting all the preferences in this set to the user. 
  
   @return The JavaFX Pane containing Controls allowing the user to update
           all the preferences in this set. 
  */
  public Pane getPane() {
    return displayPrefsPane;
  }
  
  /**
   Save all of these preferences to disk, so that they can be restored
   for the user at a later time. 
  */
  public void save() {
    
  }
  
}
