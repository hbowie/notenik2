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
  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.notenik.*;
  import com.powersurgepub.psutils2.prefs.*;
  import com.powersurgepub.psutils2.publish.*;
  import com.powersurgepub.psutils2.script.*;
  import com.powersurgepub.psutils2.textmerge.*;
  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;

  import javafx.application.*;
  import javafx.event.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
 Son of Notenik -- a program for creating, maintaining and accessing collections
 of notes. Notenik uses JavaFX instead of Swing for the UI elements. 

 @author Herb Bowie
 */
public class Notenik 
    extends Application
    implements
      ScriptExecutor {
  
  public static final String PROGRAM_NAME    = "Notenik";
  public static final String PROGRAM_VERSION = "4.00";
  
  public static final int    CHILD_WINDOW_X_OFFSET = 60;
  public static final int    CHILD_WINDOW_Y_OFFSET = 60;

  public static final        int    ONE_SECOND    = 1000;
  public static final        int    ONE_MINUTE    = ONE_SECOND * 60;
  public static final        int    ONE_HOUR      = ONE_MINUTE * 60;

  public static final String INVALID_URL_TAG = "Invalid URL";
  
  public static final String URLUNION_FILE_NAME           = "urlunion.html";
  public static final String INDEX_FILE_NAME              = "index.html";
  public static final String FAVORITES_FILE_NAME          = "favorites.html";
  public static final String NETSCAPE_BOOKMARKS_FILE_NAME = "bookmark.html";
  public static final String OUTLINE_FILE_NAME            = "outline.html";
  public static final String SUPPORT_FOLDER_NAME          = "urlunion";
  
  private             String  country = "  ";
  private             String  language = "  ";
  
  private             Appster appster;
  private             Home home;
  private             ProgramVersion      programVersion;
  
  // Variables used for logging
  private             Logger              logger;
  private             LogWindow           logWindow;
  
  /** File of Notes that is currently open. */
  private             NoteIO              noteIO = null;
  private             FileSpec            currentFileSpec = null;
  private             File                noteFile = null;
  private             File                currentDirectory;
  private             NoteExport          exporter;
  
  /** This is the current collection of Notes. */
  private             NoteList            noteList = null;
  
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
  
  private             PrefsJuggler        prefsJuggler;
  
  private             NoteSortParm        noteSortParm = new NoteSortParm();
  
  private             AboutWindow         aboutWindow;
  
  private             UserPrefs           userPrefs;
  
  private             GeneralPrefs        generalPrefs;
  private             DisplayPrefs        displayPrefs;
  private             WebPrefs            webPrefs;
  private             FavoritesPrefs      favoritesPrefs;
  private             FilePrefs           filePrefs;
  
  // private             CollectionPrefs     collectionPrefs;
  // private             MasterCollection    masterCollection;

  
  private             Reports             reports;
  private             boolean             editingMasterCollection = false;
  
  // Written flags
  private             boolean             urlUnionWritten = false;
  private             boolean             favoritesWritten = false;
  private             boolean             netscapeWritten = false;
  private             boolean             outlineWritten = false;
  private             boolean             indexWritten = false;
  
  @Override
  public void start(Stage primaryStage) {
    
    this.primaryStage = primaryStage;
    primaryStage.setTitle("Notenik");
    primaryLayout = new VBox();
    
    // Build most of the UI elements and init the Window Menu Manager
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
    logger.setLogOutput (logWindow);
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
    aboutWindow = new AboutWindow (primaryStage, false);
    
    home.setHelpMenu(primaryStage, helpMenu, aboutWindow);
    
    noteSortParm.populateMenu(sortMenu);
    
    reports = new Reports(reportsMenu);
    reports.setScriptExecutor(this);
    
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
  
  /**
   Method for callback while executing a PSTextMerge script. 
  
   @param operand 
  */
  public void scriptCallback (String operand) {
    pubOperation(reports.getReportsFolder(), operand);
  }
  
  /**
   Any pre-processing to do before PublishWindow starts its publication
   process. In particular, make the source data available to the publication
   script.

   @param publishTo The folder to which we are publishing.
   */
  public void prePub(File publishTo) {
    // File urlsTab = new File (publishTo, "urls.tab");
    // io.exportToTabDelimited(noteList, urlsTab, false, "");
    
    // File favoritesTab = new File (publishTo, "favorites.tab");
    // io.exportToTabDelimited(noteList, favoritesTab, true,
    //    generalPrefs.getFavoritesPrefs().getFavoritesTags());

    urlUnionWritten = false;
    favoritesWritten = false;
    netscapeWritten = false;
    outlineWritten = false;
    indexWritten = false;
  }

  /**
   Perform the requested publishing operation.
   
   @param operand
   */
  public boolean pubOperation(File publishTo, String operand) {
    boolean operationOK = false;
    if (operand.equalsIgnoreCase("urlunion")) {
      operationOK = publishURLUnion(publishTo);
    }
    else
    if (operand.equalsIgnoreCase("favorites")) {
      operationOK = publishFavorites(publishTo);
    }
    else
    if (operand.equalsIgnoreCase("netscape")) {
      operationOK = publishNetscape(publishTo);
    }
    else
    if (operand.equalsIgnoreCase("outline")) {
      operationOK = publishOutline(publishTo);
    }
    else
    if (operand.equalsIgnoreCase("index")) {
      operationOK = publishIndex(publishTo);
    }
    return operationOK;
  }

  /**
   Any post-processing to be done after PublishWindow has completed its
   publication process.

   @param publishTo The folder to which we are publishing.
   */
  public void postPub(File publishTo) {

  }

  private boolean publishURLUnion (File publishTo) {
    urlUnionWritten = false;
    File urlUnionFile = new File (publishTo, URLUNION_FILE_NAME);
    if (! urlUnionFile.toString().equals(noteFile.toString())) {
      exporter.exportToURLUnion (urlUnionFile, noteList);
      urlUnionWritten = true;
    }
    return urlUnionWritten;
  }

  private boolean publishFavorites (File publishTo) {
    
    // Publish selected favorites
    Logger.getShared().recordEvent(LogEvent.NORMAL, "Publishing Favorites", false);
    favoritesWritten = false;
    if (! noteFile.getName().equalsIgnoreCase (FAVORITES_FILE_NAME)) {
      favoritesWritten = exporter.publishFavorites
          (publishTo, noteList, favoritesPrefs);
    }
    return favoritesWritten;
  }

  private boolean publishNetscape (File publishTo) {
    // Publish in Netscape bookmarks format
    netscapeWritten = false;
    if (! noteFile.getName().equalsIgnoreCase (NETSCAPE_BOOKMARKS_FILE_NAME)) {
      File netscapeFile = new File (publishTo,
        NETSCAPE_BOOKMARKS_FILE_NAME);
      exporter.publishNetscape (netscapeFile, noteList);
      netscapeWritten = true;
    }
    return netscapeWritten;
  }

  private boolean publishOutline (File publishTo) {
    // Publish in outline form using dynamic html
    outlineWritten = false;
    if (! noteFile.getName().equalsIgnoreCase (OUTLINE_FILE_NAME)) {
      File dynamicHTMLFile = new File (publishTo, OUTLINE_FILE_NAME);
      exporter.publishOutline(dynamicHTMLFile, noteList);
      outlineWritten = true;
    }
    return outlineWritten;
  }

  private boolean publishIndex (File publishTo) {
    // Publish index file pointing to other files
    indexWritten = false;
    if (! noteFile.getName().equalsIgnoreCase (INDEX_FILE_NAME)) {
      File indexFile = new File (publishTo, INDEX_FILE_NAME);
      exporter.publishIndex(indexFile, noteFile,
         favoritesWritten, FAVORITES_FILE_NAME,
         netscapeWritten, NETSCAPE_BOOKMARKS_FILE_NAME,
         outlineWritten, OUTLINE_FILE_NAME);
      indexWritten = true;
    }
    return indexWritten;
  }
  
  public WebPrefs getWebPrefs() {
    return webPrefs;
  }
  
}
