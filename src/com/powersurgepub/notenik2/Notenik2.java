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
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.ui.*;

  import javafx.application.*;
  import javafx.event.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
 Son of Notenik -- a program for creating, maintaining and accessing collections
 of notes. Notenik2 uses JavaFX instead of Swing for the UI elements. 

 @author Herb Bowie
 */
public class Notenik2 
    extends Application {
  
  public static final String PROGRAM_NAME    = "Notenik2";
  public static final String PROGRAM_VERSION = "4.00";
  
  private             String  country = "  ";
  private             String  language = "  ";
  
  private             Appster appster;
  private             Home home;
  private             ProgramVersion      programVersion;
  
  // Variables used for logging
  private             Logger              logger;
  private             LogWindow           logWindow;
  
  private             Stage               primaryStage;
  private             VBox                primaryLayout;
  private             Scene               primaryScene;
  
  
  private             MenuBar             menuBar;
  
  private             Menu                fileMenu        = new Menu("File");
  private             Menu                collectionMenu  = new Menu("Collection");
  private             Menu                sortMenu        = new Menu("Sort");
  private             Menu                noteMenu        = new Menu("Note");
  private             Menu                editMenu        = new Menu("Edit");
  private             Menu                toolsMenu       = new Menu("Tools");
  private             Menu                reportsMenu     = new Menu("Reports");
  private             Menu                optionsMenu     = new Menu("Options");
  private             Menu                windowMenu      = new Menu("Window");
  private             Menu                helpMenu        = new Menu("Help");
  
  private             ToolBar             toolBar;
  private             Button              okButton;
  private             Button              newButton;
  private             Button              deleteButton;
  private             Button              firstButton;
  private             Button              priorButton;
  private             Button              nextButton;
  private             Button              lastButton;
  private             Button              launchButton;
  private             TextField           findText;
  private             Button              findButton;
  
  private             SplitPane           mainSplitPane;
  private             TabPane             collectionTabs;
  private             Tab                 listTab;
  private             Tab                 tagsTab;
  private             TabPane             noteTabs;
  private             Tab                 displayTab;
  private             Tab                 editTab;
  
  private             StatusBar           statusBar;
  
  private             WindowMenuManager   windowMenuManager;
  
  
  @Override
  public void start(Stage primaryStage) {
    
    this.primaryStage = primaryStage;
    primaryStage.setTitle("Notenik 2");
    primaryLayout = new VBox();
    
    // Build most of the UI elements
    buildMenuBar();
    windowMenuManager = WindowMenuManager.getShared(windowMenu);
    
    buildToolBar();
    
    buildContent();
    
    statusBar = new StatusBar();
    primaryLayout.getChildren().add(statusBar.getPane());
    
    primaryScene = new Scene(primaryLayout, 600, 400);
    
    // Let's set up Logging
    logger = Logger.getShared();
    logWindow = new LogWindow (primaryStage);
    logger.setLog (logWindow);
    logger.setLogAllData (false);
    logger.setLogThreshold (LogEvent.NORMAL);
    windowMenuManager.add(logWindow);
    
    logger.recordEvent(LogEvent.NORMAL, 
        PROGRAM_NAME + " " + PROGRAM_VERSION + " starting up", 
        false);

    windowMenuManager.makeVisible(logWindow);
    
    // Set up some of the shared singleton objects
    appster = new Appster
        (this,
          "powersurgepub", "com",
          PROGRAM_NAME, PROGRAM_VERSION,
          primaryStage);
    home = Home.getShared ();
    programVersion = ProgramVersion.getShared ();
    
    home.setHelpMenu(primaryStage, helpMenu);
    
    // Now let's bring the curtains up
    primaryStage.setScene(primaryScene);
    primaryStage.show();
  }
  
  private void buildMenuBar() {
    menuBar = new MenuBar();
    menuBar.setUseSystemMenuBar(true);
    menuBar.getMenus().addAll(fileMenu, collectionMenu, sortMenu, noteMenu,
        editMenu, toolsMenu, reportsMenu, optionsMenu, windowMenu, helpMenu);
    menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
    primaryLayout.getChildren().add(menuBar);
  }
  
  /**
   Build the toolbar.
  */
  private void buildToolBar() {
    toolBar = new ToolBar();
    
    okButton = new Button("OK");
    okButton.setTooltip(new Tooltip("Complete your entries for this note"));
    toolBar.getItems().add(okButton);
    
    newButton = new Button("+");
    newButton.setTooltip(new Tooltip("Add a new note"));
    toolBar.getItems().add(newButton);
        
    deleteButton = new Button("-");
    deleteButton.setTooltip(new Tooltip("Delete this note"));
    toolBar.getItems().add(deleteButton);
        
    firstButton = new Button("<<");
    firstButton.setTooltip(new Tooltip("Go to the first note"));
    toolBar.getItems().add(firstButton);
        
    priorButton = new Button("<");
    priorButton.setTooltip(new Tooltip("Go to the prior note"));
    toolBar.getItems().add(priorButton);
        
    nextButton = new Button(">");
    nextButton.setTooltip(new Tooltip("Go to the next note"));
    toolBar.getItems().add(nextButton);
        
    lastButton = new Button(">>");
    lastButton.setTooltip(new Tooltip("Go to the last note"));
    toolBar.getItems().add(lastButton);
        
    launchButton = new Button("Launch");
    launchButton.setTooltip(new Tooltip("Open this Link in your Web Browser"));
    toolBar.getItems().add(launchButton);
        
    findText = new TextField("");
    findText.setTooltip(new Tooltip("Enter some text you'd like to find"));
    toolBar.getItems().add(findText);
        
    findButton = new Button("Find");
    findButton.setTooltip(new Tooltip("Find the text you entered"));
    toolBar.getItems().add(findButton);
    
    primaryLayout.getChildren().add(toolBar);
  }
  
  /**
   Build the main content.
  */
  private void buildContent() {

    mainSplitPane = new SplitPane();
    
    collectionTabs = new TabPane();
    listTab = new Tab("List");
    listTab.setClosable(false);
    tagsTab = new Tab("Tags");
    tagsTab.setClosable(false);
    collectionTabs.getTabs().addAll(listTab, tagsTab);
    
    noteTabs = new TabPane();
    displayTab = new Tab("Display");
    displayTab.setClosable(false);
    editTab = new Tab("Edit");
    editTab.setClosable(false);
    noteTabs.getTabs().addAll(displayTab, editTab);
    
    mainSplitPane.getItems().addAll(collectionTabs, noteTabs);
    primaryLayout.getChildren().add(mainSplitPane);
  }

  /**
   @param args the command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
  
}
