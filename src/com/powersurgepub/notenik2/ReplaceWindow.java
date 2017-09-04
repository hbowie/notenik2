/*
 * Copyright 2009 - 2014 Herb Bowie
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

	import com.powersurgepub.psutils2.ui.*;

 	import javafx.beans.value.*;
 	import javafx.event.*;
 	import javafx.scene.*;
 	import javafx.scene.control.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;

/**
 A window for performing a find and replace operation.

 @author Herb Bowie
 */
public class ReplaceWindow 
    implements 
      WindowToManage {
  
  private     Notenik             notenik;
  
  private     FXUtils             fxUtils;
  private     Stage               replaceStage;
  private     Scene               replaceScene;
  private     GridPane            replacePane;
  private     Label               findLabel;
  private     TextField           findTextField;
  private     Label               replaceLabel;
  private     TextField           replaceTextField;
  private     CheckBox            titleCheckBox;
  private     CheckBox            caseSensitiveCheckBox;
  private     Button              findButton;
  private     CheckBox            linkCheckBox;
  private     Button              replaceButton;
  private     CheckBox            tagsCheckBox;
  private     Button              replaceAndFindButton;
  private     CheckBox            bodyCheckBox;
  private     Button              replaceAllButton;

  public ReplaceWindow(Notenik notenik) {
    this.notenik = notenik;
    buildUI();
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    replaceStage = new Stage(StageStyle.DECORATED);
    replaceStage.setTitle(getTitle());
    
    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		replacePane = new GridPane();
		fxUtils.applyStyle(replacePane);

		findLabel = new Label("Find:");
		replacePane.add(findLabel, 0, rowCount, 3, 1);

		rowCount++;

		findTextField = new TextField();
    findTextField.textProperty().addListener(this::findChanged); 
    findTextField.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        find();
		  } // end handle method
		}); // end event handler
		replacePane.add(findTextField, 0, rowCount, 3, 1);
		findTextField.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(findTextField, Priority.ALWAYS);

		rowCount++;

		replaceLabel = new Label("Replace:");
		replacePane.add(replaceLabel, 0, rowCount, 3, 1);
		replaceLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(replaceLabel, Priority.ALWAYS);

		rowCount++;

		replaceTextField = new TextField();
    replaceTextField.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {

		  } // end handle method
		}); // end event handler
		replacePane.add(replaceTextField, 0, rowCount, 3, 1);
		replaceTextField.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(replaceTextField, Priority.ALWAYS);

		rowCount++;

		titleCheckBox = new CheckBox("Title");
    titleCheckBox.setSelected(true);
    titleCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        resetFindButton();
		  } // end handle method
		}); // end event handler
		replacePane.add(titleCheckBox, 0, rowCount, 1, 1);
		titleCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(titleCheckBox, Priority.ALWAYS);

		caseSensitiveCheckBox = new CheckBox("Case sensitive");
    caseSensitiveCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        resetFindButton();
		  } // end handle method
		}); // end event handler
		replacePane.add(caseSensitiveCheckBox, 1, rowCount, 1, 1);
		caseSensitiveCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(caseSensitiveCheckBox, Priority.SOMETIMES);

		findButton = new Button("Find");
    findButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        find();
		  } // end handle method
		}); // end event handler
		replacePane.add(findButton, 2, rowCount, 1, 1);
		findButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(findButton, Priority.SOMETIMES);

		rowCount++;

		linkCheckBox = new CheckBox("Link");
    linkCheckBox.setSelected(true);
    linkCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        resetFindButton();
		  } // end handle method
		}); // end event handler
		replacePane.add(linkCheckBox, 0, rowCount, 1, 1);
		linkCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(linkCheckBox, Priority.SOMETIMES);

		replaceButton = new Button("Replace");
    replaceButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        replace();
		  } // end handle method
		}); // end event handler
		replacePane.add(replaceButton, 2, rowCount, 1, 1);
		replaceButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(replaceButton, Priority.SOMETIMES);

		rowCount++;

		tagsCheckBox = new CheckBox("Tags");
    tagsCheckBox.setSelected(true);
    tagsCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        resetFindButton();
		  } // end handle method
		}); // end event handler
		replacePane.add(tagsCheckBox, 0, rowCount, 1, 1);
		tagsCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(tagsCheckBox, Priority.SOMETIMES);

		replaceAndFindButton = new Button("Replace & Find");
    replaceAndFindButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        replaceAndFind();
		  } // end handle method
		}); // end event handler
		replacePane.add(replaceAndFindButton, 2, rowCount, 1, 1);
		replaceAndFindButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(replaceAndFindButton, Priority.SOMETIMES);

		rowCount++;

		bodyCheckBox = new CheckBox("Body");
    bodyCheckBox.setSelected(true);
    bodyCheckBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        resetFindButton();
		  } // end handle method
		}); // end event handler
		replacePane.add(bodyCheckBox, 0, rowCount, 1, 1);
		bodyCheckBox.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(bodyCheckBox, Priority.SOMETIMES);

		replaceAllButton = new Button("Replace All");
    replaceAllButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        notenik.replaceAll(findTextField.getText(), replaceTextField.getText(),
            titleCheckBox.isSelected(),
            linkCheckBox.isSelected(),
            tagsCheckBox.isSelected(),
            bodyCheckBox.isSelected(),
            true);
        setVisible(false);
		  } // end handle method
		}); // end event handler
		replacePane.add(replaceAllButton, 2, rowCount, 1, 1);
		replaceAllButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(replaceAllButton, Priority.SOMETIMES);

		rowCount++;
    
		replaceScene = new Scene(replacePane);
    replaceStage.setScene(replaceScene);
    
  } // end method buildUI
  
  public void findChanged(ObservableValue<? extends String> prop,
                          String oldValue,
                          String newValue) {
    if (! findTextField.getText().equals (notenik.getLastTextFound())) {
      resetFindButton();
    }
  }

  public void startReplace (String findString) {
    if (findString.length() > 0) {
      findTextField.setText(findString);
    }
  }
  
  public void noFindInProgress() {
    setFindButtonText(notenik.FIND);
    replaceButton.setDisable(true);
    replaceAndFindButton.setDisable(true);
  }
  
  public void findInProgress() {
    setFindButtonText(notenik.FIND_AGAIN);
    replaceButton.setDisable(false);
    replaceAndFindButton.setDisable(false);
  }
  
  public void setFindButtonText(String findButtonText) {
    findButton.setText(findButtonText);
  }
  
  private void resetFindButton() {
    notenik.noFindInProgress();
  }
  
  private void replaceAndFind() {
    replace();
    find();
  }
  
  private void find() {
    notenik.setFindText(findTextField.getText());
    notenik.findNote(
        findButton.getText(),
        findTextField.getText(),
        titleSelected(),
        linkSelected(),
        tagsSelected(),
        bodySelected(),
        caseSensitive(),
        true);
  }
  
  private void replace() {
    notenik.replaceNote(findTextField.getText(), replaceTextField.getText(),
        titleSelected(),
        linkSelected(),
        tagsSelected(),
        bodySelected());
  }
  
  public boolean titleSelected() {
    return titleCheckBox.isSelected();
  }
  
  public boolean linkSelected() {
    return linkCheckBox.isSelected();
  }
  
  public boolean tagsSelected() {
    return tagsCheckBox.isSelected();
  }
  
  public boolean bodySelected() {
    return bodyCheckBox.isSelected();
  }
  
  public boolean caseSensitive() {
    return caseSensitiveCheckBox.isSelected();
  }
  
  public String getTitle() {
    return ("Notenik Find and Replace");
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      replaceStage.show();
    } else {
      replaceStage.hide();
    }
  }
  
  public void toFront() {
    replaceStage.toFront();
  }
  
  public double getWidth() {
    return replaceStage.getWidth();
  }
  
  public double getHeight() {
    return replaceStage.getHeight();
  }
  
  public void setLocation(double x, double y) {
    replaceStage.setX(x);
    replaceStage.setY(y);
  }

}
