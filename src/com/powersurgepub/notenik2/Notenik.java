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
  import com.powersurgepub.psutils2.textio.*;
  import com.powersurgepub.psutils2.textmerge.*;
  import com.powersurgepub.psutils2.txmin.*;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;
  import com.powersurgepub.psutils2.widgets.*;

  import java.io.*;
  import java.net.*;
  import java.text.*;
  import java.util.*;

  import javafx.application.*;
  import javafx.beans.value.*;
  import javafx.collections.*;
  import javafx.concurrent.*;
  import javafx.event.*;
  import javafx.geometry.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.control.Alert.*;
  import javafx.scene.control.ButtonBar.*;
  import javafx.scene.control.TableView.*;
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
      DateWidgetOwner,
      DisplayWindow,
      FileSpecOpener,
      LinkTweakerApp,
      PublishAssistant,
      ScriptExecutor,
      TagsChangeAgent {
  
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
  
  private             Appster             appster;
  private             Home                home;
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
  private             boolean             fileOpen = false;
  private             boolean             noteDisplayed = false;
  
  /** This is the current collection of Notes. */
  private             NoteList            noteList = null;
  
  private             Stage               primaryStage;
  private             VBox                primaryLayout;
  private             Scene               primaryScene;
  private             FXUtils             fxUtils;
  
  /*
   Menu Definitions
  */
  private             MenuBar             menuBar;
  
  private             Menu                fileMenu        = new Menu("File");
  private             MenuItem            openMenuItem;
  private             Menu                openRecentMenu;
  private             MenuItem            openEssentialMenuItem;
  private             MenuItem            openMasterCollectionMenuItem;
  private             MenuItem            createMasterCollectionMenuItem;
  private             MenuItem            openHelpNotesMenuItem;
  private             MenuItem            fileNewMenuItem;
  private             MenuItem            generateTemplateMenuItem;
  private             MenuItem            fileSaveMenuItem;
  private             MenuItem            saveAllMenuItem;
  private             MenuItem            fileSaveAsMenuItem;
  private             MenuItem            fileBackupMenuItem;
  private             MenuItem            reloadMenuItem;
  private             MenuItem            reloadTaggedMenuItem;
  private             MenuItem            publishWindowMenuItem;
  private             MenuItem            publishNowMenuItem;
  private             Menu                importMenu;
  private             MenuItem            importMacAppInfo;
  private             MenuItem            importNotenikMenuItem;
  private             MenuItem            importTabDelimitedMenuItem;
  private             MenuItem            importXMLMenuItem;
  private             Menu                exportMenu;
  private             MenuItem            exportNotenikMenuItem;
  private             MenuItem            exportOPML;
  private             MenuItem            exportTabDelimitedMenuItem;
  private             MenuItem            exportTabDelimitedMSMenuItem;
  private             MenuItem            exportXMLMenuItem;
  
  private             MenuItem            purgeMenuItem;
  
  private             Menu                collectionMenu  = new Menu("Collection");
  
  private             Menu                sortMenu        = new Menu("Sort");
  
  private             Menu                noteMenu        = new Menu("Note");
  private             MenuItem            closeNoteMenuItem;
  private             MenuItem            nextMenuItem;
  private             MenuItem            priorMenuItem;
  
  private             Menu                editMenu        = new Menu("Edit");
  
  private             Menu                toolsMenu       = new Menu("Tools");
  
  private             Menu                reportsMenu     = new Menu("Reports");
  
  private             Menu                optionsMenu     = new Menu("Options");
  
  private             Menu                windowMenu      = new Menu("Window");
  
  private             Menu                helpMenu        = new Menu("Help");
  
  /*
   Toolbar Definitions
  */
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
  
  /*
   Window Definitions
  */
  private             SplitPane           mainSplitPane;
  
  private             TabPane             collectionTabs;
  public static final int                   LIST_TAB_INDEX = 0;
  public static final int                   TAGS_TAB_INDEX = 1;
  private             Tab                 listTab;
  private             BorderPane          listPane;
  private             TableView           noteTable;
  private             TableViewSelectionModel<SortedNote> selModel;
  private             ObservableList<Integer> selected;
  
  private             Tab                 tagsTab;
  private             BorderPane          treePane;
  private             TreeView            noteTree;
  private             TagsView            tagsView;
  
  private             TabPane             noteTabs;
  public static final int                   DISPLAY_TAB_INDEX = 0;
  public static final int                   EDIT_TAB_INDEX = 1;
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
  
  /*
   Preferences Definitions
  */
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
  private             DirectoryChooser    dirChooser = new DirectoryChooser();

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
  private             ProgressWindow      progressWindow;
  
  // System ClipBoard fields
  boolean             clipBoardOwned = false;
  Clipboard           clipBoard = null;
  // Transferable        clipContents = null;
  
  private             TextMergeHarness    textMerge = null;
  
  @Override
  public void start(Stage primaryStage) {
    
    this.primaryStage = primaryStage;
    primaryStage.setTitle("Notenik");
    primaryLayout = new VBox();
    fxUtils = FXUtils.getShared();
    
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
    aboutWindow = new AboutWindow(
      primaryStage, 
      false,   // loadFromDisk, 
      true,    // jxlUsed,
      true,    // Markdown converter Used,
      true,    // xerces used
      true,    // saxon used
      "2009"); // copyRightYearFrom
    
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

    // Set initial UI prefs
    generalPrefs.setSplitPane(mainSplitPane);
    generalPrefs.setMainWindow(primaryStage);
    
    // Get App Folder
    appFolder = home.getAppFolder();
    if (appFolder == null) {
      trouble.report ("The " + home.getProgramName()
          + " Folder could not be found",
          "App Folder Missing");
    } else {
      Logger.getShared().recordEvent (LogEvent.NORMAL,
        "App Folder = " + appFolder.toString(),
        false);
    }
    
    publishWindow = new PublishWindow(this);
    publishWindow.setOnSaveOption(false);
    publishWindow.setStatusBar(statusBar);
    
    replaceWindow = new ReplaceWindow(this);
    
    linkTweaker = new LinkTweaker(this, tweakerPrefs, primaryStage);
    
    fileInfoWindow = new FileInfoWindow(this);
    
    displayPane = new DisplayPane(displayPrefs);
    
    // Get System Properties
    userName = System.getProperty ("user.name");
    userDirString = System.getProperty (GlobalConstants.USER_DIR);
    Logger.getShared().recordEvent (LogEvent.NORMAL,
      "User Directory = " + userDirString,
      false);

    // Write some basic data about the run-time environment to the log
    Logger.getShared().recordEvent (LogEvent.NORMAL,
        "Java Virtual Machine = " + System.getProperty("java.vm.name") +
        " version " + System.getProperty("java.vm.version") +
        " from " + StringUtils.removeQuotes(System.getProperty("java.vm.vendor")),
        false);
    if (Home.runningOnMac()) {
      Logger.getShared().recordEvent (LogEvent.NORMAL,
          "Mac Runtime for Java = " + System.getProperty("mrj.version"),
          false);
    }
    Runtime runtime = Runtime.getRuntime();
    runtime.gc();
    NumberFormat numberFormat = NumberFormat.getInstance();
    Logger.getShared().recordEvent (LogEvent.NORMAL,
        "Available Memory = " + numberFormat.format (Runtime.getRuntime().freeMemory()),
        false);
    
    // Automatically open the last file opened, if any

    FileSpec lastFileSpec = filePrefs.getStartupFileSpec();
    String lastFolderString = filePrefs.getStartupFilePath();
    String lastTitle = "";
    if (lastFolderString != null
        && lastFolderString.length() > 0) {
      File lastFolder = new File (lastFolderString);
      if (goodCollection(lastFolder)) {
        if (lastFileSpec != null) {
          lastTitle = lastFileSpec.getLastTitle();
        }
        openFile (lastFolder, lastTitle, true);
        if (favoritesPrefs.isOpenStartup()) {
          launchStartupURLs();
        }
      }
    }
    
    // Now let's bring the curtains up
    primaryStage.setScene(primaryScene);
    primaryStage.setWidth
        (userPrefs.getPrefAsDouble (FavoritesPrefs.PREFS_WIDTH, 620));
    primaryStage.setHeight
        (userPrefs.getPrefAsDouble (FavoritesPrefs.PREFS_HEIGHT, 620));
    primaryStage.setX
        (userPrefs.getPrefAsDouble (FavoritesPrefs.PREFS_LEFT, 100));
    primaryStage.setY
      (userPrefs.getPrefAsDouble (FavoritesPrefs.PREFS_TOP,  100));
    primaryStage.show();
    
    WindowMenuManager.getShared().hide(logWindow);
  }
  
  /**
   Build all the menu items.
  */
  private void buildMenuBar() {
    menuBar = new MenuBar();
    menuBar.setUseSystemMenuBar(true);
    menuBar.getMenus().addAll(fileMenu, collectionMenu, sortMenu, noteMenu,
        editMenu, toolsMenu, reportsMenu, optionsMenu, windowMenu, helpMenu);
    menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
    primaryLayout.getChildren().add(menuBar);
    
    //
    // Let's build out the file Menu
    //
    
    // Open Menu Item
    openMenuItem = new MenuItem("Open...");
    KeyCombination okc
        = new KeyCharacterCombination("O", KeyCombination.SHORTCUT_DOWN);
    openMenuItem.setAccelerator(okc);
    openMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        if (editingMasterCollection) {
          openFileFromCurrentNote();
        } else {
          userOpenFile();
        }
      }
    });
    fileMenu.getItems().add(openMenuItem);
    
    // Open Essential Menu Item
    openEssentialMenuItem = new MenuItem("Open Essential Collection");
    KeyCombination ekc
        = new KeyCharacterCombination("E", KeyCombination.SHORTCUT_DOWN);
    openEssentialMenuItem.setAccelerator(ekc);
    openEssentialMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        openEssentialCollection();
      }
    });
    fileMenu.getItems().add(openEssentialMenuItem);
    
    // Open Recent Menu Item
    openRecentMenu = new Menu("Open Recent");
    fileMenu.getItems().add(openRecentMenu);
    
    // Open Master Collection Menu Item
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
    
    // Create Master Collection Menu Item
    createMasterCollectionMenuItem = new MenuItem();
    createMasterCollectionMenuItem.setText("Create Master Collection...");
    createMasterCollectionMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        createMasterCollection();
      }
    });
    fileMenu.getItems().add(createMasterCollectionMenuItem);
    
    // Open Help Notes Menu Item
    openHelpNotesMenuItem = new MenuItem("Open Help Notes");
    openHelpNotesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        openHelpNotes();
      }
    });
    fileMenu.getItems().add(openHelpNotesMenuItem);
    
    fxUtils.addSeparator(fileMenu);
    
    // New Collection Menu Item
    fileNewMenuItem = new MenuItem("New...");
    fileNewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        userNewFile();
      }
    });
    fileMenu.getItems().add(fileNewMenuItem);
    
    // Generate Template Menu Item
    generateTemplateMenuItem = new MenuItem("Generate Template...");
    generateTemplateMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        generateTemplate();
      }
    });
    fileMenu.getItems().add(generateTemplateMenuItem);
    
    fxUtils.addSeparator(fileMenu);
    
    // Save Menu Item
    fileSaveMenuItem = new MenuItem("Save");
    KeyCombination skc
        = new KeyCharacterCombination("S", KeyCombination.SHORTCUT_DOWN);
    fileSaveMenuItem.setAccelerator(skc);
    fileSaveMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        doneEditing();
      }
    });
    fileMenu.getItems().add(fileSaveMenuItem);
    
    // Save All Menu Item
    saveAllMenuItem = new MenuItem("Save All");
    saveAllMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        saveAll();
      }
    });
    fileMenu.getItems().add(saveAllMenuItem);
    
    // Save As Menu Item
    fileSaveAsMenuItem = new MenuItem("Save As...");
    fileSaveAsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        userSaveFileAs();
      }
    });
    fileMenu.getItems().add(fileSaveAsMenuItem);
    
    // Backup Menu Item
    fileBackupMenuItem = new MenuItem("Backup...");
    fileBackupMenuItem.setOnAction(e -> 
      {
        if (noteFile != null 
            && noteFile.exists()
            && noteList != null
            && noteList.size() > 0) {
          promptForBackup();
        } else {
          trouble.report(
              primaryStage, 
              "Open a Notes folder before attempting a backup", 
              "Backup Error",
              AlertType.ERROR);
        }
      });
    fileMenu.getItems().add(fileBackupMenuItem);
    
    fxUtils.addSeparator(fileMenu);
    
    // Reload Menu Item
    reloadMenuItem = new MenuItem("Reload");
    reloadMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        reloadFile();
      }
    });
    fileMenu.getItems().add(reloadMenuItem);
    
    // Reload without Untagged Notes
    reloadTaggedMenuItem = new MenuItem("Reload w/o Untagged");
    reloadTaggedMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        reloadTaggedOnly();
      }
    });
    fileMenu.getItems().add(reloadTaggedMenuItem);
    
    fxUtils.addSeparator(fileMenu);
    
    // Purge Menu Item
    purgeMenuItem = new MenuItem("Purge...");
    purgeMenuItem.setOnAction(e -> purgeMenuItemActionPerformed());
    fileMenu.getItems().add(purgeMenuItem);
    
    fxUtils.addSeparator(fileMenu);
    
    // Publish Window Menu Item
    publishWindowMenuItem = new MenuItem("Publish...");
    KeyCombination pkc
        = new KeyCharacterCombination("P", KeyCombination.SHORTCUT_DOWN);
    publishWindowMenuItem.setAccelerator(pkc);
    publishWindowMenuItem.setOnAction(e -> displayPublishWindow());
    fileMenu.getItems().add(publishWindowMenuItem);
    
    // Publish Now Menu Item
    publishNowMenuItem = new MenuItem("Publish Now");
    publishNowMenuItem.setOnAction(e -> publishWindow.publishNow());
    fileMenu.getItems().add(publishNowMenuItem);
    
    // Import Menu Items
    importMenu = new Menu("Import");
    fileMenu.getItems().add(importMenu);
    
    importMacAppInfo = new MenuItem("Mac App Info...");
    importMacAppInfo.setOnAction(e -> importMacAppInfo());
    importMenu.getItems().add(importMacAppInfo);
    
    importNotenikMenuItem = new MenuItem("Notenik...");
    importNotenikMenuItem.setOnAction(e -> importFile());
    importMenu.getItems().add(importNotenikMenuItem);
    
    importTabDelimitedMenuItem = new MenuItem("Tab-Delimited...");
    importTabDelimitedMenuItem.setOnAction(e -> importTabDelimited());
    importMenu.getItems().add(importTabDelimitedMenuItem);
    
    importXMLMenuItem = new MenuItem("XML...");
    importXMLMenuItem.setOnAction(e -> importXMLFile());
    importMenu.getItems().add(importXMLMenuItem);
    
    // Export Menu Items
    exportMenu = new Menu("Export");
    fileMenu.getItems().add(exportMenu);
    
    exportNotenikMenuItem = new MenuItem("Notenik...");
    exportNotenikMenuItem.setOnAction
        (e -> generalExport(NoteExport.NOTENIK_EXPORT));
    exportMenu.getItems().add(exportNotenikMenuItem);
    
    exportOPML = new MenuItem("OPML...");
    exportOPML.setOnAction(e -> generalExport(NoteExport.OPML_EXPORT));
    exportMenu.getItems().add(exportOPML);
    
    exportTabDelimitedMenuItem = new MenuItem("Tab-Delimited...");
    exportTabDelimitedMenuItem.setOnAction
        (e -> generalExport(NoteExport.TABDELIM_EXPORT));
    exportMenu.getItems().add(exportTabDelimitedMenuItem);
    
    exportTabDelimitedMSMenuItem = new MenuItem("Tab-Delimited for MS Links");
    exportTabDelimitedMSMenuItem.setOnAction
        (e -> generalExport(NoteExport.TABDELIM_EXPORT_MS_LINKS));
    exportMenu.getItems().add(exportTabDelimitedMSMenuItem);
    
    exportXMLMenuItem = new MenuItem("XML...");
    exportXMLMenuItem.setOnAction
        (e -> generalExport(NoteExport.XML_EXPORT));
    exportMenu.getItems().add(exportXMLMenuItem);
    
    //
    // Let's build out the Note Menu
    //
    
    // Next Note Menu Item
    nextMenuItem = new MenuItem("Go to Next Note");
    KeyCombination nextkc
        = new KeyCharacterCombination("]", KeyCombination.SHORTCUT_DOWN);
    nextMenuItem.setAccelerator(nextkc);
    nextMenuItem.setOnAction (e -> nextNote());
    noteMenu.getItems().add(nextMenuItem);
    
    // Prior Note Menu Item
    priorMenuItem = new MenuItem("Go to Previous Note");
    KeyCombination priorkc
        = new KeyCharacterCombination("[", KeyCombination.SHORTCUT_DOWN);
    priorMenuItem.setAccelerator(priorkc);
    priorMenuItem.setOnAction (e -> priorNote());
    noteMenu.getItems().add(priorMenuItem);
    
    // Close Note Menu Item
    closeNoteMenuItem = new MenuItem("Close Note");
    KeyCombination kkc
        = new KeyCharacterCombination("K", KeyCombination.SHORTCUT_DOWN);
    closeNoteMenuItem.setAccelerator(kkc);
    closeNoteMenuItem.setOnAction(e -> closeNoteMenuItemActionPerformed());
    noteMenu.getItems().add(closeNoteMenuItem);
    

    
    
  }
  
  private void purgeMenuItemActionPerformed() {                                              
    if (editPane.statusIsIncluded()) {
      purge();
    }
  } 
  
  private void closeNoteMenuItemActionPerformed() {                                                  
    if (editPane.statusIsIncluded() 
        || (editPane.dateIsIncluded() && editPane.recursIsIncluded())) {
      closeNote();
    }
  }
  
  /**
   Build the toolbar.
  */
  private void buildToolBar() {
    toolBar = new ToolBar();
    
    okButton = new Button("OK");
    okButton.setTooltip(new Tooltip("Complete your entries for this note"));
    okButton.setOnAction( e -> doneEditing() );
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
    priorButton.setOnAction(e -> priorNote());
    toolBar.getItems().add(priorButton);
        
    nextButton = new Button(">");
    nextButton.setTooltip(new Tooltip("Go to the next note"));
    nextButton.setOnAction(e -> nextNote());
    toolBar.getItems().add(nextButton);
        
    lastButton = new Button(">>");
    lastButton.setTooltip(new Tooltip("Go to the last note"));
    toolBar.getItems().add(lastButton);
        
    launchButton = new Button("Launch");
    launchButton.setTooltip(new Tooltip("Open this Link in your Web Browser"));
    launchButton.setOnAction( e -> launchButtonClicked() );
    toolBar.getItems().add(launchButton);
        
    findText = new TextField("");
    findText.setTooltip(new Tooltip("Enter some text you'd like to find"));
    findText.textProperty().addListener(this::findTextChanged);
    findText.setOnAction(e -> findTextAction());
    toolBar.getItems().add(findText);
        
    findButton = new Button("Find");
    findButton.setTooltip(new Tooltip("Find the text you entered"));
    findButton.setOnAction(e -> findNote());
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
    VBox.setVgrow(mainSplitPane, Priority.ALWAYS);
  }
  
  /**
   Build the user interface to view the entire collection as a list or
   an outline. 
  */
  private void buildCollectionTabs() {
    noteTable = noteList.getTable();
    selModel = noteTable.getSelectionModel();
    selected = selModel.getSelectedIndices();
    selected.addListener((ListChangeListener.Change<? extends Integer> change) ->
      {
        tableRowSelected();
      });
    listPane = new BorderPane();
    listPane.setCenter(noteTable);
    listTab.setContent(listPane);
    
    treePane = new BorderPane();
    noteTree = noteList.getTreeView();
    treePane.setCenter(noteTree);
    tagsTab.setContent(treePane);
    
  } // end method buildCollectionTabs
  
  /**
   Build the user interface to view and update one Note. 
  */
  private void buildNoteTabs() {
    
    displayPane = new DisplayPane(displayPrefs);
    displayTab.setContent(displayPane.getPane());
    
    editPane = new EditPane();
    editPane.addWidgets(noteList, noteIO, linkTweaker, this, primaryStage);
    editTab.setContent(editPane.getPane());
    
    // noteTabs.getTabs().removeAll();
    // noteTabs.getTabs().add(displayTab);
    // noteTabs.getTabs().add(editTab);

    // mainSplitPane.setRightComponent(notePanel);
    // mainSplitPane.setResizeWeight(0.5);

    purgeMenuItem.setDisable(! editPane.statusIsIncluded());
    closeNoteMenuItem.setDisable(! editPane.statusIsIncluded()
        || (editPane.dateIsIncluded() && editPane.recursIsIncluded()));
  } // end method buildNoteTabs

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
    fileOpen = false;
    noteDisplayed = false;
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
      chooser.setTitle ("Pick a Backups Folder");
      FileName noteFileName = new FileName (home.getUserHome());
      if (noteFile != null && noteFile.exists()) {
        noteFileName = new FileName (noteFile);
      }
      File initialFolder = getBackupFolder();
      chooser.setInitialDirectory (initialFolder);
      File selectedFolder = chooser.showDialog (primaryStage);
      if (selectedFolder != null) {
        File backupFolder = selectedFolder;
        backedUp = backup (backupFolder);
        FileSpec fileSpec = masterCollection.getFileSpec(0);
        fileSpec.setBackupFolder(backupFolder);
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
    
    File userHome = home.getUserHome();
    File userDocs = home.getUserDocs();   
    File embeddedBackupFolder = null;    
    String lastBackupFolderStr = "";
    File lastBackupFolder = null;
    
    if (noteFile != null && noteFile.exists()) {    
      FileSpec fileSpec = masterCollection.getFileSpec(0);
      embeddedBackupFolder = new File (fileSpec.getFolder(), "backups");
      lastBackupFolderStr = fileSpec.getBackupFolder();
      if (lastBackupFolderStr != null
          && lastBackupFolderStr.length() > 1) {
        lastBackupFolder = new File(lastBackupFolderStr);
      }
    }
    File backupFolder;
    if (goodBackupFolder(lastBackupFolder)) {
      backupFolder = lastBackupFolder;
    }
    else
    if (goodBackupFolder(embeddedBackupFolder)) {
      backupFolder = embeddedBackupFolder;
    } else 
    if (goodBackupFolder(userHome)) {
      backupFolder = userHome;
    } else {
      backupFolder = userDocs;
    }
    return backupFolder;
  }

  private boolean goodBackupFolder(File backupFolder) {
    return (backupFolder != null
        && backupFolder.exists() 
        && backupFolder.canWrite()
        && backupFolder.isDirectory());
  }
  
 /**
   Check to see if the user has changed anything and take appropriate
   actions if so.
   */
  public boolean modIfChanged () {
    
    boolean modOK = true;
    
    if (fileOpen && noteDisplayed) {
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
    }
    
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
  
  private void ioException(IOException e) {
    Trouble.getShared().report("I/O Exception", "Trouble");
  }
  
  public boolean preferencesAvailable() {
    return true;
  }

  /**
   Prepare the data entry screen for a new Note.
   */
  private void newNote() {

    // Capture current category selection, if any
    String selectedTags = "";
    /* Fix Later
    TagsNode tags = (TagsNode)noteTree.getLastSelectedPathComponent();
    if (tags != null) {
      selectedTags = tags.getTagsAsString();
    } */
    
    DataValueSeq newSeq = null;
    if (noteSortParm.getParm() == NoteSortParm.SORT_BY_SEQ_AND_TITLE
        && position != null
        && position.getNote() != null
        && noteList.atEnd(position)) {
      newSeq = new DataValueSeq(position.getNote().getSeq());
      newSeq.increment(false);
    }
    
    boolean modOK = modIfChanged();

    if (modOK) {
      position = new NotePositioned(noteIO.getNoteParms());
      position.setIndex (noteList.size());
      fileName = "";
      boolean seqSet = false;
      if (oldSeq != null && oldSeq.length() > 0) {
        position.getNote().setSeq(oldSeq);
        seqSet = true;
      }
      else
      if (newSeq != null) {
        position.getNote().setSeq(newSeq.toString());
        seqSet = true;
      }
      displayNote();
      editPane.setTags(selectedTags);
      if (seqSet) {
        editPane.setSeq(position.getNote().getSeq());
      }
      noteTabs.getSelectionModel().select(EDIT_TAB_INDEX);
      oldSeq = "";
    }
  }
  
  /**
   Add one note if the list is empty. 
  */
  private void addFirstNoteIfListEmpty() {
    if (noteList.size() == 0) {
      addFirstNote();
    }
  }

  /**
   Add the first Note for a new collection.
   */
  private void addFirstNote() {
    position = new NotePositioned(noteIO.getRecDef());
    position.setIndex (noteList.size());

    Note note = position.getNote();
    note.setTitle("Notenik.net");
    note.setLink("http://www.notenik.net/");
    note.setTags("Software.Java.Groovy");
    note.setBody("Home to Notenik");

    saveNote(note);
    addNoteToList();
    // noteList.fireTableDataChanged();

    modified = false;
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
   Duplicate the currently displayed event.
   */
  public void duplicateNote() {

    boolean modOK = modIfChanged();

    if (modOK) {
      Note note = position.getNote();
      Note newNote = new Note(note);
      newNote.setTitle(note.getTitle() + " copy");
      position = new NotePositioned(noteIO.getRecDef());
      position.setIndex (noteList.size());
      position.setNote(newNote);
      position.setNewNote(true);
      fileName = "";
      displayNote();
    }
  }

  private void removeNote () {
    if (position.isNewNote()) {
      System.out.println ("New Note -- ignoring delete command");
    } else {
      boolean okToDelete = true;
      if (generalPrefs.confirmDeletes()) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Really delete Note titled " 
            + position.getNote().getTitle() + "?");
        Optional<ButtonType> result = alert.showAndWait();
        okToDelete = (result.get() == ButtonType.OK);
      }
      if (okToDelete) {
        noFindInProgress();
        Note noteToDelete = position.getNote();
        String fileToDelete = noteToDelete.getDiskLocation();
        position.setNavigatorToList
            (collectionTabs.getSelectionModel().getSelectedIndex() == 0);
        position = noteList.remove (position);
        boolean deleted = new File(fileToDelete).delete();
        if (! deleted) {
          trouble.report(
              "Unable to delete note at " + position.getNote().getFileName(), 
              "Delete Failure");
        }
        
        if (deleted && editingMasterCollection) {
          masterCollection.removeRecentFile(noteToDelete.getTitle());
        }
        
        if (folderSyncPrefs.getSync()) {
          File syncFile = noteIO.getSyncFile(
              folderSyncPrefs.getSyncFolder(), 
              folderSyncPrefs.getSyncPrefix(), 
              noteToDelete.getTitle());
          syncFile.delete();
        }
        // noteList.fireTableDataChanged();
        positionAndDisplay();
      } // end if user confirmed delete
    } // end if new URL not yet saved
  } // end method removeNote

  private void checkTags() {
    boolean modOK = modIfChanged();

    if (modOK) {
      TagsChangeScreen replaceScreen = new TagsChangeScreen
          (primaryStage, true, noteList.getTagsList(), this);
      replaceScreen.setLocation (
          primaryStage.getX() + CHILD_WINDOW_X_OFFSET,
          primaryStage.getY() + CHILD_WINDOW_Y_OFFSET);
      replaceScreen.setVisible (true);
      // setUnsavedChanges (true);
      // catScreen.show();
    }
  }

  /**
   Called from TagsChangeScreen.
   @param from The from String.
   @param to   The to String.
   */
  public void changeAllTags (String from, String to) {

    boolean modOK = modIfChanged();

    if (modOK) {
      NotePositioned workNote = new NotePositioned (noteIO.getRecDef());
      int mods = 0;
      for (int workIndex = 0; workIndex < noteList.size(); workIndex++) {
        workNote.setNote (noteList.get (workIndex));
        workNote.setIndex (workIndex);
        String before = workNote.getNote().getTags().toString();
        workNote.getNote().getTags().replace (from, to);
        if (! before.equals (workNote.getNote().getTags().toString())) {
          mods++;
          noteList.modify(workNote);
          saveNote(workNote.getNote());
        }
      }
      
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Tags Replacement Results");
      alert.setHeaderText(null);
      alert.setContentText(String.valueOf (mods)
            + " tags changed");
      alert.showAndWait();

      displayNote();
    }
  }

  private void flattenTags() {
    boolean modOK = modIfChanged();

    if (modOK) {
      NotePositioned workNote = new NotePositioned(noteIO.getRecDef());
      for (int workIndex = 0; workIndex < noteList.size(); workIndex++) {
        workNote.setNote (noteList.get (workIndex));
        workNote.getNote().flattenTags();
        noteList.modify(workNote);
      }
      noFindInProgress();
      displayNote();
    }
  }

  private void lowerCaseTags() {
    boolean modOK = modIfChanged();

    if (modOK) {
      NotePositioned workNote = new NotePositioned(noteIO.getRecDef());
      for (int workIndex = 0; workIndex < noteList.size(); workIndex++) {
        workNote.setNote (noteList.get (workIndex));
        workNote.getNote().lowerCaseTags();
        noteList.modify(workNote);
      }
      noFindInProgress();
    }
  }

  public int checkTags (String find, String replace) {
    int mods = 0;
    Note next;
    Tags tags;
    String tag;
    for (int i = 0; i < noteList.size(); i++) {
      next = noteList.get(i);
      tags = next.getTags();
      boolean tagsModified = false;
      if (find.equals("")) {
        tags.merge (replace);
        tagsModified = true;
      } else {
        TagsIterator iterator = new TagsIterator (tags);
        while (iterator.hasNextTag() && (! tagsModified)) {
          tag = iterator.nextTag();
          if (tag.equalsIgnoreCase (find)) {
            iterator.removeTag();
            if (replace.length() > 0) {
              tags.merge (replace);
            }
            tagsModified = true;
          }
        } // end while this item has more categories
      } // end if we the find category is not blank
      if (tagsModified) {
        mods++;
        saveNote(next);
        // setUnsavedChanges (true);
      } // end if tags modified
    } // end of  items
    return mods;
  } 
  
  /**
   If requested, launch any Note's link that has been tagged with "startup"
  */
  private void launchStartupURLs() {
    Note next;
    Tags tags;
    String tag;
    for (int i = 0; i < noteList.size(); i++) {
      next = noteList.get(i);
      tags = next.getTags();
      TagsIterator iterator = new TagsIterator (tags);
      while (iterator.hasNextTag()) {
        tag = iterator.nextTag();
        if (tag.equalsIgnoreCase("Startup")) {
          openURL(next.getLinkAsString());
        }
      }
    }
  }
  
  private void startReplace() {
    replaceWindow.startReplace(findText.getText());
    displayAuxiliaryWindow(replaceWindow);
  }
  
  /**
    Replace all occurrences of the given text string with a 
    specified replacement.
   
    @param findString    The string we're searching for. 
    @param replaceString The string to replace the find string. 
    @param checkTitle    Should we check the title of the URL item?
    @param checkLink      Should we check the URL of the URL item?
    @param checkTags     Should we check the tags of the URL item?
    @param checkBody Should we check the comments?
    @param caseSensitive Should we do a case-sensitive comparison?
  
  */
  public int replaceAll (String findString, String replaceString, 
      boolean checkTitle, 
      boolean checkLink, 
      boolean checkTags, 
      boolean checkBody,
      boolean caseSensitive) {
    
    int itemsChanged = 0;
    boolean found = true;
    findButton.setText(FIND);
    while (found) {
      found = findNote (
        findButton.getText(),
        findString, 
        checkTitle, 
        checkLink, 
        checkTags,
        checkBody,
        caseSensitive,
        false);
      if (found) {
        boolean replaced = replaceNote(
            findString, 
            replaceString, 
            checkTitle, 
            checkLink, 
            checkTags, 
            checkBody);
        if (replaced) {
          itemsChanged++;
        }
      } // end if another item found
    } // end while more matching Notes found
    
    
    if (itemsChanged == 0) {
      PSOptionPane.showMessageDialog(primaryStage,
          "No matching Notes found",
          "OK",
          javax.swing.JOptionPane.WARNING_MESSAGE);
      statusBar.setStatus("No Notes found");
    } else {
      PSOptionPane.showMessageDialog(primaryStage,
        String.valueOf (itemsChanged)
          + " Notes modified",
        "Replacement Results",
        javax.swing.JOptionPane.INFORMATION_MESSAGE);
      statusBar.setStatus(String.valueOf(itemsChanged) + " Notes modified");
    }
    
    return itemsChanged;
    
  } // end replaceAll method
  
  /**
   The user changed the value of the search field in the tool bar. 
  
   @param prop      The Observable value
   @param oldValue  The prior value
   @param newValue  The new value
  */
  private void findTextChanged(ObservableValue<? extends String> prop, 
	                    String oldValue, 
	                    String newValue) {
    if (! findText.getText().equals (lastTextFound)) {
      noFindInProgress();
    }
	}
  
  /**
   The user hit the Return / Enter key while in the tool bar search field. 
  */
  private void findTextAction() {
    findNote();
  }

  /**
    Find the next URL item containing the search string, or position the cursor
    on the search string, if it is currently empty. 
  */
  private void findNote () {

    findNote (
      findButton.getText(), 
      findText.getText().trim(), 
      replaceWindow.titleSelected(), 
      replaceWindow.linkSelected(), 
      replaceWindow.tagsSelected(),
      replaceWindow.bodySelected(),
      replaceWindow.caseSensitive(),
      true);
      
    if (findText.getText().trim().length() == 0) {
      findText.requestFocus();
      statusBar.setStatus("Enter a search string");
    }
  }
  
  /**
    Find the specified text string within the list of URL items. This method may
    be called internally, or from the ReplaceWindow. The result will be to 
    position the displays on the item found, or display a message to the user
    that no matching item was found. 
  
    @param findButtonText Either "Find" or "Again", indicating whether we
                          are starting a new search or continuing an 
                          existing one. 
    @param findString  The string we're searching for. 
    @param checkTitle  Should we check the title of the URL item?
    @param checkLink    Should we check the URL of the URL item?
    @param checkTags   Should we check the tags of the URL item?
    @param checkBody Should we check the comments?
    @param caseSensitive Should we do a case-sensitive comparison?
    @param showDialogAtEnd Show a dialog to user when no remaining Notes found?
  */
  public boolean findNote (
      String findButtonText, 
      String findString, 
      boolean checkTitle, 
      boolean checkLink, 
      boolean checkTags,
      boolean checkBody,
      boolean caseSensitive,
      boolean showDialogAtEnd) {
        
    boolean modOK = modIfChanged();
    boolean found = false;
    if (modOK) {
      String notFoundMessage;
      if (findString != null && findString.length() > 0) {
        if (findButtonText.equals (FIND)) {
          notFoundMessage = "No Notes Found";
          position.setIndex (-1);
        } else {
          notFoundMessage = "No further Notes Found";
        }
        position.incrementIndex (1);
        String findLower = findString.toLowerCase();
        String findUpper = findString.toUpperCase();
        while (position.hasValidIndex(noteList) && (! found)) {
          Note noteCheck = noteList.get (position.getIndex());
          found = findWithinNote(
              noteCheck,
              findString, 
              checkTitle, 
              checkLink, 
              checkTags,
              checkBody,
              caseSensitive,
              findLower,
              findUpper);
          if (found) {
            foundNote = noteCheck;
          } else {
            position.incrementIndex (1);
          }
        } // while still looking for next match
        if (found) {
          findInProgress();
          lastTextFound = findString;
          position = noteList.positionUsingListIndex (position.getIndex());
          positionAndDisplay();
          statusBar.setStatus("Matching Note found");
        } else {
          PSOptionPane.showMessageDialog(primaryStage,
              notFoundMessage,
              "Not Found",
              javax.swing.JOptionPane.WARNING_MESSAGE);
          noFindInProgress();
          lastTextFound = "";
          statusBar.setStatus(notFoundMessage);
          foundNote = null;
        }
      } // end if we've got a find string
    } // end if mods ok
    return found;
  } // end method findNote
  
  /**
    Check for a search string within the given Note Item. 

    @param noteToSearch The Note item to be checked. 
    @param findString  The string we're searching for. 
    @param checkTitle  Should we check the title of the URL item?
    @param checkLink    Should we check the URL of the URL item?
    @param checkTags   Should we check the tags of the URL item?
    @param checkBody Should we check the comments?
    @param caseSensitive Should we do a case-sensitive comparison?
    @param findLower   The search string in all lower case.
    @param findUpper   The search string in all upper case. 
    @return True if an item containing the search string was found. 
  */
  private boolean findWithinNote(
      Note noteToSearch, 
      String findString, 
      boolean checkTitle, 
      boolean checkLink, 
      boolean checkTags,
      boolean checkBody,
      boolean caseSensitive,
      String findLower,
      String findUpper) {
    
    boolean found = false;
    
    if (checkTitle) {
      titleBuilder = new StringBuilder(noteToSearch.getTitle());
      if (caseSensitive) {
        titleStart = titleBuilder.indexOf(findString);
      } else {
        titleStart = StringUtils.indexOfIgnoreCase (findLower, findUpper,
            noteToSearch.getTitle(), 0);
      }
      if (titleStart >= 0) {
        found = true;
      }
    }

    if (checkLink) {
      linkBuilder = new StringBuilder(noteToSearch.getLinkAsString());
      if (caseSensitive) {
        linkStart = linkBuilder.indexOf(findString);
      } else {
        linkStart = StringUtils.indexOfIgnoreCase (findLower, findUpper,
            noteToSearch.getLinkAsString(), 0);
      }
      if (linkStart >= 0) {
        found = true;
      }
    }
    
    if (checkTags) {
      tagsBuilder = new StringBuilder(noteToSearch.getTagsAsString());
      if (caseSensitive) {
        tagsStart = tagsBuilder.indexOf(findString);
      } else {
        tagsStart = StringUtils.indexOfIgnoreCase (findLower, findUpper,
            noteToSearch.getTagsAsString(), 0);
      }
      if (tagsStart >= 0) {
        found = true;
      }
    }

    if (checkBody) {
      bodyBuilder = new StringBuilder(noteToSearch.getBody());
      if (caseSensitive) {
        bodyStart = bodyBuilder.indexOf(findString);
      } else {
        bodyStart = StringUtils.indexOfIgnoreCase (findLower, findUpper,
            noteToSearch.getBody(), 0);
      }
      if (bodyStart >= 0) {
        found = true;
      }
    }
    
    if (found) {
      foundNote = noteToSearch;
    } else {
      foundNote = null;
    }

    return found;
  }
  
  public WebPrefs getWebPrefs() {
    return webPrefs;
  }
  
  public String getLastTextFound() {
    return lastTextFound;
  }
  
  public void noFindInProgress() {
    findButton.setText(FIND);
    replaceWindow.noFindInProgress();
  }
  
  public void findInProgress() {
    findButton.setText(FIND_AGAIN);
    replaceWindow.findInProgress();
  }
  
  public void setFindText(String findString) {
    this.findText.setText(findString);
  }
  
  /**
    Replace the findString in a Note that has already been found. 
  
    @param replaceString The string to replace the found string. 
  */
  public boolean replaceNote(
      String findString,
      String replaceString,
      boolean checkTitle, 
      boolean checkLink, 
      boolean checkTags,
      boolean checkBody) {
    
    boolean replaced = false;
    if (foundNote != null) {
      if (checkTitle && titleStart >= 0) {
        titleBuilder.replace(titleStart, titleStart + findString.length(), 
            replaceString);
        foundNote.setTitle(titleBuilder.toString());
        replaced = true;
      }

      if (checkLink && linkStart >= 0) {
 
        linkBuilder.replace(linkStart, linkStart + findString.length(), 
            replaceString);
        foundNote.setLink(linkBuilder.toString());
        replaced = true;
      }
      
      if (checkTags && tagsStart >= 0) {
        tagsBuilder.replace(tagsStart, tagsStart + findString.length(), 
            replaceString);
        foundNote.setTags(tagsBuilder.toString());
        replaced = true;
      }
      
      if (checkBody && bodyStart >= 0) {
        bodyBuilder.replace(bodyStart, bodyStart + findString.length(), 
            replaceString);
        foundNote.setBody(bodyBuilder.toString());
        replaced = true;
      }
      
      if (replaced) {
        positionAndDisplay();
        statusBar.setStatus("Replacement made");
        saveNote(foundNote);
      }
    }
    return replaced;
  }

  public void firstNote () {
    boolean modOK = modIfChanged();
    if (modOK) {
      noFindInProgress();
      position.setNavigatorToList (collectionTabs.getSelectionModel().getSelectedIndex() == 0);
      position = noteList.first (position);
      positionAndDisplay();
    }
  }

  public void priorNote () {
    boolean modOK = modIfChanged();
    if (modOK) {
      noFindInProgress();
      position.setNavigatorToList (collectionTabs.getSelectionModel().getSelectedIndex() == 0);
      position = noteList.prior (position);
      positionAndDisplay();
    }
  }

  public void nextNote() {
    boolean modOK = modIfChanged();
    if (modOK) {
      noFindInProgress();
      position.setNavigatorToList (collectionTabs.getSelectionModel().getSelectedIndex() == 0);
      position = noteList.next (position);
      positionAndDisplay();
    }
  }

  public void lastNote() {
    boolean modOK = modIfChanged();
    if (modOK) {
      noFindInProgress();
      position.setNavigatorToList (collectionTabs.getSelectionModel().getSelectedIndex() == 0);
      position = noteList.last (position);
      positionAndDisplay();
    }
  }

  private void positionAndDisplay () {
    if (position.getIndex() >= 0
        && position.getIndex() < noteList.size()
        && position.getIndex() != noteTable.getSelectionModel().getSelectedIndex()) {
      noteTable.getSelectionModel().clearAndSelect(position.getIndex());
      noteTable.scrollTo(position.getIndex());
    } 
    if (position.getTagsNode() != null
        && position.getTagsNode()
        != noteTree.getSelectionModel().getSelectedItem()) {
      noteTree.getSelectionModel().select(position.getTagsNode());
      int treeIndex = noteTree.getSelectionModel().getSelectedIndex();
      noteTree.scrollTo(treeIndex);
    }
    displayNote ();
  }

  /**
   Respond when the user clicks on a row in the Note list.
   */
  private void tableRowSelected () {
    int selectedRow = noteTable.getSelectionModel().getSelectedIndex();
    SortedNote selectedNote = (SortedNote)noteTable.getSelectionModel().getSelectedItem();
    if (selectedRow >= 0 
        && selectedRow < noteList.size() 
        && selectedNote != null) {
      boolean modOK = modIfChanged();
      if (modOK) {
        position = noteList.positionUsingListIndexAndNote 
          (selectedRow, selectedNote.getNote());
        positionAndDisplay();
      }
    }
  }

  /**
   Respond when user selects a newNote from the tags tree.
   */
  private void selectBranch () {

    TreeItem<TagsNodeValue> node = (TreeItem)noteTree.getSelectionModel().getSelectedItem();
    TagsNodeValue nodeValue = node.getValue();

    if (node == null) {
      // nothing selected
    }
    else
    if (node == position.getTagsNode()) {
      // If we're already positioned on the selected node, then no
      // need to do anything else (especially since it might set off
      // an endless loop).
    }
    else
    if (nodeValue.getNodeType() == TagsNodeValue.ITEM) {
      boolean modOK = modIfChanged();
      if (modOK) {
        Note branch = (Note)nodeValue.getTaggable();
        int branchIndex = noteList.find (branch);
        if (branchIndex >= 0) {
          position = noteList.positionUsingListIndex (branchIndex);
          position.setTagsNode (node);
          positionAndDisplay();
        } else {
          System.out.println ("Selected a branch from the tree that couldn't be found in the list");
        }
      }
    }
    else {
      // Do nothing until an item is selected
    }
  }
  
  private void expandAllTags() {
    TreeItem<TagsNodeValue> root = tagsView.getRootNode();
    expandAll(root);
  }

  private void expandAll(TreeItem<TagsNodeValue> parent) {
    TreeItem<TagsNodeValue> node = parent;
    if (node.getChildren().size() >= 0) {
      ObservableList<TreeItem<TagsNodeValue>> kids = node.getChildren();
      for (int i = 0; i < kids.size(); i++) {
        TreeItem<TagsNodeValue> kid = kids.get(i);
        expandAll(kid);
      }
    }
    parent.setExpanded(true);
    // tree.collapsePath(parent);
  }
  
  private void collapseAllTags() {
    TreeItem<TagsNodeValue> root = tagsView.getRootNode();
    collapseAll(root);
  }
  
  private void collapseAll(TreeItem<TagsNodeValue> parent) {
    TreeItem<TagsNodeValue> node = parent;
    if (node.getChildren().size() >= 0) {
      ObservableList<TreeItem<TagsNodeValue>> kids = node.getChildren();
      for (int i = 0; i < kids.size(); i++) {
        TreeItem<TagsNodeValue> kid = kids.get(i);
        collapseAll(kid);
      }
    }
    parent.setExpanded(false);
  }
  
  public void displayPrefsUpdated(DisplayPrefs displayPrefs) {
    if (position != null && displayTab != null) {
      buildDisplayTab();
    }
  }

  /**
   Populate both the Display and Edit tabs with data from the current note. 
  */
  public void displayNote () {
    Note note = position.getNote();
    if (note.hasDiskLocation()) {
      reload (note);
    }
    fileName = note.getFileName();
    
    buildDisplayTab();
    
    if (editPane.getNumberOfFields() == noteIO.getNumberOfFields()) {
      for (int i = 0; i < noteIO.getNumberOfFields(); i++) {
        DataFieldDefinition fieldDef = noteIO.getRecDef().getDef(i);
        String fieldName = fieldDef.getProperName();
        DataWidget widget = editPane.get(i);
        if (fieldName.equalsIgnoreCase(NoteParms.TITLE_FIELD_NAME)) {
          widget.setText(note.getTitle());
          oldTitle = note.getTitle();
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.LINK_FIELD_NAME)) {
          widget.setText(note.getLinkAsString());
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.TAGS_FIELD_NAME)) {
          widget.setText(note.getTagsAsString());
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.BODY_FIELD_NAME)) {
          widget.setText(note.getBody());
        } 
        else {
          DataField nextField = note.getField(i);
          widget.setText(nextField.getData());
        }

      } // end for each data field
      noteDisplayed = true;
    }
    
    editPane.setLastModDate(note.getLastModDate(NoteParms.COMPLETE_FORMAT));
    statusBar.setPosition(position.getIndexForDisplay(), noteList.size());
    modified = false;
    if (currentFileSpec != null) {
      currentFileSpec.setLastTitle(note.getTitle());
    }
    
    if (note.hasInconsistentDiskLocation()) {
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Title/File Name Mismatch");
      alert.setHeaderText(null);
      alert.setContentText("The Note's file name does not match its title");

      ButtonType changeFileName = new ButtonType("Change file name to match title");
      ButtonType leaveFileName = new ButtonType("Leave it as is", ButtonData.CANCEL_CLOSE);

      alert.getButtonTypes().setAll(changeFileName, leaveFileName);

      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == changeFileName){
        System.out.println ("OK, let's fix it!");
        saveNoteAndDeleteOnRename(note);
      } 
    }
    
    // noteList.fireTableRowsUpdated(position.getIndex(), position.getIndex());
    
  }
  
  private void reload (Note note) {
      /* ClubEventReader reader 
          = new ClubEventReader (
              clubEvent.getDiskLocation(), 
              ClubEventReader.PLANNER_TYPE);
      boolean ok = true;
      reader.setClubEventCalc(clubEventCalc);
      try {
        reader.openForInput(clubEvent);
      } catch (java.io.IOException e) {
        ok = false;
        Logger.getShared().recordEvent(LogEvent.MEDIUM, 
            "Trouble reading " + clubEvent.getDiskLocation(), false);
      }
      
      reader.close(); */
  }
  
  /**
   User has pressed the OK button to indicate that they are done editing. 
   */
  private void doneEditing() {
    if (position != null && displayTab != null) {
      boolean modOK = modIfChanged();
      if (modOK) {
        positionAndDisplay();
        activateDisplayTab();
      }
    }
  } 
  
  /**
    Changes the active tab to the tab displaying an individual item.
   */
  public void activateDisplayTab () {
    noteTabs.getSelectionModel().select(DISPLAY_TAB_INDEX);
    okButton.setDisable(true);
  }
  
  /**
    Changes the active tab to the tab displaying an individual item.
   */
  public void activateItemTab () {
    noteTabs.getSelectionModel().select(EDIT_TAB_INDEX);
    okButton.setDisable(false);
  }
  
  /**
   To be called whenever the date is modified by DateWidget.
   */
  public void dateModified (String date) {
    modified = true;
  }
  
  /**
   Apply the recurrence rule to the date.
   
   @param date Date that will be incremented. 
   */
  public String recur (String date) {
    StringDate str = new StringDate();
    str.set(date);
    RecursValue recurs = new RecursValue(getRecurrenceRule());
    return recurs.recur(str);
  }
  
  /**
   Apply the recurrence rule to the date.
   
   @param date Date that will be incremented. 
   */
  public String recur (StringDate date) {
    RecursValue recurs = new RecursValue(getRecurrenceRule());
    return recurs.recur(date);
  }
  
  /**
   Provide a text string describing the recurrence rule, that can
   be used as a tool tip.
   */
  public String getRecurrenceRule() {
    String recurs = "";
    if (canRecur()) {
      recurs = editPane.getRecurs();
      if (recurs.length() > 0) {
        if (position != null) {
          Note testNote = position.getNote();
          if (testNote != null) {
            recurs = testNote.getRecursAsString();
          } // end if we have a note to test
        } // end if we have a position that might contain a note
      } // end if we don't have any recurs data from the recurs widget
    } // end if this list has a recurs field
    return recurs;
  }
  
  /**
   Does this date have an associated rule for recurrence?
   */
  public boolean canRecur() {
    return editPane.recursIsIncluded();
  }
  
  /**
   Try to open the current newNote in the local app for the file type. 
  */
  private void openNote() {
    boolean modOK = modIfChanged();
    boolean ok = modOK;
    Note noteToOpen = null;
    File noteFileToOpen = null;
    String noteTitle = "** Unknown **";
    
    if (position == null) {
      ok = false;
    }
    
    if (ok) {
      noteToOpen = position.getNote();
      if (noteToOpen == null) {
        ok = false;
      } else {
        noteTitle = noteToOpen.getTitle();
      }
    }
    
    if (ok) {
      if (noteToOpen.hasDiskLocation()) {
        noteFileToOpen = new File(noteToOpen.getDiskLocation());
      } else {
        ok = false;
      }
    }
    
    if (ok) {
      Home.getShared().openURL(noteFileToOpen);
    }
    
    if (! ok) {
      Logger.getShared().recordEvent (LogEvent.MEDIUM,
        "Unable to open note for " + noteTitle,
        false);
    }
  }

  public void handleOpenApplication() {

  }

  /**
     Standard way to respond to an About Menu Item Selection on a Mac.
   */
  public void handleAbout() {
    displayAuxiliaryWindow(aboutWindow);
  }
  
  /*
   
   This section of the program deals with user preferences.
   
   */
    
  /**
     Standard way to respond to a Preferences Item Selection on a Mac.
   */
  public void handlePreferences() {
    displayAppPrefs ();
  }

  public void displayAppPrefs () {
    displayAuxiliaryWindow(appPrefs);
  }
  
  public void displayCollectionPrefs () {
    displayAuxiliaryWindow(collectionPrefs);
  }

  public void setSplit (boolean splitPaneHorizontal) {
    if (splitPaneHorizontal) {
      mainSplitPane.setOrientation(Orientation.HORIZONTAL);
    } else {
      mainSplitPane.setOrientation(Orientation.VERTICAL);
    }
  }

  /**
   Open the passed URI. 
   
   @param inURI The URI to open. 
  */
  public void handleOpenURI(URI inURI) {
    // Not supported
  }

  /**
   Standard way to respond to a print request.
   */
  public void handlePrintFile (File printFile) {
    // not supported
  }

  /**
     We're out of here!
   */
  public void handleQuit() {

    boolean modOK = modIfChanged();
    if (modOK) {
      Platform.exit();
    }
  }
  
  /**
   Close up shop. 
  */
  @Override
  public void stop() {
    closeFile();
    savePrefs();
  }
  
  /**
   Save all the user's preferences.
  */
  private void savePrefs () {
    if (FileUtils.isGoodInputDirectory(noteFile)) {
      userPrefs.setPref (FavoritesPrefs.LAST_FILE, noteFile.toString());
    }
    userPrefs.setPref (FavoritesPrefs.PREFS_LEFT, primaryStage.getX());
    userPrefs.setPref (FavoritesPrefs.PREFS_TOP, primaryStage.getY());
    userPrefs.setPref (FavoritesPrefs.PREFS_WIDTH, primaryStage.getWidth());
    userPrefs.setPref (FavoritesPrefs.PREFS_HEIGHT, primaryStage.getHeight());
    
    savePreferredCollectionView();
    userPrefs.setPref (GeneralPrefs.SPLIT_HORIZONTAL,
        mainSplitPane.getOrientation() == Orientation.HORIZONTAL);
    appPrefs.save();
    collectionPrefs.save();
    boolean prefsOK = userPrefs.savePrefs();
    masterCollection.savePrefs();
    // tweakerPrefs.savePrefs();
  }

  private void reloadFile() {
    boolean modOK = modIfChanged();
    if (modOK) {
      saveFile();
      NotePositioned savePosition = position;
      if (goodCollection(noteFile)) {
        openFile (noteFile, "", true);
        position = savePosition;
        positionAndDisplay();
      }
    }
  }
  
  private void reloadTaggedOnly() {
    boolean modOK = modIfChanged();
    if (modOK) {
      saveFile();
      NotePositioned savePosition = position;
      if (goodCollection(noteFile)) {
        openFile (noteFile, "", false);
        position = savePosition;
        positionAndDisplay();
      }
    }
  }

  /**
   Let the user choose a folder to open.
   */
  private void userOpenFile() {
    boolean modOK = modIfChanged();
    if (modOK) {
      dirChooser.setTitle ("Open Notes Collection");
      if (FileUtils.isGoodInputDirectory(noteFile)) {
        dirChooser.setInitialDirectory (currentDirectory);
      }

      File selectedFile = null;
      selectedFile = dirChooser.showDialog(primaryStage);
      if (selectedFile != null) {
        if (FileUtils.isGoodInputDirectory(selectedFile)) {
          closeFile();
          openFile (selectedFile, "", true);
        } else {
          trouble.report ("Trouble opening file " + selectedFile.toString(),
              "File Open Error");
        }
      } // end if user approved a file/folder choice
    }
  } // end method userOpenFile
  
  /**
   Open the Help Notes for Notenik. 
  */
  private void openHelpNotes() {

    boolean modOK = modIfChanged();
    if (modOK) {
      File appFolder = Home.getShared().getAppFolder();
      File helpFolder = new File (appFolder, "help");
      File helpNotes = new File (helpFolder, "notenik-intro");
      if (goodCollection(helpNotes)) {
        closeFile();
        openFile (helpNotes, "Help Notes", true);
        noteSortParm.setParm(NoteSortParm.SORT_BY_SEQ_AND_TITLE);
        firstNote();
      }
    }
  }
  
  private void launchButtonClicked() {
    if (editingMasterCollection) {
      openFileFromCurrentNote();
    } else {
      openURL (editPane.getLink());
    }
  }
  
  private void openFileFromCurrentNote () {
    
    boolean modOK = modIfChanged();
    boolean ok = modOK;
    if (modOK) {
      Note note = null;
      
      if (position == null) {
        ok = false;
      }
      
      if (ok) {
        note = position.getNote();
        if (note == null) {
          ok = false;
        }
      }

      if (ok) {
        File fileToOpen = note.getLinkAsFile();
        if (goodCollection(fileToOpen)) {
          String collectionTitle = note.getTitle();
          closeFile();
          openFile(fileToOpen, collectionTitle, true);
        }
      }
    }
  }

  /**
   Open the specified collection and allow the user to view and edit it. 
  
   @param fileToOpen The folder containing the collection to be opened. 
   @param titleToDisplay Any special title to be used for the collection. 
   @param loadUnTagged Load notes without tags, or omit them? 
  */
  private void openFile (
      File fileToOpen, 
      String titleToDisplay, 
      boolean loadUnTagged) {
    
    // closeFile();
    
    if (masterCollection.hasMasterCollection()
        && fileToOpen.equals(masterCollection.getMasterCollectionFolder())) {
      launchButton.setText("Open");
      editingMasterCollection = true;
    } else {
      launchButton.setText("Launch");
      editingMasterCollection = false;
    }
    logNormal("Opening folder " + fileToOpen.toString());
    noteIO = new NoteIO(fileToOpen, NoteParms.NOTES_ONLY_TYPE);
    
    NoteParms templateParms = noteIO.checkForTemplate();
    if (templateParms != null) {
      noteIO = new NoteIO (fileToOpen, templateParms);
    }    
    
    initCollection();
    
    setNoteFile (fileToOpen);
    
    try {
      noteIO.load(noteList, loadUnTagged);
      if (folderSyncPrefs.getSync()) {
        syncWithFolder();
      }
    } catch (IOException e) {
      ioException(e);
    }
    buildCollectionTabs();
    buildNoteTabs();
    addFirstNoteIfListEmpty();
    // buildNoteTabs();
    // noteList.fireTableDataChanged();
    if (fileToOpen != null && noteList != null) {
      noteSortParm.setParm(currentFileSpec.getNoteSortParm());
      noteList.sortParmChanged();
    }
    position = new NotePositioned (noteIO.getRecDef());
    setPreferredCollectionView();
    int index = -1;
    if (titleToDisplay != null && titleToDisplay.length() > 0) {
      Note noteToFind = new Note(noteList.getRecDef(), titleToDisplay);
      index = noteList.find(noteToFind);
      position = noteList.positionUsingListIndex(index);
    }
    if (index < 0) {
      position = noteList.first(position);
    }
    fileOpen = true;
    noteDisplayed = false;
    positionAndDisplay();
  }
  
  /**
   Purge closed or canceled items. 
  */
  private void purge() {
    
    boolean modOK = modIfChanged();
    if (modOK) {
      noFindInProgress();
      int purged = 0;

      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Purge Options");
      alert.setHeaderText(null);
      alert.setContentText("Purge Closed Notes?");

      ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
      ButtonType discard = new ButtonType("Purge and Discard");
      ButtonType copy = new ButtonType("Purge and Copy");

      alert.getButtonTypes().setAll(cancel, discard, copy);

      Optional<ButtonType> result = alert.showAndWait();
      ButtonType option = result.get();

      File purgeTarget = null;
      String archiveFolderStr = currentFileSpec.getArchiveFolder();
      if (archiveFolderStr != null && archiveFolderStr.length() > 0) {
        File archiveFolder = new File(archiveFolderStr);
        if (archiveFolder.exists() 
            && archiveFolder.isDirectory() 
            && archiveFolder.canWrite()) {
          purgeTarget = archiveFolder;
          dirChooser.setInitialDirectory(archiveFolder.getParentFile());
        }
      }

      NoteIO purgeIO = null;

      if (option == copy) {
        dirChooser.setTitle ("Select Folder to Hold Purged Notes");
        purgeTarget = dirChooser.showDialog(primaryStage);
        if (purgeTarget == null) {
          option = cancel;
        } else {
          if (goodCollection(purgeTarget)) {
            purgeIO = new NoteIO(purgeTarget, NoteParms.DEFINED_TYPE, noteIO.getRecDef());
          } else {
            purgeTarget = null;
            option = cancel;
          }
        } // end if purge target folder not null
      } // end if option 1 was chosen

      if (option == copy || option == discard) {
        Note workNote;
        int workIndex = 0;
        while (workIndex < noteList.size()) {
          workNote = noteList.get (workIndex);
          boolean deleted = false;
          if (workNote.getStatus().isDone()) {
            boolean okToDelete = true;  
            String fileToDelete = workNote.getDiskLocation();         
            if (option == copy) {
              try {
                purgeIO.save(purgeTarget, workNote, false);
              } catch (IOException e) {
                okToDelete = false;
                System.out.println("I/O Error while attempting to save " 
                    + workNote.getTitle() + " to Archive folder");
              }
            } // end of attempt to copy
            if (okToDelete) {
              deleted = noteList.remove (workNote);
              if (! deleted) {
                System.out.println("Unable to remove " 
                    + workNote.getTitle() + " from note list");
              }
              if (deleted) {
                deleted = new File(fileToDelete).delete();
              }
              if (! deleted) {
                trouble.report(
                    "Unable to delete note at " + fileToDelete, 
                    "Delete Failure");
              }
              if (folderSyncPrefs.getSync()) {
                File syncFile = noteIO.getSyncFile(
                    folderSyncPrefs.getSyncFolder(), 
                    folderSyncPrefs.getSyncPrefix(), 
                    workNote.getTitle());
                syncFile.delete();
              }
              // noteList.fireTableDataChanged();
              if (deleted) {
                purged++;
              } 
            } // End if OK to Delete
          } // end if newNote is a candidate for deletion
          if (! deleted) {
            workIndex++;
          }
        } // end of list
      } // end if user chose to proceed with a purge

      if (purged > 0 && option == copy && purgeTarget != null) {
        currentFileSpec.setArchiveFolder(purgeTarget);
      }

      if (purged > 0) {
        openFile (noteFile, "", true);   
        position.setNavigatorToList (collectionTabs.getSelectionModel().getSelectedIndex() == 0);
        position = noteList.first (position);
        positionAndDisplay();
      }

      String plural = StringUtils.pluralize("Note", purged);

      PSOptionPane.showMessageDialog(primaryStage,
          String.valueOf(purged) + " " + plural + " purged successfully",
          "Purge Results",
          javax.swing.JOptionPane.INFORMATION_MESSAGE);
      Logger.getShared().recordEvent (LogEvent.NORMAL, String.valueOf(purged) 
          + " " + plural + " purged",
          false);
      statusBar.setStatus(String.valueOf(purged) + " Notes purged");
    }
  } // end of method purge
  
  /**
   This item is done. 
   
   We will either mark it as complete, or bump the date. 
  */
  private void closeNote() {
    Note note = position.getNote();
    if (note.hasRecurs() && note.hasDate()) {
      // Increment Date and leave status alone
      StringDate date = note.getDate();
      String newDate = note.getRecurs().recur(date);
      editPane.setDate(newDate);
    }
    else
    if (editPane.statusIsIncluded()) {
      // Change Status to Closed
      String closedStr = noteIO.getNoteParms().getItemStatusConfig().getClosedString();
      editPane.setStatus(closedStr);
      if (editPane.dateIsIncluded()) {
        editPane.setDate(StringDate.getTodayCommon());
      }
      // newNote.setStatus(ItemStatusConfig.getShared().getClosedString());
    }
    
    if (noteTabs.getSelectionModel().getSelectedIndex() != EDIT_TAB_INDEX) {
      
    }
    if (position != null 
        && displayTab != null
        && noteTabs.getSelectionModel().getSelectedIndex() != EDIT_TAB_INDEX) {
      modIfChanged();
      positionAndDisplay();
    }
  }
  
  /**
   Let's bump up the seq field for this Note, and all following
   notes until we stop creating duplicate seq fields. 
  */
  private void incrementSeq() {

    if (noteSortParm.getParm() != NoteSortParm.SORT_BY_SEQ_AND_TITLE) {
      PSOptionPane.showMessageDialog(primaryStage,
          "First Sort by Seq + Title before Incrementing a Seq Value",
          "Sort Error",
          javax.swing.JOptionPane.WARNING_MESSAGE);
    } 
    else
    if (position == null
        || position.getNote() == null
        || position.getIndex() < 0) {
      PSOptionPane.showMessageDialog(primaryStage,
          "First select a Note before Incrementing a Seq Value",
          "Selection Error",
          javax.swing.JOptionPane.WARNING_MESSAGE);
    } else {
      oldSeq = position.getNote().getSeq();
      String newSeq = noteList.incrementSeq(
          position, 
          noteIO, 
          folderSyncPrefs.getFolderSyncPrefsData());
      editPane.setSeq(newSeq);
      
    }
  }

  private void savePreferredCollectionView () {
    userPrefs.setPref (FavoritesPrefs.LIST_TAB_SELECTED,
        collectionTabs.getSelectionModel().getSelectedIndex() == 0);
  }

  private void setPreferredCollectionView () {
    boolean listTabSelected =
        userPrefs.getPrefAsBoolean (FavoritesPrefs.LIST_TAB_SELECTED, true);
    if (listTabSelected) {
      collectionTabs.getSelectionModel().select(LIST_TAB_INDEX);
      position.setNavigatorToList(true);
    } else {
      collectionTabs.getSelectionModel().select(TAGS_TAB_INDEX);
      position.setNavigatorToList(false);
    }
  }

  /** 
   Initialize a new collection to be created, or to be opened. 
  */
  private void initCollection () {
    
    // initRecDef();
    noteList = new NoteList(noteIO.getRecDef());
    noteSortParm.resetToDefaults();
    noteList.setSortParm(noteSortParm);
    position = new NotePositioned(noteIO.getRecDef());
    noteTable = noteList.getTable();
    TagsView tagsView = noteList.getTagsModel();
    noteTree = tagsView.getTreeView();
    tagsPrefs.setTagsValueList(noteList.getTagsList());
    fileOpen = false;
    noteDisplayed = false;
    // setUnsavedChanges(false);
  }
  
  /*
  private void initRecDef() {
    dict = new DataDictionary();
    recDef = new RecordDefinition(dict);
    recDef.addColumn(NoteParms.TITLE_DEF);
    recDef.addColumn(NoteParms.TAGS_DEF);
    recDef.addColumn(NoteParms.LINK_DEF);
    recDef.addColumn(NoteParms.BODY_DEF);
  } */

  private void importFile () {

    dirChooser.setTitle ("Import Notes");
    if (FileUtils.isGoodInputDirectory(currentDirectory)) {
      fileChooser.setInitialDirectory (currentDirectory);
    }
    File selectedFile = dirChooser.showDialog(primaryStage);
    if (selectedFile != null) {
      File importFile = selectedFile;
      currentDirectory = importFile;
      NoteIO importer = new NoteIO (
          importFile, 
          NoteParms.DEFINED_TYPE, 
          noteIO.getRecDef());
      try {
        importer.load(noteList, true);
      } catch (IOException e) {
        ioException(e);
      }
      // setUnsavedChanges(true);
    }
    // noteList.fireTableDataChanged();
    firstNote();
  }
  
  private void importXMLFile () {

    fileChooser.setTitle ("Import Notes from XML");
    if (FileUtils.isGoodInputDirectory(currentDirectory)) {
      fileChooser.setInitialDirectory (currentDirectory);
    }
    File selectedFile = fileChooser.showOpenDialog(primaryStage);
    if (selectedFile != null) {
      File importFile = selectedFile;
      NoteImportXML importer = new NoteImportXML(this);
      importer.parse(importFile, noteList);
    }
    // noteList.fireTableDataChanged();
    firstNote();
  }
  
  private void importTabDelimited() {
    fileChooser.setTitle("Import Notes from a Tab-Delimited File");
    if (FileUtils.isGoodInputDirectory(currentDirectory)) {
      fileChooser.setInitialDirectory(currentDirectory);
    }
    File selectedFile = fileChooser.showOpenDialog(primaryStage);
    if (selectedFile != null) {
      NoteImportTabDelim importer = new NoteImportTabDelim(this);
      importer.parse(selectedFile, noteList);
    }
    // noteList.fireTableDataChanged();
    firstNote();
  }
  
  /**
   Import info about Mac applications. 
  */
  private void importMacAppInfo() {
    
    boolean modOK = modIfChanged();

		if (modOK) {
      dirChooser.setTitle("Import Info about Mac Applications");
      File top = new File ("/");
      dirChooser.setInitialDirectory (top);
      File selectedFile = dirChooser.showDialog(primaryStage);
      if (selectedFile != null) {
        TextMergeInputMacApps macApps = new TextMergeInputMacApps();
        RecordDefinition recDef = noteList.getRecDef();
        File[] filesInArray = selectedFile.listFiles();
        ArrayList<File> files = new ArrayList<File>();
        for (File fileInArray:filesInArray) {
          files.add(fileInArray);
        }
        int fileIndex = 0;
        while (fileIndex < files.size()) {
          File file = files.get(fileIndex);
          if (macApps.isInterestedIn(file)) {
            TextMergeMacAppReader appReader = new TextMergeMacAppReader(file);
            appReader.retrieveMacAppInfo();
            String appName = appReader.getAppName();
            String fileLink = appReader.getFileLink();
            String tags = appReader.getTags();
            String lastModDate = appReader.getLastModDate();
            String version = appReader.getVersion();
            String minSysVersion = appReader.getMinSysVersion();
            String copyright = appReader.getCopyright();
            addOrUpdateAppInfo(file, appName, fileLink, tags, lastModDate, 
                version, minSysVersion, copyright, true);
          } // end if this seems to be a genuine Mac app
          else
          if (file.isDirectory()
              && (! file.isHidden())
              && (file.canRead())) {
            File[] moreFiles = file.listFiles();
            for (File nestedFile:moreFiles) {
              files.add(nestedFile);
            }
          } // end if a sub-directory
          else
          if (file.getName().endsWith(".jar")) {
            FileName fileName = new FileName(file);
            Date lastMod = new Date (file.lastModified());
            String lastModDate = dateFormatter.format (lastMod);
            addOrUpdateAppInfo(file, fileName.getBase(), fileName.getURLString(),
                "", lastModDate, "", "", "", false);
          }
          fileIndex++;
        } // end for each file in the directory
      } // end if user specified a valid directory
      // noteList.fireTableDataChanged();
      firstNote();
    }
  }
  
  private void addOrUpdateAppInfo(
      File file,
      String appName,
      String fileLink,
      String tags,
      String lastModDate,
      String version,
      String minSysVersion,
      String copyright,
      boolean macApp) {

    RecordDefinition recDef = noteList.getRecDef();
    Note appNote = new Note(noteList.getRecDef(), appName);
    StringBuilder body = new StringBuilder();
    int ix = noteList.find(appNote);
    if (ix < 0) {
      // Not found -- add it
      if (fileLink != null && fileLink.length() > 0
          && recDef.contains(NoteParms.LINK_FIELD_NAME)) {
        appNote.setLink(fileLink);
      }
      
      if (tags != null && tags.length() > 0
          && recDef.contains(NoteParms.TAGS_FIELD_NAME)) {
        appNote.setTags(tags);
      }
      
      if (lastModDate != null && lastModDate.length() > 0
          && recDef.contains(NoteParms.DATE_FIELD_NAME)) {
        appNote.setDate(lastModDate);
      } else {
        body.append(
            "Date Last Modified: " + 
            lastModDate + "  "
            + GlobalConstants.LINE_FEED_STRING);
      }
      
      if (version != null && version.length() > 0) {
        if (recDef.contains(NoteParms.SEQ_FIELD_NAME)) {
          appNote.setSeq(version);
        } else {
          body.append(
              "Version: " +
              version + "  " +
              GlobalConstants.LINE_FEED_STRING);
        }
      }
      
      if (minSysVersion != null && minSysVersion.length() > 0) {
        if (recDef.contains(NoteParms.MIN_SYS_VERSION_FIELD_NAME)) {
          appNote.setField(NoteParms.MIN_SYS_VERSION_FIELD_NAME, minSysVersion);
        } else {
          body.append(
              "Minimum System Version: " +
              minSysVersion + "  " +
              GlobalConstants.LINE_FEED_STRING);
        }
      }

      if (copyright != null && copyright.length() > 0) {
        if (recDef.getColumnNumber(NoteParms.BODY_FIELD_NAME) >= 0) {
          body.append("Copyright: " + copyright);
          appNote.setBody(body.toString());
        }
      }

      addImportedNote(appNote);

    } 
    else {
      // Found in table -- update where it makes sense
      appNote = noteList.get(ix);
      if (appNote.isLinkToMacApp() && (! macApp)) {
        // Don't replace a real app entry with a jar file
      } else {
        if (fileLink != null && fileLink.length() > 0
            && recDef.contains(NoteParms.LINK_FIELD_NAME)) {
          appNote.setLink(fileLink);
        }

        if (lastModDate != null && lastModDate.length() > 0
            && recDef.contains(NoteParms.DATE_FIELD_NAME)) {
          appNote.setDate(lastModDate);
        } 

        if (version != null && version.length() > 0) {
          if (recDef.contains(NoteParms.SEQ_FIELD_NAME)) {
            appNote.setSeq(version);
          } 
        }

        if (minSysVersion != null && minSysVersion.length() > 0) {
          if (recDef.contains(NoteParms.MIN_SYS_VERSION_FIELD_NAME)) {
            appNote.setField(NoteParms.MIN_SYS_VERSION_FIELD_NAME, minSysVersion);
          } 
        }

        if (copyright != null && copyright.length() > 0) {
          if (recDef.getColumnNumber(NoteParms.BODY_FIELD_NAME) >= 0) {
            body.append("Copyright: " + copyright);
            appNote.setBody(body.toString());
          }
        }
      } // end if not replacing a real app with a jar file
    } // end if app note already exists
    
  }
  
  private boolean addImportedNote(Note importNote) {
    boolean added = false;
    if ((! importNote.hasTitle()) 
        || importNote.getTitle().length() == 0
        || (! importNote.hasUniqueKey())) {
      // do nothing
    } else {
      importNote.setLastModDateToday();
      saveNote(importNote);
      noteList.add (importNote);
      // noteList.fireTableDataChanged();
      added = true;
    }
    return added;
  }

  private void saveFile () {
    savePreferredCollectionView();
    if (noteFile == null) {
      userSaveFileAs();
    } else {
      try {
        noteIO.save (noteList);
      } catch (IOException e) {
        ioException (e);
      }
      publishWindow.saveSource();
    }
  }
  
  public boolean saveAll() {
    
    boolean saveOK = modIfChanged();
    
    int numberSaved = 0;
    int numberDeleted = 0;
    for (int i = 0; i < noteList.totalSize() && saveOK; i++) {
      Note nextNote = noteList.get(i);
      String oldDiskLocation = nextNote.getDiskLocation();
      try {
        noteIO.save(nextNote, true);
      } catch (IOException e) {
        saveOK = false;
        trouble.report(primaryStage, 
            "Trouble saving the item to disk", "I/O Error");
        saveOK = false;
      }
      if (saveOK) {
        numberSaved++;
        String newDiskLocation = nextNote.getDiskLocation();
        if (! newDiskLocation.equals(oldDiskLocation)) {
          File oldDiskFile = new File (oldDiskLocation);
          oldDiskFile.delete();
          numberDeleted++;
        }
      } 
    }
    
    String saveResult;
    if (saveOK) {
      saveResult = "succeeded";
    } else {
      saveResult = "failed";
    }
      logger.recordEvent(LogEvent.NORMAL, 
          "Save All command succeeded, resulting in " 
            + String.valueOf(numberSaved)
            + " saves and "
            + String.valueOf(numberDeleted)
            + " deletes", false);

    
    return saveOK;
  }

  /**
   Save the current collection to a location specified by the user.
   */
  private void userSaveFileAs () {
    boolean modOK = modIfChanged();
    if (modOK) {
      dirChooser.setTitle ("Save Notes to File");
      if (FileUtils.isGoodInputDirectory(currentDirectory)) {
        dirChooser.setInitialDirectory (currentDirectory);
      }
      File selectedFile = dirChooser.showDialog (primaryStage);
      if(goodCollection(selectedFile)) {
        File chosenFile = selectedFile;
        saveFileAs(chosenFile);
      }
    }
  }
  
  /**
   Allow the user to create a new collection.
  */
  public void userNewFile() {
    boolean modOK = modIfChanged();
    if (modOK) {
      dirChooser.setTitle ("Select Folder for New Note Collection");
      if (FileUtils.isGoodInputDirectory(currentDirectory)) {
        dirChooser.setInitialDirectory (currentDirectory);
      }
      File selectedFile = dirChooser.showDialog (primaryStage);
      if (selectedFile != null) {
        if (goodCollection(selectedFile)) {
          closeFile();
          openFile(selectedFile, "", true);
        } else {
          trouble.report ("Trouble opening new file " + selectedFile.toString(),
              "New File Open Error");
        }
      } // end if user selected a file
    } // end if mods ok
  } // end method userNewFile
  
  public void openEssentialCollection() {
    boolean modOK = modIfChanged();
    if (modOK) {
      if (filePrefs.hasEssentialFilePath()) {
        File selectedFile = new File(filePrefs.getEssentialFilePath());
        if (goodCollection(selectedFile)) {
          closeFile();
          openFile (selectedFile, "", true);
        } else {
          trouble.report ("Trouble opening file " + selectedFile.toString(),
              "File Open Error");
        }
      } // end if we have an essential file path
    } // end if mod ok
  }
  
  /**
   Generate a template file containing all supported note fields. 
  */
  public void generateTemplate() {
 
    dirChooser.setTitle ("Select Folder for Note Template");
    if (FileUtils.isGoodInputDirectory(currentDirectory)) {
      dirChooser.setInitialDirectory (currentDirectory);
    }
    if (FileUtils.isGoodInputDirectory(noteFile)) {
      dirChooser.setInitialDirectory(noteFile.getParentFile());
    }
    File selectedFile = dirChooser.showDialog (primaryStage);
    if(selectedFile != null) {
      NoteParms templateParms = new NoteParms(NoteParms.NOTES_EXPANDED_TYPE);
      RecordDefinition recDef = templateParms.getRecDef();
      Note templateNote = new Note(recDef);
      templateNote.setTitle("The unique title for this note");
      templateNote.setTags("One or more tags, separated by commas");
      templateNote.setLink("http://anyurl.com");
      templateNote.setStatus("One of a number of states");
      templateNote.setType("The type of note");
      templateNote.setSeq("Rev Letter or Version Number");
      StringDate today = new StringDate();
      today.set(StringDate.getTodayYMD());
      templateNote.setDate(today);
      templateNote.setRecurs("Every Week");
      templateNote.setAuthor("The Author of the Note");
      templateNote.setRating("5");
      templateNote.setIndex("Index Term");
      templateNote.setTeaser
        ("A brief sample of the note that will make people want to read more");
      templateNote.setBody("The body of the note");
      File templateFile = new File(selectedFile, "template.txt");
      NoteIO templateIO = new NoteIO(selectedFile);
      templateIO.save(templateNote, templateFile, true); 
    }
  }
  
  /**
   Save the current collection of Notes to the specified file. 
  
   @param asFile The file to save the noteList to.  
  */
  private void saveFileAs(File asFile) {
    savePreferredCollectionView();
    setNoteFile (asFile);
    try {
      noteIO.save (noteList);
    } catch (IOException e) {
      ioException(e);
    }
    publishWindow.saveSource();
  }

  /**
   Save various bits of information about a new Note file that we are
   working with.

   @param file The specific file we are working with that contains a list
   of Notes.

   */
  private void setNoteFile (File file) {
    
    if (file == null) {
      noteFile = null;
      noteIO = null;
      exporter = null;
      currentFileSpec = null;
      statusBar.setFileName("            ", " ");
    } else {
      noteFile = file;
      if (noteIO == null) {
        noteIO = new NoteIO(file);
      } else {
        noteIO.setHomeFolder(file);
      }
      exporter = new NoteExport(this);
      if (noteList != null) {
        noteList.setSource (file);
        noteList.setTitle(noteList.getSource().getName());
      }
      currentFileSpec = masterCollection.addRecentFile (file);
      currentDirectory = file;
      userPrefs.setPref (FavoritesPrefs.LAST_FILE, file.toString());
      FileName fileName = new FileName (file);
      statusBar.setFileName(fileName);
      publishWindow.openSource(currentDirectory);
      reports.setDataFolder(file);
    }

    collectionPrefs.setFileSpec(currentFileSpec);
  }

  public void displayPublishWindow() {
    displayAuxiliaryWindow(publishWindow);
  }
  
  /**
   If the link points to a file on a file share, then display info 
   about the file. 
  */
  private void displayFileInfo() {
    boolean displayed = false;
    if (position != null) {
      Note note = position.getNote();
      if (note != null) {
        if (note.hasLink()) {
          String link = note.getLinkAsString();
          if (link.startsWith("file:")) {
            fileInfoWindow.setFile(link);
            displayAuxiliaryWindow(fileInfoWindow);
            displayed = true;
          } // end if we have a file
        } // end if we have a link
      } // end if we have a newNote
    } // end if we have a position
    if (! displayed) {
      trouble.report ("No file to display",
          "No File Specified");
    }
  } // end method

  public void displayAuxiliaryWindow(WindowToManage window) {
    window.setLocation(
        primaryStage.getX() + 60,
        primaryStage.getY() + 60);
    WindowMenuManager.getShared().makeVisible(window);
    window.toFront();
  }
  
  private void tweakURL() {
    if (editPane.hasLink()) {
      linkTweaker.setLink(editPane.getLink());
    }
    displayAuxiliaryWindow(linkTweaker);
  }
  
  private void invokeLinkTweaker() {
    displayAuxiliaryWindow(linkTweaker);
  }
  
  /**
   Get the current link so that it can be tweaked. 
  
   @return The Link to be tweaked. 
  */
  public String getLinkToTweak() {
    return editPane.getLink();
  }
  
  /**
   Set a link field to a new value after it has been tweaked. 
  
   @param tweakedLink The link after it has been tweaked. 
   @param linkID      A string identifying the link, in case there are more
                      than one. This would be the text used in the label
                      for the link. 
  */
  public void putTweakedLink (String tweakedLink, String linkID) {
    if (editPane.hasLink()) {
      editPane.setLink(tweakedLink);
    }
  }
  
  public void setLink(File file) {
    if (editPane != null && file != null) {
      editPane.setLink(StringUtils.tweakAnyLink(file.getAbsolutePath(), 
          false, false, false, ""));
    }
  }

  public void openURL (File file) {
    appster.openURL(file);
  }

  public void openURL (String url) {
    appster.openURL(url);
  }

  private void tagsActionPerformed (java.awt.event.ActionEvent evt) {
    
  }

  /**
    Validate the Links associated with the notes in the current list.
   */
  public void validateURLs () {

    boolean modOK = modIfChanged();

    if (modOK) {

      // Make sure user is ready to proceed

      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Validate Web Pages");
      alert.setHeaderText(null);
      alert.setContentText("Please ensure your Internet connection is active");
      ButtonType continueButton = new ButtonType("Continue");
      ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
      alert.getButtonTypes().setAll(continueButton, cancelButton);
      Optional<ButtonType> result = alert.showAndWait();

      // If User is ready, then proceed
      if (result.get() == continueButton) {

        // Prepare Auxiliary List to track invalid Notes
        webPageGroup = new ThreadGroup("URL Validation threads");
        urlValidators = new ArrayList();

        // Go through sorted items looking for Web Pages
        Note workNote;
        String address;
        URLValidator validator;
        for (int workIndex = 0; workIndex < noteList.size(); workIndex++) {
          workNote = noteList.get (workIndex);
          address = workNote.getURLasString();
          if (address.length() > 0) {
            validator = new URLValidator (workNote, workIndex);
            validator.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
              @Override
              public void handle(WorkerStateEvent evt) {
                progress++;
                progressWindow.setProgress(progress, progressMax);
                if (progress >= progressMax) {
                  validateURLAllDone();
                } // end if all pages checked
              }
            });
            validator.setOnFailed(new EventHandler<WorkerStateEvent>() {
              @Override
              public void handle(WorkerStateEvent evt) {
                progress++;
                progressWindow.setProgress(progress, progressMax);
                badPages++;
                if (progress >= progressMax) {
                  validateURLAllDone();
                } // end if all pages checked
              }
            });
            urlValidators.add (validator);
          }
        } // end of list

        // Prepare dialog to show validation progress
        progress = 0;
        progressMax = urlValidators.size();
        ProgressWindow progressWindow = new ProgressWindow(primaryStage,
            "Progress Validating URLs");
        windowMenuManager.add(progressWindow);
        windowMenuManager.makeVisible(progressWindow);

        // Now start threads to check Web pages
        badPages = 0;
        for (int i = 0; i < urlValidators.size(); i++) {
          validator = (URLValidator)urlValidators.get(i);
          Thread validatorThread = new Thread(webPageGroup, validator);
          validatorThread.setDaemon(true);
          validatorThread.start();
        } // end for each page being validated

        // Start timer to give the user a chance to cancel
        /*
        if (validateURLTimer == null) {
          validateURLTimer = new javax.swing.Timer (ONE_SECOND, this);
        } else {
          validateURLTimer.setDelay (ONE_SECOND);
        }
        validateURLTimer.start();
        */
      } // continue rather than cancel
    }
  } // end validateURLs method

  /**
    Handle GUI events, including the firing of various timers.

    @param event The GUI event that fired the action.
   */
  /*
  public void actionPerformed (ActionEvent event) {
    Object source = event.getSource();

    // URL Validation Timer
    if (source == validateURLTimer) {
      if (progressDialog.isCanceled()) {
        URLValidator validator;
        for (int i = 0; i < urlValidators.size(); i++) {
          validator = (URLValidator)urlValidators.get(i);
          if (! validator.isValidationComplete()) {
            Logger.getShared().recordEvent (new LogEvent (LogEvent.MEDIUM,
                "URL Validation incomplete for "
                + validator.toString(),
                false));
            validator.interrupt();
          }
        } // end for each page being validated
        validateURLAllDone();
      }
    }

  } // end method
  */

  /**
    Shut down the URL Validation process and report the results.
   */
  private void validateURLAllDone () {
    if (validateURLTimer != null
        && validateURLTimer.isRunning()) {
      validateURLTimer.stop();
    }

    // Add "Invalid URL" tags to invalid URL items
    if (badPages > 0) {
      URLValidator validator;
      NotePositioned workNote;
      for (int i = 0; i < urlValidators.size(); i++) {
        validator = (URLValidator)urlValidators.get(i);
        if (validator.getState() == Worker.State.FAILED) {
          workNote = noteList.positionUsingListIndex (validator.getIndex());
          if (workNote.getNote().equals (validator.getItemWithURL())) {
            workNote.getNote().getTags().merge (INVALID_URL_TAG);
            noteList.modify(workNote);
            saveNote(workNote.getNote());
          } // end if we have the right URL
        } // end if URL wasn't validated
      } // end for each page being validated
      // noteList.fireTableDataChanged();
    } // end if any bad pages found

    // Close progress dialog and show user the final results
    windowMenuManager.hideAndRemove(progressWindow);

    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("URL Validation Results");
    alert.setHeaderText("Look, an Information Dialog");
    alert.setContentText(String.valueOf (badPages)
          + " Invalid URL(s) Found out of "
          + String.valueOf (urlValidators.size()));
    alert.showAndWait();

  } // end method

  public File getCurrentDirectory () {
    return currentDirectory;
  }
  
  private void generalExport(int exportType) {
    boolean modOK = modIfChanged();
    if (modOK) {
      
      boolean ok = true;
      int exported = 0;
      
      String selectTagsStr 
        = tagsPrefs.getSelectTagsAsString();
      String suppressTagsStr 
        = tagsPrefs.getSuppressTagsAsString();
      
      File selectedFile;
      fileChooser.setTitle ("Export to " 
          + NoteExport.EXPORT_TYPE[exportType]);
      dirChooser.setTitle ("Export to " 
          + NoteExport.EXPORT_TYPE[exportType]);
      switch (exportType) {

        case NoteExport.NOTENIK_EXPORT:
          selectedFile = dirChooser.showDialog(primaryStage);
          if (selectedFile == null) {
            // this condition will be handled later
          }
          else
          if (selectedFile.isFile()
              || (! selectedFile.canWrite())) {
            ok = false;
            noValidExportDestination();
          } 
          break;

        case NoteExport.OPML_EXPORT:
          fileChooser.setInitialDirectory(noteFile.getParentFile());
          fileChooser.setInitialFileName (noteFile.getName() + ".opml");
          selectedFile = fileChooser.showSaveDialog(primaryStage);
          break;
        
        case NoteExport.XML_EXPORT:
          fileChooser.setInitialDirectory(noteFile.getParentFile());
          fileChooser.setInitialFileName (noteFile.getName() + ".xml");
          selectedFile = fileChooser.showSaveDialog(primaryStage);
          break;
          
        case NoteExport.TABDELIM_EXPORT_MS_LINKS:
          fileChooser.setInitialDirectory(noteFile.getParentFile());
          fileChooser.setInitialFileName (noteFile.getName() + ".txt");
          selectedFile = fileChooser.showSaveDialog(primaryStage);
          break;

        case NoteExport.TABDELIM_EXPORT:
        default:
          fileChooser.setInitialDirectory(noteFile.getParentFile());
          fileChooser.setInitialFileName (noteFile.getName() + ".txt");
          selectedFile = fileChooser.showSaveDialog(primaryStage);
      } // end switch for fileChooser setup
      
      if (selectedFile == null) {
        ok = false;
        noValidExportDestination();
      } 
      if (ok) {
        if (exportType == NoteExport.OPML_EXPORT) {
          exported = 
            exporter.OPMLExport(
              selectedFile,
              noteList);
        } else {
          exported = 
            exporter.generalExport(
              selectedFile,
              noteFile,
              noteIO.getRecDef(),
              noteList,
              exportType, 
              selectTagsStr, 
              suppressTagsStr);
        }

        if (ok && exported >= 0) {
          Alert alert = new Alert(AlertType.INFORMATION);
          alert.setTitle("Export Results");
          alert.setHeaderText(null);
          alert.setContentText(String.valueOf(exported) 
                  + " Notes exported successfully to"
                  + GlobalConstants.LINE_FEED
                  + selectedFile.toString());
          alert.showAndWait();
          Logger.getShared().recordEvent (LogEvent.NORMAL, String.valueOf(exported) 
              + " Notes exported to " 
              + selectedFile.toString(),
              false);
          statusBar.setStatus(String.valueOf(exported) + " Notes exported");
        } else {
          Logger.getShared().recordEvent (LogEvent.MEDIUM,
            "Problem exporting Notes to " + selectedFile.toString(),
              false);
          Trouble.getShared().report ("I/O error attempting to export notes to " 
                + selectedFile.toString(),
              "I/O Error");
          statusBar.setStatus("Export problems");
        }
      } // end if prepared to attempt export
    } // end if last mod ok
  } // end method generalExport
  
  private void noValidExportDestination() {
    Trouble.getShared().report ("No valid export destination specified",
        "Export Aborted");
  }
  
  /**
   Copy the current newNote to the system clipboard. 
   */
  private void copyNote() {
    boolean noNoteSelected = true;
    if (position != null) {
      Note note = position.getNote();
      if (note != null) {
        noNoteSelected = false;
        TextLineWriter writer = new ClipboardMaker();
        noteIO.save(note, writer);
      }
    }
    
    if (noNoteSelected) {
      trouble.report ("Select a Note before trying to copy it", 
          "No Note Selected");
    } 
  }
  
  /**
   Paste note from clipboard. 
  */
  private void pasteNote() {

    boolean ok = false;
    boolean modOK = modIfChanged();
    Note newNote = null;
    if (modOK) {
      ok = true;
      String noteText = "";
      TextLineReader reader = new ClipboardReader();
      newNote = noteIO.getNote(reader);
      if (newNote == null 
          || (! newNote.hasTitle()) 
          || (! newNote.hasUniqueKey())
          || (newNote.getTitle().length() == 0)) {
        ok = false;
      }
    }
    
    if (ok) {
      String newFileName = newNote.getFileName();
        if (noteIO.exists(newFileName)) {
          trouble.report (primaryStage, 
            "A Note already exists with the same What field",
            "Duplicate Found");
        ok = false;
      }
    }
    
    if (ok) {
      newNote.setLastModDateToday();
      position = new NotePositioned(noteIO.getRecDef());
      position.setIndex (noteList.size());
      position.setNote(newNote);
      position.setNewNote(true);
      fileName = "";
      displayNote();
      saveNote(newNote);
      addNoteToList ();
    }
    
    if (! ok) {
      trouble.report ("Trouble pasting new note from Clipboard",
          "Clipboard Error");
    }
    
  }
  
  public void genHTMLtoClipboard() {
    boolean noNoteSelected = true;
    boolean ok = true;
    Note note = null;
    if (position != null) {
      note = position.getNote();
      if (note != null) {
        noNoteSelected = false;
        NoteExport exporter = new NoteExport(this);
        ok = exporter.bodyToHTMLClipboard(note);
      }
    }
    
    if (noNoteSelected) {
      trouble.report ("Select a Note before trying to generate HTML", 
          "No Note Selected");
    } 
    
    if (ok) {
      logNormal("HTML generated for body of Note " + note.getTitle());
    }
  }
  
  public void genHTMLtoFile() {
    boolean noNoteSelected = true;
    boolean ok = true;
    Note note = null;
    File selectedFile = null;
    if (position != null) {
      note = position.getNote();
      if (note != null) {
        noNoteSelected = false;
        String htmlFolderStr = currentFileSpec.getHTMLFolder();
        if (htmlFolderStr.length() > 0) {
          File htmlFolder = new File(htmlFolderStr);
          if (htmlFolder.exists()) {
            fileChooser.setInitialDirectory(htmlFolder);
            FileName fileName = new FileName(note.getDiskLocation());
            fileChooser.setInitialFileName(fileName.getBase() + ".html");
          }
        }
        selectedFile = fileChooser.showSaveDialog(primaryStage);
        if (selectedFile == null) {
          ok = false;
          noValidExportDestination();
        }
        NoteExport exporter = new NoteExport(this);
        ok = exporter.bodyToHTMLFile(note, selectedFile);
      }
    }
    
    if (noNoteSelected) {
      trouble.report ("Select a Note before trying to generate HTML", 
          "No Note Selected");
    } 
    
    if (ok) {
      logNormal("HTML generated for body of Note " + note.getTitle());
    }
  }
  
  public void logNormal (String msg) {
    Logger.getShared().recordEvent (LogEvent.NORMAL, msg, false);
  }
  
}
