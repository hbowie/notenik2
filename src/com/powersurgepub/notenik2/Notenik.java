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

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.links.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.notenik.*;
  import com.powersurgepub.psutils2.prefs.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.publish.*;
  import com.powersurgepub.psutils2.script.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.tags.*;
  import com.powersurgepub.psutils2.textmerge.*;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;
  import com.powersurgepub.psutils2.widgets.*;

  import java.io.*;
  import java.net.*;
  import java.text.*;
  import java.util.*;

  import javafx.application.*;
  import javafx.event.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.control.Alert.*;
  import javafx.scene.control.ButtonBar.*;
  import javafx.scene.input.*;
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
      AppToBackup,
      DisplayWindow,
      FileSpecOpener,
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
  private             LogOutput           logOutput;
  
  /** File of Notes that is currently open. */
  private             NoteIO              noteIO = null;
  private             FileSpec            currentFileSpec = null;
  private             File                noteFile = null;
  private             File                currentDirectory;
  private             NoteExport          exporter;
  private             String              oldTitle = "";
  private             String              oldSeq = "";
  private             String              fileName = "";
  
  /** This is the current collection of Notes. */
  private             NoteList            noteList = null;
  
  private             Stage               primaryStage;
  private             VBox                primaryLayout;
  private             Scene               primaryScene;
  
  
  private             MenuBar             menuBar;
  
  private             Menu                fileMenu        = new Menu("File");
  private             Menu                openRecentMenu;
  private             MenuItem            openMasterCollectionMenuItem;
  private             MenuItem            createMasterCollectionMenuItem;
  
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
  
  private             DisplayPane         displayPane;
  private             EditPane            editPane;
  
  private             StatusBar           statusBar;
  
  private             WindowMenuManager   windowMenuManager;
  
  private             NoteSortParm        noteSortParm = new NoteSortParm();
  
  private             AboutWindow         aboutWindow;
  
  private             PublishWindow       publishWindow;
  
  private             LinkTweaker         linkTweaker;
  
  private             UserPrefs           userPrefs;
  
  private             PrefsJuggler        appPrefs;
  private             GeneralPrefs        generalPrefs;
  private             DisplayPrefs        displayPrefs;
  private             WebPrefs            webPrefs;
  private             FavoritesPrefs      favoritesPrefs;
  private             TagsPrefs           tagsPrefs;
  private             FilePrefs           filePrefs;
  private             TweakerPrefs        tweakerPrefs;
  
  private             PrefsJuggler        collectionPrefs;
  private             FolderSyncPrefs     folderSyncPrefs;
  private             HTMLPrefs           htmlPrefs;
  
  private             MasterCollection    masterCollection;
  
  private             Reports             reports;
  private             boolean             editingMasterCollection = false;
  
  private             NotePositioned      position = null;
  private             boolean             modified = false;
  private             boolean             unsavedChanges = false;
  
  // Written flags
  private             boolean             urlUnionWritten = false;
  private             boolean             favoritesWritten = false;
  private             boolean             netscapeWritten = false;
  private             boolean             outlineWritten = false;
  private             boolean             indexWritten = false;
  
  private             Trouble             trouble = Trouble.getShared();

  private             File                appFolder;
  private             String              userName;
  private             String              userDirString;
  
  // Replace Window
  private             ReplaceWindow       replaceWindow;
  
  // File Info Window
  private             FileInfoWindow      fileInfoWindow;

  // Variables used for logging


  private DateFormat    longDateFormatter
      = new SimpleDateFormat ("EEEE MMMM d, yyyy");
  private DateFormat  backupDateFormatter
      = new SimpleDateFormat ("yyyy-MM-dd-HH-mm");
  private    DateFormat       dateFormatter
    = new SimpleDateFormat ("yyyy-MM-dd");

  
  // The following fields define the fields in the collection. 
  // private             int                 noteType = NoteParms.NOTES_ONLY_TYPE;
  // private             DataDictionary      dict = null;
  // private             RecordDefinition    recDef = null;
  
  private             FileChooser         fileChooser = new FileChooser();

  public  static final String             FIND = "Find";
  public  static final String             FIND_AGAIN = "Again";

  private             String              lastTextFound = "";
  
  private             Note                foundNote = null;
  
  private             StringBuilder       titleBuilder = new StringBuilder();
  private             int                 titleStart = -1;
  private             StringBuilder       linkBuilder = new StringBuilder();
  private             int                 linkStart = -1;
  private             StringBuilder       tagsBuilder = new StringBuilder();
  private             int                 tagsStart = -1;
  private             StringBuilder       bodyBuilder = new StringBuilder();
  private             int                 bodyStart = -1;

  // Fields used to validate Web Page Notes
  private             javax.swing.Timer   validateURLTimer;
  private             ThreadGroup         webPageGroup;
  private             ArrayList           urlValidators;
  private             int                 progressMax = 0;
  private             int                 progress = 0;
  private             int                 badPages = 0;
  
  // System ClipBoard fields
  boolean             clipBoardOwned = false;
  Clipboard           clipBoard = null;
  Transferable        clipContents = null;
  
  private             TextMergeHarness    textMerge = null;
  
  private             DisplayPane          displayPane;
  private             EditPane             editPane;
  public static final int DISPLAY_TAB_INDEX = 0;
  public static final int CONTENT_TAB_INDEX = 1;
  
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
    
    // Initialize user preferences
    userPrefs = UserPrefs.getShared();
    
    // Build window for App Preferences
    appPrefs = new PrefsJuggler(primaryStage);
    
    appPrefs.addGeneralPrefs();
    generalPrefs = appPrefs.getGeneralPrefs();
    
    displayPrefs = new DisplayPrefs(this);
    appPrefs.addSet(displayPrefs);
    
    webPrefs = new WebPrefs(this);
    reports.setWebPrefs(webPrefs);
    appPrefs.addSet(webPrefs);
    
    favoritesPrefs = new FavoritesPrefs();
    appPrefs.addSet(favoritesPrefs);
    
    tagsPrefs = new TagsPrefs();
    appPrefs.addSet(tagsPrefs);
    
    filePrefs = new FilePrefs(this);
    filePrefs.loadFromPrefs();
    appPrefs.addSet(filePrefs);
    
    tweakerPrefs = new TweakerPrefs();
    appPrefs.addSet(tweakerPrefs);
    
    appPrefs.setScene();
    appPrefs.addToMenu(optionsMenu, true);
    WindowMenuManager.getShared().add(appPrefs);
    
    // Set up separate window for Collection Preferences
    collectionPrefs = new PrefsJuggler(primaryStage);
    
    folderSyncPrefs = new FolderSyncPrefs(this, primaryStage);
    collectionPrefs.addSet(folderSyncPrefs);
    
    htmlPrefs = new HTMLPrefs(this, primaryStage);
    collectionPrefs.addSet(htmlPrefs);
    
    collectionPrefs.setScene();
    
    exporter = new NoteExport(this);
    
    masterCollection = new MasterCollection();
    filePrefs.setRecentFiles(masterCollection.getRecentFiles());
    masterCollection.registerMenu(openRecentMenu, this);
    
    masterCollection.load();
    
    if (filePrefs.purgeRecentFilesAtStartup()) {
      masterCollection.purgeInaccessibleFiles();
    }
    
    if (masterCollection.hasMasterCollection()) {
      createMasterCollectionMenuItem.setDisable(true);
      openMasterCollectionMenuItem.setDisable(false);
    } else {
      createMasterCollectionMenuItem.setDisable(false);
      openMasterCollectionMenuItem.setDisable(true);
    }
    
    // initRecDef();
    // noteList = new NoteList(recDef);
    // position = new NotePositioned(recDef);
    // buildNoteTabs();

    // Set initial UI prefs
    GeneralPrefs.getShared().setSplitPane(mainSplitPane);
    GeneralPrefs.getShared().setMainWindow(primaryStage);
    
    // Now let's bring the curtains up
    primaryStage.setScene(primaryScene);
    primaryStage.setWidth
        (userPrefs.getPrefAsInt (FavoritesPrefs.PREFS_WIDTH, 620));
    primaryStage.setHeight
        (userPrefs.getPrefAsInt (FavoritesPrefs.PREFS_HEIGHT, 620));
    primaryStage.setX
        (userPrefs.getPrefAsInt (FavoritesPrefs.PREFS_LEFT, 100));
    primaryStage.setY
      (userPrefs.getPrefAsInt (FavoritesPrefs.PREFS_TOP,  100));
    primaryStage.show();
    
    WindowMenuManager.getShared().hide(logWindow);
  }
  
  @Override
  public void stop() {
    appPrefs.save();
  }
  
  private void buildMenuBar() {
    menuBar = new MenuBar();
    menuBar.setUseSystemMenuBar(true);
    menuBar.getMenus().addAll(fileMenu, collectionMenu, sortMenu, noteMenu,
        editMenu, toolsMenu, reportsMenu, optionsMenu, windowMenu, helpMenu);
    menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
    primaryLayout.getChildren().add(menuBar);
    
    openRecentMenu.setText("Open Recent");
    fileMenu.getItems().add(openRecentMenu);
    
    openMasterCollectionMenuItem = new MenuItem();
    KeyCombination mkc
        = new KeyCharacterCombination("M", KeyCombination.SHORTCUT_DOWN);
    openMasterCollectionMenuItem.setAccelerator(mkc);
    openMasterCollectionMenuItem.setText("Open Master Collection");
    openMasterCollectionMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        openMasterCollection();
      }
    });
    fileMenu.getItems().add(openMasterCollectionMenuItem);
    
    createMasterCollectionMenuItem = new MenuItem();
    createMasterCollectionMenuItem.setText("Create Master Collection...");
    createMasterCollectionMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        createMasterCollection();
      }
    });
    fileMenu.getItems().add(createMasterCollectionMenuItem);
    
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
  
  private void createMasterCollection() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle ("Create Master Collection");
    if (FileUtils.isGoodInputDirectory(currentDirectory)) {
      chooser.setInitialDirectory (currentDirectory);
    }
    File selectedFile = chooser.showDialog (primaryStage);
    if(selectedFile != null) {
      int filesSaved = masterCollection.createMasterCollection(selectedFile);
      if (filesSaved > 0) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("Master Collection Creation Results");
        alert.setHeaderText(null);
        alert.setContentText(String.valueOf(filesSaved) 
                + " Recent Files successfully saved to "
                + GlobalConstants.LINE_FEED
                + selectedFile.toString());
        alert.showAndWait();
        createMasterCollectionMenuItem.setDisable(true);
        openMasterCollectionMenuItem.setDisable(false);
      } // End if we saved any recent files
    } // End if user selected a file
  } // end method createMasterCollection
  
  private void openMasterCollection() {
    boolean modOK = modIfChanged();
    if (modOK) {
      if (masterCollection.hasMasterCollection()) {
        File selectedFile = masterCollection.getMasterCollectionFolder();
        if (goodCollection(selectedFile)) {
          closeFile();
          openFile (selectedFile, "", true);
        } else {
          Trouble.getShared().report (
              "Trouble opening file " + selectedFile.toString(),
              "File Open Error");
        }
      } // end if we have a master collection
    } // end if mod ok
  }
  
  public void handleOpenFile (FileSpec fileSpec) {
    handleOpenFile (new File(fileSpec.getPath()));
  }
  
  /**      
    Standard way to respond to a document being passed to this application on a Mac.
   
    @param inFile File to be processed by this application, generally
                  as a result of a file or directory being dragged
                  onto the application icon.
   */
  public void handleOpenFile (File inFile) {
    boolean modOK = modIfChanged();
    if (modOK) {
      if (goodCollection(inFile)) {
        closeFile();
        openFile (inFile, "", true);
      }
    }
  }
  
  /**
   Close the current notes collection in an orderly fashion. 
  */
  private void closeFile() {
    if (this.noteFile != null) {
      publishWindow.closeSource();
      if (currentFileSpec != null) {
        currentFileSpec.setNoteSortParm(noteSortParm.getParm());
      }
      filePrefs.handleClose();
    }
  }
  
  public void displayPrefsUpdated(DisplayPrefs displayPrefs) {
    if (position != null && displayTab != null) {
      buildDisplayTab();
    }
  }
  
  /**
   Provide a static, non-editable display of the note on the display tab. 
  */
  private void buildDisplayTab() {
    Note note = position.getNote();
    
    displayPane.startDisplay();
    if (note.hasTags()) {
      displayPane.displayTags(note.getTags());
    }
    
    displayPane.displayTitle(note.getTitle());
    
    if (note.hasLink()) {
      displayPane.displayLink(
          NoteParms.LINK_FIELD_NAME, 
          "", 
          note.getLinkAsString());
    }
    
    if (editPane.getNumberOfFields() == noteIO.getNumberOfFields()) {
      for (int i = 0; i < noteIO.getNumberOfFields(); i++) {
        DataFieldDefinition fieldDef = noteIO.getRecDef().getDef(i);
        String fieldName = fieldDef.getProperName();
        DataWidget widget = editPane.get(i);
        if (fieldName.equalsIgnoreCase(NoteParms.TITLE_FIELD_NAME)) {
          // Ignore -- already handled above
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.LINK_FIELD_NAME)) {
          // Ignore -- already handled above
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.TAGS_FIELD_NAME)) {
          // Ignore -- already handled above
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.BODY_FIELD_NAME)) {
          // Ignore -- handled below
        } 
        else {
          DataField nextField = note.getField(i);
          displayPane.displayField(fieldName, nextField.getData());
        }

      } // end for each data field
    
      if (note.hasBody()) {
        displayPane.displayLabelOnly("Body");
        displayPane.displayBody(note.getBody());
      }

      displayPane.displayDivider();
    
    }
    
    displayPane.displayDateAdded(note.getLastModDateStandard());
    
    displayPane.finishDisplay();
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
  
  /**
   Sync the list with a Notational Velocity style folder. 
  
   @return True if everything went OK. 
  */
  public boolean syncWithFolder () {
    
    boolean ok = true;
    StringBuilder msgs = new StringBuilder();
    
    String syncFolderString = folderSyncPrefs.getSyncFolder();
    String syncPrefix = folderSyncPrefs.getSyncPrefix();
    boolean sync = folderSyncPrefs.getSync();
    File syncFolder = null;
    
    // Check to see if we have the info we need to do a sync
    if (syncFolderString == null
        || syncFolderString.length() == 0
        || syncPrefix == null
        || syncPrefix.length() == 0
        || (! sync)) {
      ok = false;
    }
    
    if (ok) {
      syncFolder = new File (syncFolderString);
      if (goodCollection(syncFolder)) {
      } else {
        Trouble.getShared().report(
            primaryStage, 
            "Trouble reading from folder: " + syncFolder.toString(), 
            "Problem with Sync Folder");
        ok = false;
      }
    }
    
    int synced = 0;
    int added = 0;
    int addedToSyncFolder = 0;
    
    if (ok) {  
      
      // Now go through the items on the list and mark them all as unsynced
      Note workNote;
      for (int workIndex = 0; workIndex < noteList.size(); workIndex++) {
        workNote = noteList.get (workIndex);
        workNote.setSynced(false);
      }

      // Now match directory entries in the folder with items on the list
      DirectoryReader directoryReader = new DirectoryReader (syncFolder);
      directoryReader.setLog (Logger.getShared());
      try {
        directoryReader.openForInput();
        while (! directoryReader.isAtEnd()) {
          File nextFile = directoryReader.nextFileIn();
          FileName nextFileName = new FileName(nextFile);
          if ((nextFile != null) 
              && (! nextFile.getName().startsWith ("."))
              && nextFile.exists()
              && NoteIO.isInterestedIn(nextFile)
              && nextFile.getName().startsWith(syncPrefix)
              && nextFileName.getBase().length() > syncPrefix.length()) {
            String fileNameBase = nextFileName.getBase();
            String nextTitle 
                = fileNameBase.substring(syncPrefix.length()).trim();
            int i = 0;
            boolean found = false;
            while (i < noteList.size() && (! found)) {
              workNote = noteList.get(i);
              found = (workNote.getTitle().equals(nextTitle));
              if (found) {
                workNote.setSynced(true);
                Date lastModDate = new Date (nextFile.lastModified());
                if (lastModDate.compareTo(workNote.getLastModDate()) > 0) {
                  Note syncNote = noteIO.getNote(nextFile, syncPrefix);
                  msgs.append(
                      "Note updated to match more recent info from sync folder for "
                      + syncNote.getTitle()
                      + "\n");
                  workNote.setTags(syncNote.getTagsAsString());
                  workNote.setLink(syncNote.getLinkAsString());
                  workNote.setBody(syncNote.getBody());
                  noteIO.save(syncNote, true);
                }
                synced++;
              } else {
                i++;
              }
            } // end while looking for a matching newNote
            if ((! found)) {
              // Add new nvAlt newNote to Notenik collection
              Note syncNote = noteIO.getNote(nextFile, syncPrefix);
              syncNote.setLastModDateToday();
              try {
                noteIO.save(syncNote, true);
                position = noteList.add (syncNote);
              } catch (IOException e) {
                ioException(e);
              }
            }
          } // end if file exists, can be read, etc.
        } // end while more files in sync folder
        directoryReader.close();
      } catch (IOException ioe) {
        Trouble.getShared().report(primaryStage, 
            "Trouble reading sync folder: " + syncFolder.toString(), 
            "Sync Folder access problems");
        ok = false;
      } // end if caught I/O Error
    }
      
    if (ok) {
      msgs.append(String.valueOf(added) + " "
          + StringUtils.pluralize("item", added)
          + " added\n");
      
      msgs.append(String.valueOf(synced)  + " existing "
          + StringUtils.pluralize("item", synced)
          + " synced\n");
      
      // Now add any unsynced notes to the sync folder
      Note workNote;
      for (int workIndex = 0; workIndex < noteList.size(); workIndex++) {
        workNote = noteList.get(workIndex);
        if (! workNote.isSynced()) {
          workNote.setLastModDateToday();
          saveNote(workNote);
          msgs.append("Added to Sync Folder " + workNote.getTitle() + "\n");
          addedToSyncFolder++;
        }
      } // end of list of notes
      msgs.append(String.valueOf(addedToSyncFolder) + " "
          + StringUtils.pluralize("note", addedToSyncFolder)
          + " added to sync folder\n");
      msgs.append("Folder Sync Completed!\n");
    }
    
    if (ok) {
      logger.recordEvent (LogEvent.NORMAL,
        msgs.toString(),
        false);
    }
    return ok;
      
  }
  
  /**
   Check to see if the passed file seems to point to a valid 
   Collection folder. 
  
   @param fileToCheck The file to be checked. 
  
   @return false if file is null, doesn't exist, or isn't a directory,
           or can't be read, or can't be written.         
  */
  public boolean goodCollection(File fileToCheck) {
    return (fileToCheck != null
      && fileToCheck.exists()
      && fileToCheck.isDirectory()
      && fileToCheck.canRead()
      && fileToCheck.canWrite());
  }
  
  /**
   Saves a newNote in its primary location and in its sync folder, if specified. 
  
   @param note The newNote to be saved. 
  */
  protected boolean saveNote(Note note) {
    try {
      noteIO.save(note, true);
      if (folderSyncPrefs.getSync()) {
        noteIO.saveToSyncFolder(
            folderSyncPrefs.getSyncFolder(), 
            folderSyncPrefs.getSyncPrefix(), 
            note);
        note.setSynced(true);
      }
      return true;
    } catch (IOException e) {
      ioException(e);
      return false;
    }
  }
  
  /**
   Backup without prompting the user. 
  
   @return True if backup was successful. 
  */
  public boolean backupWithoutPrompt() {

    boolean backedUp = false;
    
    if (noteFile != null && noteFile.exists()) {
      FileName urlFileName = new FileName (noteFile);
      File backupFolder = getBackupFolder();
      String backupFileName 
          = filePrefs.getBackupFileName(noteFile, urlFileName.getExt());
      File backupFile = new File 
          (backupFolder, backupFileName);
      backedUp = backup (backupFile);
    }

    return backedUp;
    
  }
  
  /**
   Prompt the user for a backup location. 

   @return True if backup was successful.
  */
  public boolean promptForBackup() {
    boolean modOK = modIfChanged();
    boolean backedUp = false;
    DirectoryChooser chooser = new DirectoryChooser();

		if (modOK) {
      chooser.setTitle ("Make Backup of Notenik Folder");
      FileName noteFileName = new FileName (home.getUserHome());
      if (noteFile != null && noteFile.exists()) {
        noteFileName = new FileName (noteFile);
      }
      File backupFolder = getBackupFolder();
      chooser.setInitialDirectory (backupFolder);
      File selectedFile = chooser.showDialog (primaryStage);
      if (selectedFile != null) {
        File backupFile = selectedFile;
        backedUp = backup (backupFile);
        FileSpec fileSpec = masterCollection.getFileSpec(0);
        fileSpec.setBackupFolder(backupFile);
        if (backedUp) {
          Alert alert = new Alert(AlertType.INFORMATION);
          alert.setTitle("Backup Results");
          alert.setContentText("Backup completed successfully");
          alert.showAndWait();
        } // end if backed up successfully
      } // end if the user selected a backup location
    } // end if modIfChanged had no problems

    return backedUp;

  }
  
  /**
   Backup the data store to the indicated location. 
  
   @param backupFile The backup file to be used. 
  
   @return 
  */
  public boolean backup(File folderForBackups) {
    
    StringBuilder backupPath = new StringBuilder();
    StringBuilder fileNameWithoutDate = new StringBuilder();
    try {
      backupPath.append(folderForBackups.getCanonicalPath());
    } catch (IOException e) {
      backupPath.append(folderForBackups.getAbsolutePath());
    }
    backupPath.append(File.separator);
    String noteFileName = noteFile.getName();
    if (noteFileName.equalsIgnoreCase("notes")) {
      backupPath.append(noteFile.getParentFile().getName());
      backupPath.append(" ");
      fileNameWithoutDate.append(noteFile.getParentFile().getName());
      fileNameWithoutDate.append(" ");
    }
    backupPath.append(noteFile.getName());
    fileNameWithoutDate.append(noteFile.getName());
    backupPath.append(" ");
    fileNameWithoutDate.append(" ");
    backupPath.append("backup ");
    fileNameWithoutDate.append("backup ");
    backupPath.append(filePrefs.getBackupDate());
    File backupFolder = new File (backupPath.toString());
    backupFolder.mkdir();
    boolean backedUp = FileUtils.copyFolder (noteFile, backupFolder);
    if (backedUp) {
      FileSpec fileSpec = masterCollection.getFileSpec(0);
      filePrefs.saveLastBackupDate
          (fileSpec, masterCollection.getPrefsQualifier(), 0);
      logger.recordEvent (LogEvent.NORMAL,
          "Notes backed up to " + backupFolder.toString(),
            false);
      filePrefs.pruneBackups(folderForBackups, fileNameWithoutDate.toString());
    } else {
      logger.recordEvent (LogEvent.MEDIUM,
          "Problem backing up Notes to " + backupFolder.toString(),
            false);
    }
    return backedUp;
  }
  
  /**
   Return the presumptive folder to be used for backups. 
  
   @return The folder we think the user wishes to use for backups,
           based on his past choices, or on the application defaults.
  */
  private File getBackupFolder() {
    File backupFolder = home.getUserHome();
    if (noteFile != null && noteFile.exists()) {    
      FileSpec fileSpec = masterCollection.getFileSpec(0);
      String backupFolderStr = fileSpec.getBackupFolder();
      File defaultBackupFolder = new File (fileSpec.getFolder(), "backups");
      if (backupFolderStr == null
          || backupFolderStr.length() < 2) {
        backupFolder = defaultBackupFolder;
      } else {
        backupFolder = new File (backupFolderStr);
        if (backupFolder.exists()
            && backupFolder.canWrite()) {
          // leave as-is
        } else {
          backupFolder = defaultBackupFolder;
        }
      }
    }
    return backupFolder;
  }
  
 /**
   Check to see if the user has changed anything and take appropriate
   actions if so.
   */
  public boolean modIfChanged () {
    
    boolean modOK = true;
    
    Note note = position.getNote();
    
    // Check each field for changes
    for (int i = 0; i < noteIO.getNumberOfFields(); i++) {
      DataFieldDefinition fieldDef = noteIO.getRecDef().getDef(i);
      String fieldName = fieldDef.getProperName();
      if (i < editPane.getNumberOfFields()) {
        DataWidget widget = editPane.get(i);
        if (fieldName.equalsIgnoreCase(NoteParms.TITLE_FIELD_NAME)) {
          if (! note.equalsTitle (widget.getText())) {
            oldTitle = note.getTitle();
            note.setTitle (widget.getText());
            modified = true;
          }
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.LINK_FIELD_NAME)) {
          if ((widget.getText().equals (note.getLinkAsString()))
              || ((widget.getText().length() == 0) && note.blankLink())) {
            // No change
          } else {
            note.setLink (widget.getText());
            modified = true;
          }
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.TAGS_FIELD_NAME)) {
          if (! note.equalsTags (widget.getText())) {
            note.setTags (widget.getText());
            modified = true;
          }
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.BODY_FIELD_NAME)) {
          if (! widget.getText().equals (note.getBody())) {
            note.setBody (widget.getText());
            modified = true;
          }
        } 
        else
        if (fieldName.equalsIgnoreCase(NoteParms.SEQ_FIELD_NAME)) {
          if (! widget.getText().equals (note.getSeq())) {
            note.setSeq (widget.getText());
            modified = true;
          }
        } 
        else
        if (fieldName.equalsIgnoreCase(NoteParms.STATUS_FIELD_NAME)) {
          ItemStatus statusValue = new ItemStatus(widget.getText());
          if (note.getStatus().compareTo(statusValue) != 0) {
            note.setStatus (widget.getText());
            modified = true;
          }
        } 
        else
        if (fieldName.equalsIgnoreCase(NoteParms.RECURS_FIELD_NAME)) {
          RecursValue recursValue = new RecursValue(widget.getText());
          if (note.getRecurs().compareTo(recursValue) != 0) {
            note.setRecurs (widget.getText());
            modified = true;
          }
        }  
        else
        if (fieldName.equalsIgnoreCase(NoteParms.DATE_FIELD_NAME)) {
          String newDate = widget.getText();
          if (note.getDateAsString().compareTo(newDate) != 0) {
            note.setDate(newDate);
            modified = true;
          }
        }
        else {
          DataField nextField = note.getField(i);
          if (! widget.getText().equals(nextField.getData())) {
            note.storeField(fieldName, widget.getText());
            modified = true;
          } // end if generic field has been changed
        } // end if generic field
      } // end if we have a widget
    } // end for each field
    
    // If entry has been modified, then let's update if we can
    if (modified) {
      
      // Got to have a title
      String newFileName = note.getFileName();
      if ((! note.hasTitle()) || note.getTitle().length() == 0) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Data Entry Error");
        alert.setContentText
          ("The Note cannot be saved because the Title field has been left blank");
        ButtonType okType = new ButtonType("OK, let me fix it");
        ButtonType cancelType = new ButtonType(
            "Cancel and discard the Note", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okType, cancelType);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okType){
          modOK = true;
        } else {
          modOK = false;
        }
      } 
      else 
      // If we changed the title, then check to see if we have another 
      // Note by the same name
      if ((! newFileName.equals(fileName))
          && noteIO.exists(newFileName)
          && (! newFileName.equalsIgnoreCase(note.getDiskLocationBase()))) {
        Trouble.getShared().report (primaryStage, 
            "A Note already exists with the same Title",
            "Duplicate Found");
        modOK = false;
      } else {
        // Modify newNote on disk
        note.setLastModDateToday();
        saveNoteAndDeleteOnRename(note);
        if (position.isNewNote()) {
          if (note.hasUniqueKey()) {
            addNoteToList ();
          } // end if we have newNote worth adding
        } else {
          noteList.modify(position);
        }
        // noteList.fireTableDataChanged();
        
        if (editingMasterCollection) {
          masterCollection.modRecentFile(oldTitle, note.getTitle());
        }
      }
      oldSeq = "";
    } // end if modified
    
    return modOK;
  } // end modIfChanged method
  
  /**
   Save the note, and if it now has a new disk location, 
   delete the file at the old disk location. 
  
   @param note The note to be saved. 
  */
  private void saveNoteAndDeleteOnRename(Note note) {
    String oldDiskLocation = note.getDiskLocation();
    saveNote(note);
    String newDiskLocation = note.getDiskLocation();
    if (! newDiskLocation.equals(oldDiskLocation)) {
      File oldDiskFile = new File (oldDiskLocation);
      oldDiskFile.delete();
      if (folderSyncPrefs.getSync()) {
        File oldSyncFile = noteIO.getSyncFile(
            folderSyncPrefs.getSyncFolder(), 
            folderSyncPrefs.getSyncPrefix(), 
            oldTitle);
        oldSyncFile.delete();
      }
    }
  }
  
  private void addNoteToList () {
    position = noteList.add (position.getNote());
    if (position.hasValidIndex (noteList)) {
      positionAndDisplay();
    }
  }
  
  private void positionAndDisplay () {
    /*
    if (position.getIndex() >= 0
        && position.getIndex() < noteList.size()
        && position.getIndex() != noteTable.getSelectedRow()) {
      noteTable.setRowSelectionInterval
          (position.getIndex(), position.getIndex());
      noteTable.scrollRectToVisible
          ((noteTable.getCellRect(position.getIndex(), 0, false)));
    } 
    if (position.getTagsNode() != null
        && position.getTagsNode()
        != noteTree.getLastSelectedPathComponent()) {
      TreePath path = new TreePath(position.getTagsNode().getPath());
      noteTree.setSelectionPath (path);
      noteTree.scrollPathToVisible (path);
    }
    displayNote ();
*/
  }
  
  private void ioException(IOException e) {
    Trouble.getShared().report("I/O Exception", "Trouble");
  }
  
}
