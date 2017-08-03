/*
 * Copyright 2013 - 2017 Herb Bowie
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
	import com.powersurgepub.psutils2.publish.*;
	import com.powersurgepub.psutils2.ui.*;

 	import javafx.beans.value.*;
 	import javafx.event.*;
 	import javafx.scene.control.*;
 	import javafx.scene.layout.*;
  import javafx.scene.text.*;

/**
 
 @author Herb Bowie
 */
public class WebPrefs 
    implements 
      PrefSet,
      WebPrefsProvider {
  
  public static final String CSS_HREF = "css-href";
  
  public static final String CSS_HREF_DEFAULT 
      = "http://fonts.googleapis.com/css?family=Merriweather+Sans:400,700";
  
  public static final String FONT_FAMILY = "font-family";
  
  public static final String FONT_FAMILY_DEFAULT
      = "Merriweather Sans";
  
  public static final String FONT_SIZE = "font-size";
  
  public static final String FONT_SIZE_DEFAULT
      = "100%";
  
  private Notenik           notenikApp;

  private boolean           setupComplete = false;
  
  private     FXUtils       fxUtils;
  private     GridPane      webPrefsPanel;
  private     Label         webCSShrefLabel;
  private     TextArea      webCSShrefText;
  private     Label         webFontFamilyLabel;
  private     TextField     webFontFamilyText;
  private     Label         webFontSizeLabel;
  private     TextField     webFontSizeText;
  private     Button        webResetToDefaultsButton;

  
  /**
   Creates new form WebPrefs
   */
  public WebPrefs(Notenik notenikApp) {
    this.notenikApp = notenikApp;
    buildUI();
    webCSShrefText.setText
        (UserPrefs.getShared().getPref(CSS_HREF, CSS_HREF_DEFAULT));
    webFontFamilyText.setText
        (UserPrefs.getShared().getPref(FONT_FAMILY, FONT_FAMILY_DEFAULT));
    webFontSizeText.setText
        (UserPrefs.getShared().getPref(FONT_SIZE, FONT_SIZE_DEFAULT));
    setupComplete = true;
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		webPrefsPanel = new GridPane();
		fxUtils.applyStyle(webPrefsPanel);

		webCSShrefLabel = new Label("Link to Stylesheet for Fonts:");
		webCSShrefLabel.setTextAlignment(TextAlignment.RIGHT);
		webPrefsPanel.add(webCSShrefLabel, 0, rowCount, 1, 1);
		webCSShrefLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(webCSShrefLabel, Priority.SOMETIMES);

		webCSShrefText = new TextArea();
    webCSShrefText.textProperty().addListener(this::cssHrefChanged);
		webPrefsPanel.add(webCSShrefText, 1, rowCount, 1, 1);
		webCSShrefText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(webCSShrefText, Priority.ALWAYS);
		webCSShrefText.setMaxHeight(Double.MAX_VALUE);
		webCSShrefText.setPrefRowCount(100);
		GridPane.setVgrow(webCSShrefText, Priority.ALWAYS);
		webCSShrefText.setPrefRowCount(100);
		webCSShrefText.setWrapText(true);

		rowCount++;

		webFontFamilyLabel = new Label("Font Family:");
		webFontFamilyLabel.setTextAlignment(TextAlignment.RIGHT);
		webPrefsPanel.add(webFontFamilyLabel, 0, rowCount, 1, 1);
		webFontFamilyLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(webFontFamilyLabel, Priority.SOMETIMES);

		webFontFamilyText = new TextField();
    webFontFamilyText.textProperty().addListener(this::fontFamilyChanged);
    webFontFamilyText.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        saveFontFamilyPref();
		  } // end handle method
		}); // end event handler
		webPrefsPanel.add(webFontFamilyText, 1, rowCount, 1, 1);
		webFontFamilyText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(webFontFamilyText, Priority.ALWAYS);

		rowCount++;

		webFontSizeLabel = new Label("Font Size:");
		webFontSizeLabel.setTextAlignment(TextAlignment.RIGHT);
		webPrefsPanel.add(webFontSizeLabel, 0, rowCount, 1, 1);
		webFontSizeLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(webFontSizeLabel, Priority.SOMETIMES);

		webFontSizeText = new TextField();
    webFontSizeText.textProperty().addListener(this::fontSizeChanged);
    webFontSizeText.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {

		  } // end handle method
		}); // end event handler
		webPrefsPanel.add(webFontSizeText, 1, rowCount, 1, 1);
		webFontSizeText.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(webFontSizeText, Priority.ALWAYS);

		rowCount++;

		webResetToDefaultsButton = new Button("Reset to Defaults");
		webResetToDefaultsButton.setTextAlignment(TextAlignment.RIGHT);
    webResetToDefaultsButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        resetToDefaults();
		  } // end handle method
		}); // end event handler
		webPrefsPanel.add(webResetToDefaultsButton, 0, rowCount, 1, 1);
		webResetToDefaultsButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(webResetToDefaultsButton, Priority.SOMETIMES);

		rowCount++;
  } // end method buildUI
  
  public void cssHrefChanged(ObservableValue<? extends String> prop,
        String oldValue, String newValue) {
    saveCSShrefPref();
  }
  
  public void fontFamilyChanged(ObservableValue<? extends String> prop,
        String oldValue, String newValue) {
    saveFontFamilyPref();
  }
  
  public void fontSizeChanged(ObservableValue<? extends String> prop,
        String oldValue, String newValue) {
    saveFontSizePref();
  }
  
  public void saveCSShrefPref() {
    UserPrefs.getShared().setPref
        (CSS_HREF, webCSShrefText.getText());
  }
  
  public void saveFontFamilyPref() {
    UserPrefs.getShared().setPref
        (FONT_FAMILY, webFontFamilyText.getText());
  }
  
  public void saveFontSizePref() {
    UserPrefs.getShared().setPref
        (FONT_SIZE, webFontSizeText.getText());
  }

  public String getFontFamily() {
    return webFontFamilyText.getText();
  }
  
  public String getFontSize() {
    return webFontSizeText.getText();
  }
  
  public String getCSShref() {
    return webCSShrefText.getText();
  }
  
  private void resetToDefaults () {
    webCSShrefText.setText(CSS_HREF_DEFAULT);
    webFontFamilyText.setText(FONT_FAMILY_DEFAULT);
    webFontSizeText.setText(FONT_SIZE_DEFAULT);
  }
  
  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle() {
    return "Web";
  }
  
  /**
   Get a JavaFX Pane presenting all the preferences in this set to the user. 
  
   @return The JavaFX Pane containing Controls allowing the user to update
           all the preferences in this set. 
  */
  public Pane getPane() {
    return webPrefsPanel;
  }
  
  /**
   Save all of these preferences to disk, so that they can be restored
   for the user at a later time. 
  */
  public void save() {
    saveCSShrefPref();
    saveFontFamilyPref();
    saveFontSizePref();
  }

}
