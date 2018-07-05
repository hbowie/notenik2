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
  import com.powersurgepub.psutils2.values.Work;
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
public class EditPane
    implements TextHandler {
  
  private static final int                HEIGHT_SEP         = 16;
  private static final String             WORK_TITLE         = "work-title";

  private             NoteCollectionModel model = null;
  private             FXUtils             fxUtils;
  private             GridPane            editPane;
  
  private             int                 rowCount;
  
  private             LinkLabel           linkLabel = null;
  private             DataWidget          linkWidget = null;
  private             TextSelector        tagsTextSelector = null;
  private             TextSelector        authorTextSelector = null;
  private             TextSelector        workTitleTextSelector = null;
  private             TextSelector        workTypeTextSelector = null;
  private             DataWidget          statusWidget = null;
  private             DateWidget          dateWidget = null;
  private             DataWidget          recursWidget = null;
  private             DataWidget          seqWidget = null;
  private             DataWidget          workYearWidget = null;
  private             DataWidget          workIDWidget = null;
  private             DataWidget          workRightsWidget = null;
  private             DataWidget          workRightsHolderWidget = null;
  private             DataWidget          workPublisherWidget = null;
  private             DataWidget          workCityWidget = null;
  private             DataWidget          workLinkWidget = null;
  private             DataWidget          workMinorTitleWidget = null;

  private             boolean             statusIncluded      = false;
  private             boolean             dateIncluded        = false;
  private             boolean             recursIncluded      = false;
  private             boolean             seqIncluded         = false;
  private             boolean             authorIncluded      = false;
  private             boolean             workTitleIncluded   = false;
  private             boolean             workYearIncluded    = false;
  private             boolean             workTypeIncluded    = false;
  private             boolean             workIDIncluded      = false;
  private             boolean             workRightsIncluded  = false;
  private             boolean             workRightsHolderIncluded = false;
  private             boolean             workPublisherIncluded = false;
  private             boolean             workCityIncluded    = false;
  private             boolean             workLinkIncluded    = false;
  private             boolean             workMinorTitleIncluded = false;
  
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

    this.model = model;
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

      int fieldType = fieldDef.getType();
      String fieldName = fieldDef.getCommonName().getCommonForm();

      if (fieldName.equals(NoteParms.WORK_LINK_COMMON_NAME)) {
        workLinkWidget = widgetWithLabel.getWidget();
        workLinkIncluded = true;
      } else if (fieldName.equals(NoteParms.WORK_MINOR_TITLE_COMMON_NAME)) {
        workMinorTitleWidget = widgetWithLabel.getWidget();
        workMinorTitleIncluded = true;
      } else if (fieldType == DataFieldDefinition.TAGS_TYPE) {
        tagsTextSelector = (TextSelector) widgetWithLabel.getWidget();
        tagsTextSelector.setValueList(model.getTagsList());
        tagsTextSelector.setHandlesMultipleValues(true);
      } else if (fieldType == DataFieldDefinition.AUTHOR_TYPE) {
        authorTextSelector = (TextSelector) widgetWithLabel.getWidget();
        authorTextSelector.setValueList(model.getAuthorList());
        authorTextSelector.setHandlesMultipleValues(false);
        authorIncluded = true;
      } else if (fieldType == DataFieldDefinition.WORK_TYPE) {
        workTitleTextSelector = (TextSelector) widgetWithLabel.getWidget();
        workTitleTextSelector.setValueList(model.getWorkList());
        workTitleTextSelector.setHandlesMultipleValues(false);
        workTitleTextSelector.setFieldName(WORK_TITLE);
        workTitleTextSelector.addTextHandler(this);
        workTitleIncluded = true;
      } else if (fieldType == DataFieldDefinition.PICK_FROM_LIST) {
        if (fieldDef.getCommonName().equals(NoteParms.WORK_TYPE_COMMON_NAME)) {
          workTypeTextSelector = (TextSelector) widgetWithLabel.getWidget();
          workTypeTextSelector.setValueList(Work.getWorkTypeValueList());
          workTypeTextSelector.setHandlesMultipleValues(false);
          workTypeIncluded = true;
        }
      } else if (fieldType == DataFieldDefinition.LINK_TYPE) {
        linkWidget = widgetWithLabel.getWidget();
        linkLabel = (LinkLabel) widgetWithLabel.getLabel();
        linkLabel.setLinkTweaker(linkTweaker);
      } else if (fieldType == DataFieldDefinition.STATUS_TYPE) {
        statusWidget = widgetWithLabel.getWidget();
        statusIncluded = true;
      } else if (fieldType == DataFieldDefinition.DATE_TYPE) {
        dateWidget = (DateWidget) widgetWithLabel.getWidget();
        dateIncluded = true;
      } else if (fieldType == DataFieldDefinition.RECURS_TYPE) {
        recursIncluded = true;
        recursWidget = widgetWithLabel.getWidget();
      } else if (fieldType == DataFieldDefinition.SEQ_TYPE) {
        seqWidget = widgetWithLabel.getWidget();
        seqIncluded = true;
      } else if (fieldName.equals(NoteParms.WORK_IDENTIFIER_COMMON_NAME)) {
        workIDWidget = widgetWithLabel.getWidget();
        workIDIncluded = true;
      } else if (fieldName.equals(NoteParms.WORK_RIGHTS_COMMON_NAME)) {
        workRightsWidget = widgetWithLabel.getWidget();
        workRightsIncluded = true;
      } else if (fieldName.equals(NoteParms.WORK_RIGHTS_HOLDER_COMMON_NAME)) {
        workRightsHolderWidget = widgetWithLabel.getWidget();
        workRightsHolderIncluded = true;
      } else if (fieldName.equals(NoteParms.PUBLISHER_COMMON_NAME)) {
        workPublisherWidget = widgetWithLabel.getWidget();
        workPublisherIncluded = true;
      } else if (fieldName.equals(NoteParms.PUBLISHER_CITY_COMMON_NAME)) {
        workCityWidget = widgetWithLabel.getWidget();
        workCityIncluded = true;
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

    if (authorTextSelector != null) {
      authorTextSelector.setValueList(model.getAuthorList());
    }

    if (workTitleTextSelector != null) {
      workTitleTextSelector.setValueList(model.getWorkList());
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

  /**
   * When a text selector's value is updated, check to see if there are values
   * from other Notes that can be filled in for the user.
   *
   * @param fieldName The name identifying the field that was updated.
   */
  public void textSelectionUpdated(String fieldName) {

    if (fieldName.equals(WORK_TITLE)) {
      String workTitle = workTitleTextSelector.getText();
      Work work = model.getWorkList().getWork(workTitle);
      if (work != null) {

        if (work.hasAuthorName()
            && authorIncluded
            && authorTextSelector.getText().length() == 0) {
          authorTextSelector.setText(work.getAuthorName());
        }

        if (work.hasYear()
            && dateIncluded
            && dateWidget.getText().length() == 0) {
          dateWidget.setText(work.getYear());
        }

        if (work.hasType()
          && workTypeIncluded
            && (workTypeTextSelector.getText().equals("")
                || workTypeTextSelector.getText().equals(Work.UNKNOWN))) {
              workTypeTextSelector.setText(work.getTypeLabel());
        } // end if work from list has a type

        if (work.hasID()
          && workIDIncluded
            && workIDWidget.getText().length() == 0) {
            workIDWidget.setText(work.getID());
        }

        if (work.hasRights()
            && workRightsIncluded
            && workRightsWidget.getText().length() == 0) {
          workRightsWidget.setText(work.getRights());
        }

        if (work.hasRightsOwner()
            && workRightsHolderIncluded
            && workRightsHolderWidget.getText().length() == 0) {
          workRightsHolderWidget.setText(work.getRightsOwner());
        }

        if (work.hasPublisher()
            && workPublisherIncluded
            && workPublisherWidget.getText().length() == 0) {
          workPublisherWidget.setText(work.getPublisher());
        }

        if (work.hasCity()
            && workCityIncluded
            && workCityWidget.getText().length() == 0) {
          workCityWidget.setText(work.getCity());
        }

        if (work.hasLink()
            && workLinkIncluded
            && workLinkWidget.getText().length() == 0) {
          workLinkWidget.setText(work.getLink());
        }

        if (work.hasMinorTitle()
            && workMinorTitleIncluded
            && workMinorTitleWidget.getText().length() == 0) {
          workMinorTitleWidget.setText(work.getMinorTitle());
        }

      } // end if we already have data about this work
    } // end if work title being updated
  } // end method textSelectionUpdated
  
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

  public boolean hasAuthor() {
    return (authorTextSelector != null);
  }

  public void setAuthor(String author) {
    authorTextSelector.setText(author);
  }

  public TextSelector getAuthorTextSelector() {
    return authorTextSelector;
  }

  public boolean hasWorkTitle() {
    return (workTitleTextSelector != null);
  }

  public void setWorkTitle(String workTitle) {
    workTitleTextSelector.setText(workTitle);
  }

  public TextSelector getWorkTitleTextSelector() {
    return workTitleTextSelector;
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
