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

  import com.powersurgepub.psutils2.links.*;
	import com.powersurgepub.psutils2.notenik.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.ui.*;
	import com.powersurgepub.psutils2.widgets.*;

  import java.util.*;

 	import javafx.event.*;
 	import javafx.scene.control.*;
 	import javafx.scene.layout.*;
 	import javafx.stage.*;


/**
  A JavaFX UI container for all the editable fields in a Notenik Collection. 

  @author Herb Bowie
 */
public class EditPane {
  
  private static final int                HEIGHT_SEP         = 16;
  private             FXUtils             fxUtils;
  private             GridPane            editPane;
  
  private             int                 rowCount;
  
  private             LinkLabel           linkLabel = null;
  private             DataWidget          linkWidget = null;
  private             TextSelector        tagsTextSelector = null;
  private             DataWidget          statusWidget = null;
  private             DateWidget          dateWidget = null;
  private             DataWidget          recursWidget = null;
  private             DataWidget          seqWidget = null;
  private             boolean             statusIncluded      = false;
  private             boolean             dateIncluded        = false;
  private             boolean             recursIncluded      = false;
  private             boolean             seqIncluded         = false;
  
  private             Label               lastModDateLabel;
  private             Label               lastModDateText;
  
  private             ArrayList<DataWidget> widgets = new ArrayList<DataWidget>();
  
  private             double                minHeight = 0;
  private             double                prefHeight = 0;  
  
  /**
   Creates new form EditTab
   */
  public EditPane() {
    
    fxUtils = FXUtils.getShared();
    rowCount = 0;

		editPane = new GridPane();
		fxUtils.applyStyle(editPane);
    
  }
  
  public void addWidgets(
      NoteCollectionModel model, 
      LinkTweakerInterface linkTweaker,
      DateWidgetOwner dateWidgetOwner,
      Stage editStage) {
    
    minHeight = 0;
    prefHeight = 0;
    WidgetWithLabel widgetWithLabel = new WidgetWithLabel();
    for (int i = 0; i < model.getNumberOfFields(); i++) {
      DataFieldDefinition fieldDef = model.getRecDef().getDef(i);
      widgetWithLabel = model.getNoteParms().getWidgetWithLabel(fieldDef, editPane, rowCount); 
      double widgetMinHeight = widgetWithLabel.getWidget().getMinHeight();
      double widgetPrefHeight = widgetWithLabel.getWidget().getPrefHeight();
      minHeight = minHeight + widgetMinHeight + HEIGHT_SEP;
      prefHeight = prefHeight + widgetPrefHeight + HEIGHT_SEP;
      switch (fieldDef.getType()) {
        // Special processing for Tags
        case (DataFieldDefinition.TAGS_TYPE):
          tagsTextSelector = (TextSelector)widgetWithLabel.getWidget();
          tagsTextSelector.setValueList(model.getTagsList());
          break;
        case (DataFieldDefinition.LINK_TYPE):
          linkWidget = widgetWithLabel.getWidget();
          linkLabel = (LinkLabel)widgetWithLabel.getLabel();
          linkLabel.setLinkTweaker(linkTweaker);
          break;
        case (DataFieldDefinition.STATUS_TYPE):
          statusWidget = widgetWithLabel.getWidget();
          statusIncluded = true;
          break;
        case (DataFieldDefinition.DATE_TYPE):
          dateWidget = (DateWidget)widgetWithLabel.getWidget();
          dateIncluded = true;
          break;
        case (DataFieldDefinition.RECURS_TYPE):
          recursIncluded = true;
          recursWidget = widgetWithLabel.getWidget();
          break;
        case (DataFieldDefinition.SEQ_TYPE):
          seqWidget = widgetWithLabel.getWidget();
          seqIncluded = true;
          break;
      }
      widgets.add(widgetWithLabel.getWidget());
      rowCount++;

    } // end for each data field
    
    if (dateIncluded) {
      dateWidget.setOwner(dateWidgetOwner);
      dateWidget.setStage(editStage);
    }
    
    if (tagsTextSelector != null) {
      tagsTextSelector.setValueList(model.getTagsList());
    }
    
    lastModDateLabel = new Label();
    lastModDateText = new Label();
    lastModDateLabel.setLabelFor(lastModDateText);
    lastModDateLabel.setText("Mod Date:");
    lastModDateText.setText("  ");
    editPane.add(lastModDateLabel, 0, rowCount);
    editPane.add(lastModDateText, 1, rowCount);
    double widgetMinHeight = lastModDateText.getMinHeight();
    minHeight = minHeight + widgetMinHeight + HEIGHT_SEP;
    double widgetPrefHeight = lastModDateText.getPrefHeight();
    prefHeight = prefHeight + widgetPrefHeight + HEIGHT_SEP;
    
    /*  Dimension mins = editPanel.getMinimumSize();
    int minW = mins.width;
    mins.setSize(minW, minHeight);
    editPanel.setMinimumSize(mins); 
    
    Dimension prefs = editPanel.getPreferredSize();
    int prefW = prefs.width;
    prefs.setSize(prefW, prefHeight);
    editPanel.setPreferredSize(prefs);
    
    editPanel.revalidate();
    */
    
  }
  
  /**
   Get the pane after the widgets have been added. 
  
   @return The pane containing all the edit widgets. 
  */
  public Pane getPane() {
    return editPane;
  }
  
  public boolean hasLink() {
    return (linkWidget != null && linkWidget.getText().length() > 0);
  }
  
  public String getLink() {
    if (linkWidget == null) {
      return "";
    } else {
      return linkWidget.getText();
    }
  }
  
  public void setLink(String link) {
    if (linkWidget != null) {
      linkWidget.setText(link);
    }
  }
  
  public boolean statusIsIncluded() {
    return statusIncluded;
  }
  
  public void setStatus(String status) {
    if (statusIsIncluded()) {
      statusWidget.setText(status);
    }
  }
  
  public boolean dateIsIncluded() {
    return dateIncluded;
  }
  
  public void setDate(String date) {
    if (dateIsIncluded()) {
      dateWidget.setText(date);
    }
  }
  
  public boolean recursIsIncluded() {
    return recursIncluded;
  }
  
  public String getRecurs() {
    if (recursIsIncluded()) {
      return recursWidget.getText();
    } else {
      return "";
    }
  }
  
  public boolean seqIsIncluded() {
    return seqIncluded;
  }
  
  public void setSeq(String seq) {
    if (seqIsIncluded()) {
      seqWidget.setText(seq);
    }
  }
  
  public boolean hasTags() {
    return (tagsTextSelector != null);
  }
  
  public void setTags(String tags) {
    if (hasTags()) {
      tagsTextSelector.setText(tags);
    }
  }
  
  public TextSelector getTagsTextSelector() {
    return tagsTextSelector;
  }
  
  private void tagsActionPerformed (java.awt.event.ActionEvent evt) {
    
  }
  
  public int getNumberOfFields() {
    return widgets.size();
  }
  
  public DataWidget get(int i) {
    return widgets.get(i);
  }
  
  public void setLastModDate(String modDateStr) {
    lastModDateText.setText(modDateStr);
  }
  
}
