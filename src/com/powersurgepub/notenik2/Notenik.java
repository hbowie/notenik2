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
  import javafx.scene.control.Skin.*;
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
      AppWithLinksToValidate,
      DateWidgetOwner,
      DisplayWindow,
      FileSpecOpener,
      LinkTweakerApp,
      NoteCollectionView,
      PublishAssistant,
      ScriptExecutor,
      TagsChangeAgent,
      WebLauncher {
  
  public static final String PROGRAM_NAME    = "Notenik";
  public static final String PROGRAM_VERSION = "4.20";
  
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
  private             NoteCollectionModel model = null;
  // private             NoteIO              noteIO = null;
  // private             FileSpec            currentFileSpec = null;
  // private             File                noteFile = null;
  private             File                currentDirectory;
  private             NoteExport          exporter;
  // private             String              oldTitle = "";
  // private             String              oldSeq = "";
  // private             String              fileName = "";
  // private             boolean             fileOpen = false;

  // private             Note                currentNote = null;
  private             int                 displayedID = -1;
  private             boolean             noteDisplayed = false;
  private             boolean             newNote = true;
  // private             String              uniqueKeyDisplayed = "";
  // private             String              sortKeyDisplayed = "";
  // private             String              tagsDisplayed = "";
  // private             NotePositioned      position = null;
  private             boolean             modified = false;
  private             boolean             opInProgress = false;
  private             boolean             unsavedChanges = false;
  private             int                 listPosition = 0;
  private             String              lastGoodTitle = "";
  
  /** This is the current collection of Notes. */
  // private             NoteList            noteList = null;
  
  private             Stage               primaryStage;
  private             VBox                primaryLayout;
  private             Scene               primaryScene;
  private             FXUtils             fxUtils;
  
  /*
   Menu Definitions
  */
  private             MenuBar             menuBar;
  
  private             Menu                fileMenu        = new Menu("File");
  private             MenuItem              openMenuItem;
  private             Menu                  openRecentMenu;
  private             MenuItem              jumpMenuItem;
  private             MenuItem              openEssentialMenuItem;
  private             MenuItem              openMasterCollectionMenuItem;
  private             MenuItem              createMasterCollectionMenuItem;
  private             MenuItem              openHelpNotesMenuItem;
  private             MenuItem              fileNewMenuItem;
  private             MenuItem              fileSaveMenuItem;
  private             MenuItem              saveAllMenuItem;
  private             MenuItem              fileSaveAsMenuItem;
  private             MenuItem              fileBackupMenuItem;
  private             MenuItem              reloadMenuItem;
  private             MenuItem              reloadTaggedMenuItem;
  private             MenuItem              publishWindowMenuItem;
  private             MenuItem              publishNowMenuItem;
  private             Menu                  importMenu;
  private             MenuItem                importMacAppInfo;
  private             MenuItem                importNotenikMenuItem;
  private             MenuItem                importTabDelimitedMenuItem;
  private             MenuItem                importXMLMenuItem;
  private             Menu                  exportMenu;
  private             MenuItem                exportNotenikMenuItem;
  private             MenuItem                exportOPML;
  private             MenuItem                exportTabDelimitedMenuItem;
  private             MenuItem                exportTabDelimitedMSMenuItem;
  private             MenuItem                exportXMLMenuItem;
  private             MenuItem              purgeMenuItem;
  
  private             Menu                collectionMenu  = new Menu("Collection");
  private             MenuItem              collectionPrefsMenuItem;
  private             MenuItem              collectionTemplateMenuItem;
  private             MenuItem              findMenuItem;
  private             MenuItem              replaceMenuItem;
  private             MenuItem              addReplaceTagsMenuItem;
  private             MenuItem              flattenTagsMenuItem;
  private             MenuItem              lowerCaseTagsMenuItem;
  private             MenuItem              validateURLsMenuItem;
  
  private             Menu                sortMenu        = new Menu("Sort");
  
  private             Menu                noteMenu        = new Menu("Note");
  private             MenuItem              newNoteMenuItem;
  private             MenuItem              deleteNoteMenuItem;
  private             MenuItem              nextMenuItem;
  private             MenuItem              priorMenuItem;
  private             MenuItem              openNoteMenuItem;
  private             MenuItem              closeNoteMenuItem;
  private             MenuItem              getFileInfoMenuItem;
  private             MenuItem              incrementSeqMenuItem;
  private             MenuItem              incrementDateMenuItem;
  private             MenuItem              copyNoteMenuItem;
  private             MenuItem              pasteNoteMenuItem;
  private             Menu                  htmlMenu;
  private             MenuItem                htmlToClipboardMenuItem;
  private             MenuItem                htmlToFileMenuItem;
  private             MenuItem              copyCodeMenuItem;
  
  private             KeyCombination        incKC;
  
  private             Menu                editMenu        = new Menu("Edit");
  private             MenuItem              undoEditsMenuItem;
  private             MenuItem              cutMenuItem = new MenuItem();
  private             MenuItem              copyMenuItem = new MenuItem();
  private             MenuItem              pasteMenuItem = new MenuItem();
  
  private             Menu                toolsMenu       = new Menu("Tools");
  private             MenuItem              toolsOptionsMenuItem;
  private             MenuItem              toolsLinkTweakerMenuItem;
  
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
  private             BorderPane          listPane = null;
  private             TableView           noteTable;
  private             TableViewSelectionModel<SortedNote> selModel;
  private             ObservableList<Integer> selected;
  
  private             Tab                 tagsTab;
  private             BorderPane          treePane = null;
  private             TreeView            noteTree;
  private             MultipleSelectionModel<SortedNote> treeSelModel;
  private             ObservableList<Integer> treeSelected;
  private             HBox                treeButtonsPane;
  private             Button              expandAllButton;
  private             Button              collapseAllButton;
  
  private             TabPane             noteTabs;
  public static final int                   DISPLAY_TAB_INDEX = 0;
  public static final int                   EDIT_TAB_INDEX = 1;
  private             Tab                 displayTab;
  private             Tab                 editTab;
  
  private             DisplayPane         displayPane;
  private             EditPane            editPane;
  
  private             StatusBar           statusBar;
  
  private             WindowMenuManager   windowMenuManager;
  
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
  
  private             Reports             reports;
  
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
  private             ThreadGroup         webPageGroup;
  private             ArrayList<URLValidator> urlValidators;
  private             int                 linksToCheck = 0;
  private             int                 linksChecked = 0;
  private             int                 deadLinks = 0;
  private             URLValidationProgressWindow      progressWindow;
  private             Note                initialSelection;
  
  // System ClipBoard fields
  boolean             clipBoardOwned = false;
  Clipboard           clipBoard = null;
  // Transferable        clipContents = null;
  
  private             TextMergeHarness    textMerge = null;
  
  // Work Items for Cut, Copy and Paste operations
  private             Object              objWithFocus;
  private             TextInputControl    textIn;
  private             Note                noteToCopy;
  private             String              selectedText;
  private             Clipboard           clipboard;
  private             boolean             hasTransferableText;
  
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
    
    model = new NoteCollectionModel(this);
    model.setSortMenu(sortMenu);
    
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
    model.setFilePrefs(filePrefs);
    
    tweakerPrefs = new TweakerPrefs();
    appPrefs.addSet(tweakerPrefs);
    
    appPrefs.setScene();
    appPrefs.addToMenu(optionsMenu, true);
    WindowMenuManager.getShared().add(appPrefs);
    
    // Set up separate window for Collection Preferences
    collectionPrefs = new PrefsJuggler(primaryStage);
    collectionPrefs.setTitle("Collection Preferences");
    
    folderSyncPrefs = new FolderSyncPrefs(this, primaryStage);
    collectionPrefs.addSet(folderSyncPrefs);
    
    htmlPrefs = new HTMLPrefs(this, primaryStage);
    collectionPrefs.addSet(htmlPrefs);
    
    collectionPrefs.setScene();
    
    exporter = new NoteExport(this);
    
    filePrefs.setRecentFiles(model.getMaster().getRecentFiles());
    model.getMaster().registerMenu(openRecentMenu, this);
    model.getMaster().load();
    
    if (filePrefs.purgeRecentFilesAtStartup()) {
      model.getMaster().purgeInaccessibleFiles();
    }
    
    if (model.getMaster().hasMasterCollection()) {
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

    boolean opened = model.openAtStartup(favoritesPrefs.isOpenStartup());
    if (opened) {
      newCollection();
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
        if (model.editingMasterCollection()) {
          openCollectionFromCurrentNote();
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
    
    // Jump to Last Collection Menu Item
    jumpMenuItem = new MenuItem("Jump to Last Collection");
    FXUtils.assignShortcut(jumpMenuItem, "J");
    jumpMenuItem.setOnAction(e -> jumpToLastCollection());
    fileMenu.getItems().add(jumpMenuItem);
    
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
    fileNewMenuItem = new MenuItem("New Collection...");
    fileNewMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        userNewFile();
      }
    });
    fileMenu.getItems().add(fileNewMenuItem);
    
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
        // saveAll();
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
        if (model.isOpen() && model.size() > 0) {
          promptForBackup();
        } else {
          trouble.report(
              primaryStage, 
              "Open a Notes folder before attempting a backup", 
              "Backup Error",
              AlertType.ERROR);
        }
      });
    FXUtils.assignShortcut(fileBackupMenuItem, "B");
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
    // Let's build out the Collection Menu
    //
    
    // Collection Prefs Menu Item
    collectionPrefsMenuItem = new MenuItem("Collection Preferences");
    collectionPrefsMenuItem.setOnAction(e -> displayAuxiliaryWindow(collectionPrefs));
    collectionMenu.getItems().add(collectionPrefsMenuItem);
    
    // Collection Template Menu Item
    collectionTemplateMenuItem = new MenuItem("Collection Template");
    collectionTemplateMenuItem.setOnAction(e -> editTemplate());
    collectionMenu.getItems().add(collectionTemplateMenuItem);
    
    fxUtils.addSeparator(collectionMenu);
    
    // Find Menu Item
    findMenuItem = new MenuItem("Find");
    KeyCombination fkc
        = new KeyCharacterCombination("F", KeyCombination.SHORTCUT_DOWN);
    findMenuItem.setAccelerator(fkc);
    findMenuItem.setOnAction(e -> findNote());
    collectionMenu.getItems().add(findMenuItem);
    
    // Replace Menu Item
    replaceMenuItem = new MenuItem("Replace...");
    KeyCombination rkc
        = new KeyCharacterCombination("R", KeyCombination.SHORTCUT_DOWN);
    replaceMenuItem.setAccelerator(rkc);
    replaceMenuItem.setOnAction(e -> startReplace());
    collectionMenu.getItems().add(replaceMenuItem);
    
    fxUtils.addSeparator(collectionMenu);
    
    // Add/Replace Tag Menu Item
    addReplaceTagsMenuItem = new MenuItem("Add/Replace Tag...");
    addReplaceTagsMenuItem.setOnAction(e -> checkTags());
    collectionMenu.getItems().add(addReplaceTagsMenuItem);
    
    // Flatten Tag Levels Menu Item
    flattenTagsMenuItem = new MenuItem("Flatten Tag Levels");
    flattenTagsMenuItem.setOnAction(e -> flattenTags());
    collectionMenu.getItems().add(flattenTagsMenuItem);
    
    // Lower Case Tags Menu Item
    lowerCaseTagsMenuItem = new MenuItem("Lower Case Tags");
    lowerCaseTagsMenuItem.setOnAction(e -> lowerCaseTags());
    collectionMenu.getItems().add(lowerCaseTagsMenuItem);
    
    fxUtils.addSeparator(collectionMenu);
    
    // Validate Links Menu Item
    validateURLsMenuItem = new MenuItem("Validate Links...");
    validateURLsMenuItem.setOnAction(e -> validateURLs());
    collectionMenu.getItems().add(validateURLsMenuItem);
    
    
    //
    // Let's build out the Note Menu
    //
    
    // New Note Menu Item
    newNoteMenuItem = new MenuItem("New Note");
    KeyCombination nkc
        = new KeyCharacterCombination("n", KeyCombination.SHORTCUT_DOWN);
    newNoteMenuItem.setAccelerator(nkc);
    newNoteMenuItem.setOnAction(e -> newNote());
    noteMenu.getItems().add(newNoteMenuItem);
    
    // Delete Note Menu Item
    deleteNoteMenuItem = new MenuItem("Delete Note");
    deleteNoteMenuItem.setOnAction(e -> removeNote());
    FXUtils.assignShortcut(deleteNoteMenuItem, "D");
    noteMenu.getItems().add(deleteNoteMenuItem);
    
    undoEditsMenuItem = new MenuItem("Undo Edits");
    FXUtils.assignShortcut(undoEditsMenuItem, "Z");
    undoEditsMenuItem.setOnAction(e -> undoEdits());
    noteMenu.getItems().add(undoEditsMenuItem);
    
    fxUtils.addSeparator(noteMenu);
    
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
    
    fxUtils.addSeparator(noteMenu);
    
    openNoteMenuItem = new MenuItem("Text Edit Note");
    KeyCombination tkc 
        = new KeyCharacterCombination("T", KeyCombination.SHORTCUT_DOWN);
    openNoteMenuItem.setAccelerator(tkc);
    openNoteMenuItem.setOnAction(e -> openNote());
    noteMenu.getItems().add(openNoteMenuItem);
    
    // Close Note Menu Item
    closeNoteMenuItem = new MenuItem("Close Note");
    KeyCombination kkc
        = new KeyCharacterCombination("K", KeyCombination.SHORTCUT_DOWN);
    closeNoteMenuItem.setAccelerator(kkc);
    closeNoteMenuItem.setOnAction(e -> closeNoteMenuItemActionPerformed());
    noteMenu.getItems().add(closeNoteMenuItem);
    
    // Get File Info Menu Item
    getFileInfoMenuItem = new MenuItem("Get File Info...");
    KeyCombination gkc
        = new KeyCharacterCombination("G", KeyCombination.SHORTCUT_DOWN);
    getFileInfoMenuItem.setAccelerator(gkc);
    getFileInfoMenuItem.setOnAction(e -> displayFileInfo());
    noteMenu.getItems().add(getFileInfoMenuItem);
    
    // Increment Seq Menu Item
    incrementSeqMenuItem = new MenuItem("Increment Seq");
    incrementSeqMenuItem.setOnAction(e -> incrementSeq());
    noteMenu.getItems().add(incrementSeqMenuItem);
    
    //Increment Date Menu Item
    incrementDateMenuItem = new MenuItem("Increment Date");
    incrementDateMenuItem.setOnAction(e -> incrementDate());
    noteMenu.getItems().add(incrementDateMenuItem);
    
    fxUtils.addSeparator(noteMenu);
    
    // Copy Note Menu Item
    copyNoteMenuItem = new MenuItem("Copy Note");
    copyNoteMenuItem.setOnAction(e -> copyNote());
    noteMenu.getItems().add(copyNoteMenuItem);
    
    // Paste Note Menu Item
    pasteNoteMenuItem = new MenuItem("Paste Note");
    pasteNoteMenuItem.setOnAction(e -> pasteNote());
    noteMenu.getItems().add(pasteNoteMenuItem);
    
    // Copy Code Menu Item
    copyCodeMenuItem = new MenuItem("Copy Code");
    FXUtils.assignShortcut(copyCodeMenuItem, "Y");
    copyCodeMenuItem.setOnAction(e -> copyCode());
    noteMenu.getItems().add(copyCodeMenuItem);
    
    // Gen HTML Menu
    htmlMenu = new Menu("Gen HTML");
    noteMenu.getItems().add(htmlMenu);
    
    // HTML to Clipboard Menu Item
    htmlToClipboardMenuItem = new MenuItem("Copy to Clipboard");
    htmlToClipboardMenuItem.setOnAction(e -> genHTMLtoClipboard());
    htmlMenu.getItems().add(htmlToClipboardMenuItem);
    
    // HTML to File Menu Item
    htmlToFileMenuItem = new MenuItem("Save to File");
    htmlToFileMenuItem.setOnAction(e -> genHTMLtoFile());
    htmlMenu.getItems().add(htmlToFileMenuItem);
    
    incKC = new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN);
    
    //
    // Build the Edit Menu
    //
    
    // Cut Menu Item
    cutMenuItem = new MenuItem("Cut");
    cutMenuItem.setOnAction(e -> cut());
    editMenu.getItems().add(cutMenuItem);
    
    // Copy Menu Item
    copyMenuItem = new MenuItem("Copy");
    copyMenuItem.setOnAction(e -> copy());
    editMenu.getItems().add(copyMenuItem);
    
    // Paste Menu Item
    pasteMenuItem = new MenuItem("Paste");
    pasteMenuItem.setOnAction(e -> paste());
    editMenu.getItems().add(pasteMenuItem);

    editMenu.setOnShowing(e -> setCutCopyPaste());
    
    // 
    // Build the Tools Menu
    //
    
    // Build the Options Menu Item
    toolsOptionsMenuItem = new MenuItem("Options...");
    KeyCombination commakc
        = new KeyCharacterCombination(",", KeyCombination.SHORTCUT_DOWN);
    toolsOptionsMenuItem.setAccelerator(commakc);
    toolsOptionsMenuItem.setOnAction(e -> handlePreferences());
    toolsMenu.getItems().add(toolsOptionsMenuItem);
    
    // Build the Link Tweaker Menu Item
    toolsLinkTweakerMenuItem = new MenuItem("Link Tweaker...");
    KeyCombination lkc
        = new KeyCharacterCombination("L", KeyCombination.SHORTCUT_DOWN);
    toolsLinkTweakerMenuItem.setAccelerator(lkc);
    toolsLinkTweakerMenuItem.setOnAction(e -> invokeLinkTweaker());
    toolsMenu.getItems().add(toolsLinkTweakerMenuItem);
    
  }
  
  /**
   Edit Menu about to be shown, figure out which items to disable.
  */
  private void setCutCopyPaste() {
    
    prepCutCopyPaste();
    
    // Now let's see if a cut would be valid
    if (textIn != null && selectedText.length() > 0) {
      cutMenuItem.setDisable(false);
    } else {
      cutMenuItem.setDisable(true);
    }

    // Now let's see if a copy would be valid
    
    copyMenuItem.setDisable(true);
    if (textIn != null && selectedText.length() > 0) {
      copyMenuItem.setDisable(false);
    }
    else
    if (noteToCopy != null) {
      copyMenuItem.setDisable(false);
    }
    
    // Now let's see if a paste would be valid
    pasteMenuItem.setDisable(false);
    if (! model.isOpen()) {
      pasteMenuItem.setDisable(true);
    }
  }
  
  private void cut() {
    prepCutCopyPaste();
    if (objWithFocus instanceof TextInputControl) {
      textIn.cut();
    }
  }
  
  private void copy() {
    prepCutCopyPaste();
    if (objWithFocus instanceof TextInputControl) {
      textIn.copy();
    } else {
      copyNote();
    }
  }
  
  private void paste() {
    prepCutCopyPaste();
    if (objWithFocus instanceof TextInputControl) {
      textIn.paste();
    } else {
      pasteNote();
    }
    
  }
  
  private void undoEdits() {
    if (model.isOpen() && model.hasSelection()) {
      displaySelectedNote();
    }
  }
  
  private void copyCode() {
    if (model.isOpen()
        && model.hasSelection()) {
      boolean ok = false;
      boolean modOK = false;
      if (opInProgress) {
        System.out.println("Notenik.copyCode operation in progress = " 
            + String.valueOf(opInProgress));
      } else {
        modOK = modIfChanged();
      }

      if (modOK) {
        if (model.getSelection().hasCode()) {
          clipboard = Clipboard.getSystemClipboard();
          ClipboardContent content = new ClipboardContent();
          content.putString(model.getSelection().getCode());
          clipboard.setContent(content);
        }
      }
    }
  }
  
  /**
   Copy the current newNote to the system clipboard. 
   */
  private void copyNote() {
    boolean noNoteSelected = true;
    if (model.hasSelection()) {
      Note note = model.getSelection();
      if (note != null) {
        noNoteSelected = false;
        TextLineWriter writer = new ClipboardMaker();
        model.save(note, writer);
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
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.pasteNote operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    Note newNote = null;
    if (modOK) {
      ok = true;
      String noteText = "";
      TextLineReader reader = new ClipboardReader();
      newNote = model.getNote(reader);
      if (newNote == null 
          || (! newNote.hasTitle()) 
          || (! newNote.hasUniqueKey())
          || (newNote.getTitle().length() == 0)) {
        ok = false;
      }
    }
    
    if (ok) {
      String newFileName = newNote.getFileName();
        if (model.exists(newFileName)) {
          trouble.report (primaryStage, 
            "A Note already exists with the same What field",
            "Duplicate Found");
        ok = false;
      }
    }
    
    if (ok) {
      opInProgress = true;
      newNote.setLastModDateToday();
      model.add(newNote);
      model.select(newNote);
      positionAndDisplaySelection();
      opInProgress = false;
    }
    
    if (! ok) {
      trouble.report ("Trouble pasting new note from Clipboard",
          "Clipboard Error");
    }
    
  }
  
  /**
   Prepare for possible cut, copy or paste operation.
  */
  private void prepCutCopyPaste() {

    objWithFocus = primaryScene.focusOwnerProperty().get();
    textIn = null;
    noteToCopy = null;
    selectedText = "";
    if (objWithFocus instanceof TextInputControl) {
      textIn = (TextInputControl)objWithFocus;
      if (textIn != null
          && textIn.getSelectedText() != null) {
        selectedText = textIn.getSelectedText();
      }
    }
    else
    if (objWithFocus instanceof TableView
        || objWithFocus instanceof TreeView) {
      if (model.isOpen() && model.hasSelection()) {
        noteToCopy = model.getSelection();
      }
    }
    clipboard = Clipboard.getSystemClipboard();
    hasTransferableText = ((clipboard != null) 
        && clipboard.hasString());
  }
  
  private void editTemplate() {
    if (model.isOpen()) {
      model.getTemplate().showAndWait();
      reloadFile();
    }
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
    newButton.setOnAction(e -> newNote());
    toolBar.getItems().add(newButton);
        
    deleteButton = new Button("-");
    deleteButton.setTooltip(new Tooltip("Delete this note"));
    deleteButton.setOnAction(e -> removeNote());
    toolBar.getItems().add(deleteButton);
        
    firstButton = new Button("<<");
    firstButton.setTooltip(new Tooltip("Go to the first note"));
    firstButton.setOnAction(e -> firstNote());
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
    lastButton.setOnAction(e -> lastNote());
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
    editTab.setOnSelectionChanged(e -> {
        if (editTab.isSelected()) {
          startEditing();
        } else {
          doneEditing();
        }
    });
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

    listPane = new BorderPane();
    newViews();
    listTab.setContent(listPane);
    
  } // end method buildCollectionTabs
  
  /**
   Build the user interface to view and update one Note. 
  */
  private void buildNoteTabs() {
    
    displayPane = new DisplayPane(displayPrefs);
    displayTab.setContent(displayPane.getPane());
    
    editPane = new EditPane();
    editPane.addWidgets(model, linkTweaker, this, primaryStage);
    editTab.setContent(editPane.getPane());
    
    // noteTabs.getTabs().removeAll();
    // noteTabs.getTabs().add(displayTab);
    // noteTabs.getTabs().add(editTab);

    // mainSplitPane.setRightComponent(notePanel);
    // mainSplitPane.setResizeWeight(0.5);

    purgeMenuItem.setDisable(! editPane.statusIsIncluded());
    if (editPane.statusIsIncluded()
        || (editPane.dateIsIncluded() && editPane.recursIsIncluded())) {
      closeNoteMenuItem.setDisable(false);
    } else {
      closeNoteMenuItem.setDisable(true);
    }
  } // end method buildNoteTabs
  
  /**
   The list views have been refreshed for some reason, such as a change
   in sort parameters, so they should be reacquired and reapplied. 
  */
  public void newViews() {
    
    // Table View
    noteTable = model.getTableView();
    selModel = noteTable.getSelectionModel();
    selected = selModel.getSelectedIndices();
    selected.addListener((ListChangeListener.Change<? extends Integer> change) ->
      {
        tableRowSelected();
      });
    if (listPane != null) {
      listPane.setCenter(noteTable);
    }
    
    // Tree View
    noteTree = model.getTree();
    treeSelModel = noteTree.getSelectionModel();
    treeSelected = treeSelModel.getSelectedIndices();
    treeSelected.addListener(
        (ListChangeListener.Change<? extends Integer> change) ->
      {
        treeNodeSelected();
      });
    
    treePane = new BorderPane();
    
    noteTree.setMaxWidth(Double.MAX_VALUE);
    treePane.setCenter(noteTree);
    // GridPane.setVgrow(noteTree, Priority.ALWAYS);
    // GridPane.setHgrow(noteTree, Priority.ALWAYS);
    
    expandAllButton = new Button("Expand All");
    expandAllButton.setOnAction(e -> expandAllTags());
    
    collapseAllButton = new Button("Collapse All");
    collapseAllButton.setOnAction(e -> collapseAllTags());
    
    treeButtonsPane = new HBox(10, expandAllButton, collapseAllButton);
    
    treePane.setBottom(treeButtonsPane);
    BorderPane.setMargin(treeButtonsPane, new Insets(10));
    
    tagsTab.setContent(treePane);
    
    boolean hasDate = model.getRecDef().contains(NoteParms.DATE_FIELD_NAME);
    boolean hasSeq = model.getRecDef().contains(NoteParms.SEQ_FIELD_NAME);
    int sortParm = model.getSortParm().getParm();
    boolean seqSort = (sortParm == NoteSortParm.SORT_BY_SEQ_AND_TITLE);
    boolean dateSort = (sortParm == NoteSortParm.SORT_TASKS_BY_DATE);
    int incField = 0;
    if (hasDate && (! hasSeq)) {
      incField = 1;
    }
    else
    if (hasSeq && (! hasDate)) {
      incField = 2;
    }
    else
    if (seqSort) {
      incField = 2;
    }
    else
    if (dateSort) {
      incField = 1;
    } 
    else
    if (hasDate && hasSeq) {
      incField = 1;
    }
    
    incrementDateMenuItem.setAccelerator(null);
    incrementSeqMenuItem.setAccelerator(null);
    
    if (incField == 1) {
      incrementDateMenuItem.setAccelerator(incKC);
    }
    else
    if (incField == 2) {
      incrementSeqMenuItem.setAccelerator(incKC);
    }
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
    if (selectedFile != null) {
      int filesSaved = model.createMasterCollection(selectedFile);
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
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.OpenMasterCollection operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      boolean masterOpened = false;
      String errMsg = null;
      if (model.getMaster().hasMasterCollection()) {
        try {
          masterOpened = model.openMasterCollection();
          newCollection();
        } catch (NoteCollectionException e) {
          errMsg = e.getMessage();
        }
      }
      if (errMsg == null && (! masterOpened)) {
        errMsg = "No Master Collection to Open";
      }
      if (! masterOpened) {
          Trouble.getShared().report (
              errMsg,
              "Master Collection Open Error");
        }
    } // end if mod ok
  }
  
  /**
   Open the last collection opened. 
  */
  private void jumpToLastCollection() {
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.jumpToLastCollection operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      RecentFiles recentFiles = model.getMaster().getRecentFiles();
      boolean found = false;
      int i = 0;
      FileSpec jumpSpec = null;
      FileSpec currentSpec = model.getFileSpec();
      FileSpec masterSpec = null;
      if (model.getMaster().hasMasterCollection()) {
        File masterFile = model.getMaster().getMasterCollectionFolder();
        masterSpec = model.getMaster().getFileSpec(masterFile);
      }
      
      while (i < recentFiles.size() && (! found)) {
        jumpSpec = recentFiles.get(i);
        if (jumpSpec.equals(currentSpec)
            || jumpSpec.equals(masterSpec)) {
          i++;
        } else {
          found = true;
        }
      }

      if (found 
          && jumpSpec != null 
          && NoteCollectionModel.goodFolder(jumpSpec.getFolder())) {
        closeFile();
        openFile (jumpSpec, false);
      } // end if we have a good collection to jump to
    } // end if mod ok
  }
  
  /** 
   Open the collection so important that the user has deemed it to be
   "Essential". 
  */
  private void openEssentialCollection() {
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.openEssentialCollection operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      if (filePrefs.hasEssentialFilePath()) {
        File selectedFile = new File(filePrefs.getEssentialFilePath());
        if (NoteCollectionModel.goodFolder(selectedFile)) {
          closeFile();
          openFile (selectedFile);
        } else {
          trouble.report ("Trouble opening file " + selectedFile.toString(),
              "File Open Error");
        }
      } // end if we have an essential file path
    } // end if mod ok
  }
  
  /**      
    Standard way to respond to a document being passed to this application on a Mac.
   
    @param inFile File to be processed by this application, generally
                  as a result of a file or directory being dragged
                  onto the application icon.
   */
  public void handleOpenFile (File inFile) {
    FileSpec fileSpec = model.getMaster().getFileSpec(inFile);
    if (fileSpec == null) {
      fileSpec = new FileSpec(inFile);
    }
    handleOpenFile(fileSpec);
  }
  
  public void handleOpenFile (FileSpec fileSpec) {
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.handleOpenFile operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      if (NoteCollectionModel.goodFolder(fileSpec.getFolder())) {
        closeFile();
        openFile (fileSpec, false);
      }
    }
  }
  /**
   Close the current notes collection in an orderly fashion. 
  */
  private void closeFile() {
   
    savePrefs();
    if (model.isOpen()) {
      publishWindow.closeSource();
      model.close();
    }
    noteDisplayed = false;
  }
  
  /**
   Provide a static, non-editable display of the note on the display tab. 
  */
  private void buildDisplayTab() {
    
    displayPane.startDisplay();

    String workTitle = model.getSelection().getFieldData(NoteParms.WORK_TITLE);
    String workRights = model.getSelection().getFieldData(NoteParms.WORK_RIGHTS);
    String workRightsHolder = model.getSelection().getFieldData(NoteParms.WORK_RIGHTS_HOLDER);

    if (model.getSelection().hasTags()) {
      displayPane.displayTags(model.getSelection().getTags());
    }
    
    displayPane.displayTitle(model.getSelection().getTitle());
    
    if (model.getSelection().hasLink()) {
      if (model.editingMasterCollection()) {
        displayPane.displayLink(
          this,
          NoteParms.LINK_FIELD_NAME, 
          "", 
          model.getSelection().getLinkAsString());
      } else {
        displayPane.displayLink(
          null,
          NoteParms.LINK_FIELD_NAME, 
          "", 
          model.getSelection().getLinkAsString());
      }
    }

    int fieldsDisplayed = 0;
    if (editPane.getNumberOfFields() == model.getNumberOfFields()) {
      for (int i = 0; i < model.getNumberOfFields(); i++) {
        DataFieldDefinition fieldDef = model.getRecDef().getDef(i);
        String fieldName = fieldDef.getProperName();
        DataWidget widget = editPane.get(i);
        // DataField nextField = model.getSelection().getField(i);
        DataField nextField = model.getSelection().getField(i, fieldName);
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
        else
        if (fieldName.equalsIgnoreCase(NoteParms.AUTHOR_FIELD_NAME)
            || fieldName.equalsIgnoreCase(NoteParms.AUTHOR_INFO)
            || fieldName.equalsIgnoreCase(NoteParms.AUTHOR_LINK)) {
          // Ignore -- handled above
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.WORK_TITLE)
            || fieldName.equalsIgnoreCase(NoteParms.WORK_TYPE)
            || fieldName.equalsIgnoreCase(NoteParms.WORK_MINOR_TITLE)
            || fieldName.equalsIgnoreCase(NoteParms.WORK_PAGE_NUMBERS)
            || fieldName.equalsIgnoreCase(NoteParms.WORK_IDENTIFIER)
            || fieldName.equals(NoteParms.WORK_LINK)
            || fieldName.equalsIgnoreCase(NoteParms.WORK_RIGHTS)
            || fieldName.equalsIgnoreCase(NoteParms.WORK_RIGHTS_HOLDER)
            || fieldName.equalsIgnoreCase(NoteParms.PUBLISHER)
            || fieldName.equalsIgnoreCase(NoteParms.PUBLISHER_CITY)) {
          // Ignore -- handled below
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.CODE_FIELD_NAME)) {
          displayPane.displayCode(fieldName, nextField.getData());
        }
        else
          if (fieldName.equalsIgnoreCase(NoteParms.DATE_ADDED_FIELD_NAME)) {
          // Ignore -- handled below
          }
        else
          if (fieldName.equalsIgnoreCase(NoteParms.DATE_FIELD_NAME)
            && (workTitle != null && workTitle.length() > 0)) {
              // Ignore -- handled below
          }
        else {
          displayPane.displayField(fieldName, nextField.getData());
          fieldsDisplayed++;
        }

      } // end for each data field
    
      if (model.getSelection().hasBody()) {
        if (fieldsDisplayed > 0) {
          displayPane.displayLabelOnly("Body");
        }
        displayPane.displayBody(model.getSelection().getBody());
      }

      displayPane.displayDivider();
    
    }
    
    if (model.getSelection().hasAuthor()) {
      Author author = model.getSelection().getAuthor();
      String authorInfo 
          = model.getSelection().getFieldData(NoteParms.AUTHOR_INFO);
      if (authorInfo != null && authorInfo.length() > 0) {
        author.setAuthorInfo(authorInfo);
      }
      String authorLink 
          = model.getSelection().getFieldData(NoteParms.AUTHOR_LINK);
      if (authorLink != null && authorLink.length() > 0) {
        author.setLink(authorLink);
      }
      displayPane.displayAuthor(author);
    }

    if (workTitle != null && workTitle.length() > 0) {
      WisdomSource source = new WisdomSource(workTitle);
      Note wisdom = model.getSelection();
      
      String workType = wisdom.getFieldData(NoteParms.WORK_TYPE);
      if (workType != null && workType.length() > 0) {
        source.setType(workType);
      }
      
      String workMinorTitle = wisdom.getFieldData(NoteParms.WORK_MINOR_TITLE);
      if (workMinorTitle != null && workMinorTitle.length() > 0) {
        source.setMinorTitle(workMinorTitle);
      }
      
      String workID = wisdom.getFieldData(NoteParms.WORK_IDENTIFIER);
      if (workID != null && workID.length() > 0) {
        source.setID(workID);
      }
      
      String workLink = wisdom.getFieldData(NoteParms.WORK_LINK);
      if (workLink != null && workLink.length() > 0) {
        source.setLink(workLink);
      }
      
      if (workRights != null && workRights.length() > 0) {
        source.setRights(workRights);
      }
      
      if (workRightsHolder != null && workRightsHolder.length() > 0) {
        source.setRightsOwner(workRightsHolder);
      }
      
      String publisher = wisdom.getFieldData(NoteParms.PUBLISHER);
      if (publisher != null && publisher.length() > 0) {
        source.setPublisher(publisher);
      }
      
      String pubCity = wisdom.getFieldData(NoteParms.PUBLISHER_CITY);
      if (pubCity != null && pubCity.length() > 0) {
        source.setCity(pubCity);
      }

      if (wisdom.hasDate()) {
        source.setYear(wisdom.getDateAsString());
      }
      
      String pages = wisdom.getFieldData(NoteParms.WORK_PAGE_NUMBERS);
      
      displayPane.displaySource(source, pages);

      if (source.hasRights() || source.hasRightsOwner() || source.hasYear()) {
        displayPane.displayRights(source.getRights(), source.getYear(), source.getRightsOwner());
      }
    }
    
    displayPane.displayDateAdded(model.getSelection().getDateAddedAsString());
    
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
    if (! urlUnionFile.toString().equals(model.getFolder().toString())) {
      exporter.exportToURLUnion (urlUnionFile, model);
      urlUnionWritten = true;
    }
    return urlUnionWritten;
  }

  private boolean publishFavorites (File publishTo) {
    
    // Publish selected favorites
    Logger.getShared().recordEvent(LogEvent.NORMAL, "Publishing Favorites", false);
    favoritesWritten = false;
    if (! model.getFolder().getName().equalsIgnoreCase (FAVORITES_FILE_NAME)) {
      favoritesWritten = exporter.publishFavorites
          (publishTo, model, favoritesPrefs);
    }
    return favoritesWritten;
  }

  private boolean publishNetscape (File publishTo) {
    // Publish in Netscape bookmarks format
    netscapeWritten = false;
    if (! model.getFolder().getName().equalsIgnoreCase (NETSCAPE_BOOKMARKS_FILE_NAME)) {
      File netscapeFile = new File (publishTo,
        NETSCAPE_BOOKMARKS_FILE_NAME);
      exporter.publishNetscape (netscapeFile, model);
      netscapeWritten = true;
    }
    return netscapeWritten;
  }

  private boolean publishOutline (File publishTo) {
    // Publish in outline form using dynamic html
    outlineWritten = false;
    if (! model.getFolder().getName().equalsIgnoreCase (OUTLINE_FILE_NAME)) {
      File dynamicHTMLFile = new File (publishTo, OUTLINE_FILE_NAME);
      exporter.publishOutline(dynamicHTMLFile, model);
      outlineWritten = true;
    }
    return outlineWritten;
  }

  private boolean publishIndex (File publishTo) {
    // Publish index file pointing to other files
    indexWritten = false;
    if (! model.getFolder().getName().equalsIgnoreCase (INDEX_FILE_NAME)) {
      File indexFile = new File (publishTo, INDEX_FILE_NAME);
      exporter.publishIndex(indexFile, null,
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
    
    boolean ok;
    model.setSyncPrefs(folderSyncPrefs.getFolderSyncPrefsData());
    try {
      ok = model.syncWithFolder();
    } catch (IOException e) {
      ok = false;
    }
    if (! ok) {
      Trouble.getShared().report(
            primaryStage, 
            "Trouble syncing with folder: " + folderSyncPrefs.getSyncFolder(), 
            "Problem with Sync Folder");
    }
    return ok;
  }
  
  /**
   Prompt the user for a backup location, then backup and prune if he
   provides one. 

   @return True if backup was successful.
  */
  public boolean promptForBackup() {
    
    BackupInfo backupInfo = model.getBackupInfo();
    
    if (model.isOpen()) {
      boolean modOK = false;
      if (opInProgress) {
        System.out.println("Notenik.promptForBackup operation in progress = " 
            + String.valueOf(opInProgress));
      } else {
        modOK = modIfChanged();
      }
      
      if (modOK) {
        File selectedFile = backupInfo.letUserChooseBackupFile(primaryStage);
        if (backupInfo.okSoFar()) {
          backupInfo.backupToZip();
          model.setBackupFolder(backupInfo.getBackupFolder());
          if (backupInfo.okSoFar()) {
            model.saveLastBackupDate();
            backupInfo.pruneBackups();
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Backup Results");
            alert.setHeaderText("Collection successfully backed up to:");
            alert.setContentText(
                "Folder: " + backupInfo.getBackupFolder().toString()
                + GlobalConstants.LINE_FEED_STRING
                + "File: " + backupInfo.getBackupFileName());
            alert.showAndWait();
          } // end if backed up successfully
        } // end if the user selected a backup location
      } // end if modIfChanged had no problems
    }

    return backupInfo.backupSuccess();
  }
  
  /**
   Backup without prompting the user. 
  
   @return True if backup was successful. 
  */
  public boolean backupWithoutPrompt() {
    return model.backupZipWithoutPrompt();
  }
  
 /**
   Check to see if the user has changed anything and take appropriate
   actions if so.
   */
  private boolean modIfChanged () {
    
    opInProgress = true;
    boolean modOK = true;
    
    if (model.isOpen() 
        && model.hasSelection() 
        && noteDisplayed
        && displayedID >= 0
        && displayedID == model.getSelectedID()) {
      checkFieldsForChanges();

      // If entry has been modified, then let's update if we can
      if (modified) {
        String newFileName = model.getSelection().getFileName();
        
        // Got to have a title
        if ((! model.getSelection().hasTitle()) 
            || model.getSelection().getTitle().length() == 0) {
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
        if (model.uniqueKeyChanged()
            && model.contains(model.getSelection().getUniqueKey())) {
          Trouble.getShared().report (primaryStage, 
              "Another Note already exists with the same Title",
              "Duplicate Found");
          modOK = false;
        }
        else
        // If we changed the title, then check to see if we have another 
        // Note by the same name
        if (model.fileNameChanged()
            && model.selectedExists()) {
          Trouble.getShared().report (primaryStage, 
              "A Note already exists with the same File Name on disk",
              "Duplicate Found");
          modOK = false;
        } else {
          // Modify newNote on disk
          model.modifySelection();
          model.updateSelection();
        }
      } // end if modified
    }
    modified = false;
    opInProgress = false;
    return modOK;
  } // end modIfChanged method
  
  /**
   Check each field to check for any changes made by the user. If changes, 
   then set modified flag to true, and update the selected note accordingly.
  */
  private void checkFieldsForChanges() {
    for (int i = 0; i < model.getNumberOfFields(); i++) {
      DataFieldDefinition fieldDef = model.getRecDef().getDef(i);
      String fieldName = fieldDef.getProperName();
      if (i < editPane.getNumberOfFields()) {
        DataWidget widget = editPane.get(i);
        if (fieldName.equalsIgnoreCase(NoteParms.TITLE_FIELD_NAME)) {
          if (! model.getSelection().equalsTitle (widget.getText())) {
            String oldTitle = model.getSelection().getTitle();
            model.getSelection().setTitle (widget.getText());
            modified = true;
          }
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.LINK_FIELD_NAME)) {
          if ((widget.getText().equals (model.getSelection().getLinkAsString()))
              || ((widget.getText().length() == 0) 
                && model.getSelection().blankLink())) {
            // No change
          } else {
            model.getSelection().setLink (widget.getText());
            modified = true;
          }
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.TAGS_FIELD_NAME)) {
          if (! model.getSelection().equalsTags (widget.getText())) {
            model.getSelection().setTags (widget.getText());
            modified = true;
          }
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.BODY_FIELD_NAME)) {
          if (! widget.getText().equals (model.getSelection().getBody())) {
            model.getSelection().setBody (widget.getText());
            modified = true;
          }
        } 
        else
        if (fieldName.equalsIgnoreCase(NoteParms.SEQ_FIELD_NAME)) {
          if (! widget.getText().equals (model.getSelection().getSeq())) {
            model.getSelection().setSeq (widget.getText());
            modified = true;
          }
        } 
        else
        if (fieldName.equalsIgnoreCase(NoteParms.STATUS_FIELD_NAME)) {
          ItemStatus statusValue = new ItemStatus(widget.getText());
          if (model.getSelection().getStatus().compareTo(statusValue) != 0) {
            model.getSelection().setStatus (widget.getText());
            modified = true;
          }
        } 
        else
        if (fieldName.equalsIgnoreCase(NoteParms.RECURS_FIELD_NAME)) {
          RecursValue recursValue = new RecursValue(widget.getText());
          if (model.getSelection().getRecurs().compareTo(recursValue) != 0) {
            model.getSelection().setRecurs (widget.getText());
            modified = true;
          }
        }  
        else
        if (fieldName.equalsIgnoreCase(NoteParms.DATE_FIELD_NAME)) {
          String newDate = widget.getText();
          if (model.getSelection().getDateAsString().compareTo(newDate) != 0) {
            model.getSelection().setDate(newDate);
            modified = true;
          }
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.CODE_FIELD_NAME)) {
          String code = widget.getText();
          model.getSelection().setCode(code);
          modified = true;
        }
        else {
          DataField nextField = model.getSelection().getField(i);
          if (! widget.getText().equals(nextField.getData())) {
            model.getSelection().storeField(fieldName, widget.getText());
            modified = true;
          } // end if generic field has been changed
        } // end if generic field
      } // end if we have a widget
    } // end for each field
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
    
    DataValueSeq newSeq = null;
    if (model.getSortParm().getParm() == NoteSortParm.SORT_BY_SEQ_AND_TITLE
        && model.hasSelection()
        && model.getSelection().hasSeq()) {
      newSeq = new DataValueSeq(model.getSelectedSeq().toString());
      boolean incrementingOnLeft = (newSeq.getPositionsToRightOfDecimal() == 0);
      newSeq.increment(incrementingOnLeft);
    }
    
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.newNote operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }

    if (modOK) {
      Note newNote = model.getNewNote();
      // boolean seqSet = false;
      if (newSeq != null) {
        newNote.setSeq(newSeq.toString());
        // seqSet = true;
      }

      model.select(newNote);
      displaySelectedNote();
      editPane.setTags(model.getSelectedTags());
      // if (seqSet) {
      //   editPane.setSeq(newNote.getSeq());
      // }
      noteTabs.getSelectionModel().select(EDIT_TAB_INDEX);
      // oldSeq = "";
    }
  }
  
  /**
   Duplicate the currently displayed note.
   */
  public void duplicateNote() {

    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.duplicateNote operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }

    if (model.hasSelection() && modOK) {
      Note newNote = new Note(model.getSelection());
      String copyTitle = model.getSelectedTitle() + " copy";
      newNote.setTitle("");
      model.select(newNote);
      model.getSelection().setTitle(copyTitle);
      displaySelectedNote();
      noteTabs.getSelectionModel().select(EDIT_TAB_INDEX);
      statusBar.setPosition(model.getSelectedSortIndex() + 1, model.sortedSize());
    }
  }

  /**
   Delete the note currently displayed and selected.
  */
  private void removeNote () {
    if (model.selectionIsNew()) {
      System.out.println ("New Note -- ignoring delete command");
    } else {
      boolean okToDelete = true;
      String titleToDelete = model.getSelection().getTitle();
      if (generalPrefs.confirmDeletes()) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Really delete Note titled " 
            + model.getSelection().getTitle() + "?");
        Optional<ButtonType> result = alert.showAndWait();
        okToDelete = (result.get() == ButtonType.OK);
      }
      if (okToDelete) {
        noFindInProgress();
        opInProgress = true;
        String nextTitle = model.nextTitle();
        boolean deleted = model.removeSelection();
        if (deleted) {
          model.select(nextTitle);
          positionAndDisplaySelection();
        } else {
          trouble.report(
              "Trouble deleting note titled " + titleToDelete, 
              "Delete Problem");
        }
        opInProgress = false;
      } // end if user confirmed delete
    } // end if new URL not yet saved
  } // end method removeNote

  private void checkTags() {
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.checkTags operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      TagsChangeScreen replaceScreen = new TagsChangeScreen
          (primaryStage, true, model.getTagsList(), this);
      replaceScreen.setLocation (
          primaryStage.getX() + CHILD_WINDOW_X_OFFSET,
          primaryStage.getY() + CHILD_WINDOW_Y_OFFSET);
      replaceScreen.setVisible (true);
    }
  }

  /**
   Called from TagsChangeScreen.
   @param from The from String.
   @param to   The to String.
   */
  public void changeAllTags (String from, String to) {

    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.changeAllTags operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }

    if (modOK) {
      initialSelection = model.getSelection();
      int mods = 0;
      for (int workIndex = model.firstNote(); 
          (workIndex >= 0 && workIndex < model.size()); 
          workIndex = model.nextNote(workIndex)) {
        model.select(workIndex);
        model.getSelection().getTags().replace (from, to);
        if (model.tagsChanged()) {
          mods++;
          model.modifySelection();
        }
      }
      
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Tags Replacement Results");
      alert.setHeaderText(null);
      alert.setContentText(String.valueOf (mods)
            + " tags changed");
      alert.showAndWait();

      selectPositionAndDisplay(initialSelection);
    }
  }

  private void flattenTags() {
    
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.flattenTags operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }

    if (modOK) {
      initialSelection = model.getSelection();
      int mods = 0;
      for (int workIndex = model.firstNote(); 
          workIndex >= 0 && workIndex < model.size(); 
          workIndex = model.nextNote(workIndex)) {
        model.select(workIndex);
        model.getSelection().flattenTags();
        if (model.tagsChanged()) {
          mods++;
          model.modifySelection();
        }
      }
      
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Flatten Tags Results");
      alert.setHeaderText(null);
      alert.setContentText(String.valueOf (mods)
            + " tags flattened");
      alert.showAndWait();
      
      selectPositionAndDisplay(initialSelection);
    }
  }

  private void lowerCaseTags() {
    
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.lowerCaseTags operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }

    if (modOK) {
      initialSelection = model.getSelection();
      int mods = 0;
      for (int workIndex = model.firstNote(); 
          workIndex >= 0 && workIndex < model.size(); 
          workIndex = model.nextNote(workIndex)) {
        model.select(workIndex);
        model.getSelection().lowerCaseTags();
        if (model.tagsChanged()) {
          mods++;
          model.modifySelection();
        }
      }
      
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Lower Case Tags Results");
      alert.setHeaderText(null);
      alert.setContentText(String.valueOf (mods)
            + " tags changed to lower case");
      alert.showAndWait();
      
      selectPositionAndDisplay(initialSelection);
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
    Find the next Note containing the search string, or position the cursor
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
    Find the specified text string within the list of Notes. This method may
    be called internally, or from the ReplaceWindow. The result will be to 
    position the displays on the item found, or display a message to the user
    that no matching item was found. 
  
    @param findButtonText Either "Find" or "Again", indicating whether we
                          are starting a new search or continuing an 
                          existing one. 
    @param findString  The string we're searching for. 
    @param checkTitle  Should we check the title of the Note?
    @param checkLink    Should we check the URL of the Note?
    @param checkTags   Should we check the tags of the Note?
    @param checkBody Should we check the body?
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
        
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.findNote operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    boolean found = false;
    if (modOK) {
      String notFoundMessage;
      if (findString != null && findString.length() > 0) {
        if (findButtonText.equals (FIND)) {
          lastGoodTitle = model.getSelectedTitle();
          notFoundMessage = "No Notes Found";
          listPosition = model.firstNote();
        } else {
          notFoundMessage = "No further Notes Found";
          listPosition = model.nextNote(listPosition);
        }
        String findLower = findString.toLowerCase();
        String findUpper = findString.toUpperCase();
        while (listPosition >= 0
            && listPosition < model.size() 
            && (! found)) {
          Note noteCheck = model.get(listPosition);
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
            listPosition = model.nextNote(listPosition);
          }
        } // while still looking for next match
        if (found) {
          findInProgress();
          lastTextFound = findString;
          selectPositionAndDisplay(foundNote);
          lastGoodTitle = model.getSelectedTitle();
          statusBar.setStatus("Matching Note found");
        } else {
          PSOptionPane.showMessageDialog(primaryStage,
              notFoundMessage,
              "Not Found",
              javax.swing.JOptionPane.WARNING_MESSAGE);
          noFindInProgress();
          lastTextFound = "";
          statusBar.setStatus(notFoundMessage);
          Note lastGoodNote = model.getFromTitle(lastGoodTitle);
          foundNote = null;
          selectPositionAndDisplay(lastGoodNote);
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
    
    opInProgress = true;
    boolean replaced = false;
    if (foundNote != null 
        && foundNote.equals(model.getSelection())) {
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
        model.modifySelection();
        positionAndDisplaySelection();
        statusBar.setStatus("Replacement made");
      }
    }
    opInProgress = false;
    return replaced;
  }
  
  /* ===========================================================================
  
    Routines to select, position and display a note, where select means to 
    communicate the selection to the model, position means to select the
    note on the table and tree views, and display means to show the note's 
    field's on the display and edit tabs. 
  
  *  ======================================================================== */
  
  /**
   Select, position and display the first note in the sorted list. 
  */
  public void firstNote () {
    boolean ok = true;
    if (model.isOpen()) {
      if (model.hasSelection()) {
        ok = false;
        if (opInProgress) {
          System.out.println("Notenik.firstNote operation in progress = " 
              + String.valueOf(opInProgress));
        } else {
          ok = modIfChanged();
        }
        if (ok) {
          noFindInProgress();
          String titleToSelect = model.firstTitle();
          model.select(titleToSelect);
          positionAndDisplaySelection();
        }
      }
    }
  }

  /**
   Select, position and display the prior note in the sorted list. 
  */
  public void priorNote () {
    boolean ok = true;
    if (model.isOpen()) {
      if (model.hasSelection()) {
        ok = false;
        if (opInProgress) {
          System.out.println("Notenik.priorNote operation in progress = " 
              + String.valueOf(opInProgress));
        } else {
          ok = modIfChanged();
        }
        if (ok) {
          noFindInProgress();
          String titleToSelect = model.priorTitle();
          model.select(titleToSelect);
          positionAndDisplaySelection();
        }
      }
    }
  }

  /**
   Select, position and display the next note in the sorted list. 
  */
  public void nextNote() {
    
    boolean ok = true;
    if (model.isOpen()) {
      if (model.hasSelection()) {
        ok = false;
        if (opInProgress) {
          System.out.println("Notenik.nextNote operation in progress = " 
              + String.valueOf(opInProgress));
        } else {
          ok = modIfChanged();
        }
        if (ok) {
          noFindInProgress();
          String titleToSelect = model.nextTitle();
          model.select(titleToSelect);
          positionAndDisplaySelection();
        }
      }
    }
  }

  /**
   Select, position and display the last note in the sorted list. 
  */
  public void lastNote() {
    boolean ok = true;
    if (model.isOpen()) {
      if (model.hasSelection()) {
        ok = false;
        if (opInProgress) {
          System.out.println("Notenik.lastNote operation in progress = " 
              + String.valueOf(opInProgress));
        } else {
          ok = modIfChanged();
        }
        if (ok) {
          noFindInProgress();
          String titleToSelect = model.lastTitle();
          model.select(titleToSelect);
          positionAndDisplaySelection();
        }
      }
    }
  }
  
  private void selectPositionAndDisplay(Note note) {
    if (model.isOpen() && note != null) {
      model.select(note);
      positionAndDisplaySelection();
    }
  }

  private void positionAndDisplaySelection () {
    if (model.isOpen() && model.hasSelection()) {
      positionSelection();
      displaySelectedNote();
      statusBar.setPosition(model.getSelectedSortIndex() + 1, model.sortedSize());
    }
  }
  
  /**
   Position the table and tree views to select the selected note. 
  */
  private void positionSelection() {
    if (model.hasSelection()) {
      position(model.getSelection());
    }
  }
  
  /**
   Try to position the table view and the tree view to select the given note.
  
   @param noteToSelect The note to select within the two views. 
  */
  private void position(Note noteToSelect) {
    
    opInProgress = true;
    // Let's try to select the note within the TableView
    SortedNote sortedNote = model.getSortedNote(noteToSelect);
    if (sortedNote != null) {
      noteTable.getSelectionModel().clearSelection();
      noteTable.getSelectionModel().select(sortedNote);
      int selectedIndex = noteTable.getSelectionModel().getSelectedIndex();
      // noteTable.scrollTo(selectedIndex);
    }
    
    // Now let's try to select the note within the TreeView
    TreeItem firstNode = noteToSelect.getTagsNode();
    if (firstNode != null) {
      noteTree.getSelectionModel().clearSelection();
      noteTree.getSelectionModel().select(firstNode);
    }
    opInProgress = false;
  }

  /**
   Respond when the user clicks on a row in the Note list.
   */
  private void tableRowSelected () {
    int selectedRow = noteTable.getSelectionModel().getSelectedIndex();
    SortedNote selectedNote = (SortedNote)noteTable.getSelectionModel().getSelectedItem();
    if (selectedRow >= 0 
        && selectedRow < model.sortedSize()
        && selectedNote != null) {
      boolean modOK = false;
      if (opInProgress) {
        // System.out.println("Notenik.tableRowSelected mod in linksChecked = " 
        //     + String.valueOf(opInProgress));
      } else {
        modOK = modIfChanged();
      }
      if (modOK) {
        model.select(selectedNote.getNote());
        TreeItem firstNode = selectedNote.getNote().getTagsNode();
        if (firstNode != null) {
          noteTree.getSelectionModel().clearSelection();
          noteTree.getSelectionModel().select(firstNode);
        }
        statusBar.setPosition(model.getSelectedSortIndex() + 1, model.sortedSize());
        displaySelectedNote();
      }
    }
  }

  /**
   Respond when user selects a note from the tags tree.
   */
  private void treeNodeSelected () {

    TreeItem<TagsNodeValue> node = (TreeItem)noteTree.getSelectionModel().getSelectedItem();

    if (node == null) {
      // nothing selected
    } else {
      TagsNodeValue nodeValue = node.getValue();
      if (nodeValue.getNodeType() == TagsNodeValue.ITEM) {
        boolean modOK = false;
        if (opInProgress) {
          // System.out.println("Notenik.treeNodeSelected operation in progress = " 
          //   + String.valueOf(opInProgress));
        } else {
          modOK = modIfChanged();
        }
        if (modOK) {
          Note branch = (Note)nodeValue.getTaggable();
          model.select(branch);
          SortedNote sortedNote = model.getSortedNote(branch);
          if (sortedNote != null) {
            noteTable.getSelectionModel().clearSelection();
            noteTable.getSelectionModel().select(sortedNote);
            int selectedIndex = noteTable.getSelectionModel().getSelectedIndex();
            // noteTable.scrollTo(selectedIndex);
          }
          displaySelectedNote();
          statusBar.setPosition(model.getSelectedSortIndex() + 1, model.sortedSize());
        }
      }
    }
  } // end method treeNodeSelected
  
  private void expandAllTags() {
    model.expandAll();
  }
  
  private void collapseAllTags() {
    model.collapseAll();
  }
  
  public void displayPrefsUpdated(DisplayPrefs displayPrefs) {
    if (model.isOpen()
        && model.hasSelection()
        && displayTab != null) {
      buildDisplayTab();
    }
  }

  /**
   Populate both the Display and Edit tabs with data from the current note. 
  */
  private void displaySelectedNote () {
    
    buildDisplayTab();
    
    if (editPane.getNumberOfFields() == model.getNumberOfFields()) {
      for (int i = 0; i < model.getNumberOfFields(); i++) {
        DataFieldDefinition fieldDef = model.getRecDef().getDef(i);
        String fieldName = fieldDef.getProperName();
        DataWidget widget = editPane.get(i);
        if (fieldName.equalsIgnoreCase(NoteParms.TITLE_FIELD_NAME)) {
          widget.setText(model.getSelection().getTitle());
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.LINK_FIELD_NAME)) {
          widget.setText(model.getSelection().getLinkAsString());
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.TAGS_FIELD_NAME)) {
          widget.setText(model.getSelection().getTagsAsString());
        }
        else
        if (fieldName.equalsIgnoreCase(NoteParms.BODY_FIELD_NAME)) {
          widget.setText(model.getSelection().getBody());
        } 
        else {
          DataField nextField = model.getSelection().getField(i, fieldName);
          widget.setText(nextField.getData());
        }

      } // end for each data field
      noteDisplayed = true;
      displayedID = model.getSelection().getCollectionID();
    }
    
    editPane.setLastModDate(model.getSelection().getLastModDate(NoteParms.COMPLETE_FORMAT));
    modified = false;
    
    if (model.getSelection().hasInconsistentDiskLocation()) {
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Title/File Name Mismatch");
      alert.setHeaderText("The Note's file name does not match its title");
      alert.setContentText("File Name: " + model.getSelection().getDiskLocationBase()
          + GlobalConstants.LINE_FEED_STRING
          + "Simplified Title: " + model.getSelection().getFileName());

      ButtonType changeFileName = new ButtonType("Change file name to match title");
      ButtonType leaveFileName = new ButtonType("Leave it as is", ButtonData.CANCEL_CLOSE);

      alert.getButtonTypes().setAll(changeFileName, leaveFileName);

      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == changeFileName){
        System.out.println ("OK, let's fix it!");
        model.saveSelectionAndDeleteOnRename();
      } 
    }
    
    // noteList.fireTableRowsUpdated(position.getIndex(), position.getIndex());
    
  }
  
  private void startEditing() {
    if (model.isOpen()
        && model.hasSelection()
        && editTab != null) {
      activateEditTab();
    }
  }
  
  /**
   User has pressed the OK button to indicate that they are done editing. 
   */
  private void doneEditing() {
    if (model.isOpen()
        && model.hasSelection()
        && displayTab != null) {
      boolean modOK = false;
      if (opInProgress) {
        System.out.println("Notenik.doneEditing operation in progress = " 
            + String.valueOf(opInProgress));
      } else {
        modOK = modIfChanged();
      }
      if (modOK) {
        positionAndDisplaySelection();
        activateDisplayTab();
      }
    }
  } 
  
  /**
    Changes the active tab to the tab displaying an individual item.
   */
  public void activateDisplayTab () {
    if (noteTabs.getSelectionModel().getSelectedIndex() != DISPLAY_TAB_INDEX) {
      noteTabs.getSelectionModel().select(DISPLAY_TAB_INDEX);
    }
    okButton.setDisable(true);
  }
  
  /**
    Changes the active tab to the tab displaying an individual item.
   */
  public void activateEditTab () {
    if (noteTabs.getSelectionModel().getSelectedIndex() != EDIT_TAB_INDEX) {
      noteTabs.getSelectionModel().select(EDIT_TAB_INDEX);
    }
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
        if (model.isOpen() && model.hasSelection()) {
          Note testNote = model.getSelection();
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
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.tableRowSelected operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    boolean ok = modOK;
    Note noteToOpen = null;
    File noteFileToOpen = null;
    String noteTitle = "** Unknown **";
    
    if (! model.hasSelection()) {
      ok = false;
    }
    
    if (ok) {
      noteToOpen = model.getSelection();
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

    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.handleQuit operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
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
    if (model.isOpen()) {
      userPrefs.setPref (FavoritesPrefs.LAST_FILE, model.getFolder().toString());
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
    model.savePrefs();
    // tweakerPrefs.savePrefs();
  }

  /**
   Let the user choose a folder to open.
   */
  private void userOpenFile() {
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.userOpenFile operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      dirChooser.setTitle ("Open Notes Collection");
      if (model.isOpen()) {
        dirChooser.setInitialDirectory (currentDirectory);
      }

      File selectedFile = null;
      selectedFile = dirChooser.showDialog(primaryStage);
      if (selectedFile != null) {
        if (NoteCollectionModel.goodFolder(selectedFile)) {
          closeFile();
          openFile (selectedFile);
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

    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.openHelpNotes operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      File appFolder = Home.getShared().getAppFolder();
      File helpFolder = new File (appFolder, "help");
      File helpNotes = new File (helpFolder, "notenik-intro");
      if (NoteCollectionModel.goodFolder(helpNotes)) {
        closeFile();
        openFile (helpNotes);
        model.getSortParm().setParm(NoteSortParm.SORT_BY_SEQ_AND_TITLE);
        firstNote();
      }
    }
  }
  
  private void launchButtonClicked() {
    if (model.editingMasterCollection()) {
      openCollectionFromCurrentNote();
    } else {
      openURL (editPane.getLink());
    }
  }
  
  public void launchButtonPressed(String link) {
    launchButtonClicked();
  }
  
  private void openCollectionFromCurrentNote () {
    
    if (model.isOpen() && model.hasSelection()) {
      boolean modOK = modIfChanged();
      if (modOK) {
        File fileToOpen = model.getSelection().getLinkAsFile();
        if (NoteCollectionModel.goodFolder(fileToOpen)) {
          closeFile();
          openFile(fileToOpen);
        }
      }
    }
  }
  
  /**
   Reload the current file from disk and recreate the UI. 
  */
  private void reloadFile() {
    if (model.isOpen()) {
      boolean modOK = false;
      if (opInProgress) {
        System.out.println("Notenik.reloadFile operation in progress = " 
            + String.valueOf(opInProgress));
      } else {
        modOK = modIfChanged();
      }
      if (modOK) {
        FileSpec fileSpec = model.getFileSpec();
        savePrefs();
        publishWindow.closeSource();
        model.close();
        noteDisplayed = false;
        openFile(fileSpec, false);
      }
    }
  }
  
  private void reloadTaggedOnly() {
    
    if (model.isOpen()) {
      boolean modOK = false;
      if (opInProgress) {
        System.out.println("Notenik.reloadTaggedOnly operation in progress = " 
            + String.valueOf(opInProgress));
      } else {
        modOK = modIfChanged();
      }
      if (modOK) {
        FileSpec fileSpec = model.getFileSpec();
        savePrefs();
        publishWindow.closeSource();
        model.close();
        noteDisplayed = false;
        openFile(fileSpec, true);
      }
    }
  }
  
  /**
   Open the specified collection and allow the user to view and edit it. 
   This method assumes that the last collection, if any, has already been
   closed. 
  
   @param fileToOpen The folder to be opened. 
  */
  private void openFile(File fileToOpen) {
    FileSpec fileSpec = model.getMaster().getFileSpec(fileToOpen);
    if (fileSpec == null) {
      fileSpec = new FileSpec(fileToOpen);
    }
    openFile(fileSpec, false);
  }

  /**
   Open the specified collection and allow the user to view and edit it. 
   This method assumes that the last collection, if any, has already been
   closed. 
  
   @param fileToOpen The file spec identifying the collection to be opened. 
  */
  private void openFile (FileSpec fileToOpen, boolean taggedOnly) {
    
    model.openStart(fileToOpen, taggedOnly);
    model.openFinish();
    newCollection();
  }
  
  /**
   Rewire the UI to hook up to the new collection just opened/created.
  */
  private void newCollection() {
    
    reports.setDataFolder(model.getFolder());
    publishWindow.openSource(model.getFolder());
    displayedID = -1;
    if (model.editingMasterCollection()) {
      launchButton.setText("Open");
    } else {
      launchButton.setText("Launch");
    }   
    
    buildCollectionTabs();
    buildNoteTabs();
    this.statusBar.setFileName(model.getFileName());
    setPreferredCollectionView();
    
    Note noteToDisplay = null;
    if (model.getFileSpec().hasLastTitle()) {
      noteToDisplay = model.getFromTitle(model.getFileSpec().getLastTitle());
    }
    if (noteToDisplay == null) {
      noteToDisplay = model.getSorted(0);
    }
    selectPositionAndDisplay(noteToDisplay);
  }
  
  /**
   Purge closed or canceled items. 
  */
  private void purge() {
    
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.purge operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      noFindInProgress();
      int purged = 0;

      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Purge Options");
      alert.setHeaderText(null);
      alert.setContentText("Purge Closed Notes?");

      ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
      ButtonType discard = new ButtonType("Discard Purged");
      ButtonType copy = new ButtonType("Copy Purged");

      alert.getButtonTypes().setAll(cancel, discard, copy);

      Optional<ButtonType> result = alert.showAndWait();
      ButtonType option = result.get();

      File purgeTarget = null;
      String archiveFolderStr = model.getFileSpec().getArchiveFolder();
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
          if (NoteCollectionModel.goodFolder(purgeTarget)) {
            purgeIO = new NoteIO(purgeTarget, NoteParms.DEFINED_TYPE, model.getRecDef());
          } else {
            purgeTarget = null;
            option = cancel;
          }
        } // end if purge target folder not null
      } // end if option 1 was chosen

      if (option == copy || option == discard) {
        Note workNote;
        int workIndex = model.firstNote();
        while (workIndex < model.size()) {
          workNote = model.get (workIndex);
          boolean deleted = false;
          if (workNote.getStatus().isDone()) {
            boolean okToDelete = true;  
            // String fileToDelete = workNote.getDiskLocation();         
            if (option == copy) {
              try {
                purgeIO.save(purgeTarget, workNote, false);
              } catch (IOException e) {
                okToDelete = false;
                Logger.getShared().recordEvent(LogEvent.MEDIUM, 
                    "I/O Error while attemptint to save "
                      + workNote.getTitle() + " to Archive folder", 
                    false);
              }
            } // end of attempt to copy
            if (okToDelete) {
              deleted = model.remove (workNote);
              if (! deleted) {
                Logger.getShared().recordEvent(LogEvent.MEDIUM, 
                    "Unable to remove " 
                    + workNote.getTitle() + " from note list", false);
              }
              /*if (deleted) {
                deleted = new File(fileToDelete).delete();
              }
              if (! deleted) {
                trouble.report(
                    "Unable to delete note at " + fileToDelete, 
                    "Delete Failure");
              } */
              if (folderSyncPrefs.getSync()) {
                File syncFile = model.getSyncFile(workNote.getTitle());
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
        model.getFileSpec().setArchiveFolder(purgeTarget);
      }

      /* if (purged > 0) {
        openFile (noteFile, "", true);   
        position.setNavigatorToList (collectionTabs.getSelectionModel().getSelectedIndex() == 0);
        position = noteList.first (position);
        positionAndDisplay();
      } */

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

    if (model.isOpen() && model.hasSelection()) {
      boolean modOK = false;
      if (opInProgress) {
        System.out.println("Notenik.closeNote operation in progress = " 
            + String.valueOf(opInProgress));
      } else {
        modOK = modIfChanged();
      }
      if (modOK) {
        Note note = model.getSelection();
        boolean closeMods = false;
        if (note.hasRecurs() && note.hasDate()) {
          // Increment Date and leave status alone
          StringDate date = note.getDate();
          String newDate = note.getRecurs().recur(date);
          editPane.setDate(newDate);
          closeMods = true;
        }
        else
        if (editPane.statusIsIncluded()) {
          // Change Status to Closed
          String closedStr = model.getNoteParms().getItemStatusConfig().getClosedString();
          editPane.setStatus(closedStr);
          note.setStatus(closedStr);
          if (editPane.dateIsIncluded()) {
            editPane.setDate(StringDate.getTodayCommon());
          }
          closeMods = true;
          // newNote.setStatus(ItemStatusConfig.getShared().getClosedString());
        }
        if (closeMods) {
          if (opInProgress) {
            System.out.println("Notenik.closeNote with closeMods operation in progress = " 
              + String.valueOf(opInProgress));
          } else {
            modIfChanged();
            positionAndDisplaySelection();
          }
        }
      } // end if any modifications were made without problems
    } // End if we have a good note selection to start with
  }
  
  /**
   Bump the date up by one day. 
  */
  private void incrementDate() {
    
    if (model.isOpen() 
        && model.hasSelection()
        && model.getSelection().hasDate()) {
      boolean modOK = false;
      if (opInProgress) {
        System.out.println("Notenik.incrementDate with operation in progress = " 
            + String.valueOf(opInProgress));
      } else {
        modOK = modIfChanged();
      }
      if (modOK) {
        Note note = model.getSelection();
        StringDate date = note.getDate();
        String newDate = date.increment();
        editPane.setDate(newDate);
        if (opInProgress) {
          System.out.println("Notenik.incrementDate with mods but operation in progress = " 
            + String.valueOf(opInProgress));
        } else {
          modIfChanged();
          positionAndDisplaySelection();
        }
      } // end if any modifications were made without problems
    } else {
      PSOptionPane.showMessageDialog(primaryStage,
          "First select a Note with a Date before Incrementing a Date",
          "Selection Error",
          javax.swing.JOptionPane.WARNING_MESSAGE);
    }
  }
  
  /**
   Let's bump up the seq field for this Note, and all following
   notes until we stop creating duplicate seq fields. 
  */
  private void incrementSeq() {

    if (model.getSortParm().getParm() != NoteSortParm.SORT_BY_SEQ_AND_TITLE) {
      PSOptionPane.showMessageDialog(primaryStage,
          "First Sort by Seq + Title before Incrementing a Seq Value",
          "Sort Error",
          javax.swing.JOptionPane.WARNING_MESSAGE);
    } 
    else
    if (! model.hasSelection()) {
      PSOptionPane.showMessageDialog(primaryStage,
          "First select a Note before Incrementing a Seq Value",
          "Selection Error",
          javax.swing.JOptionPane.WARNING_MESSAGE);
    } else {
      boolean modOK = false;
      if (opInProgress) {
        System.out.println("incrementSeq with modInProgress");
      } else {
        modOK = modIfChanged();
      }
      if (modOK) {
        String startingTitle = model.getSelection().getTitle();
        // oldSeq = model.getSelection().getSeq();
        String newSeq = model.incrementSeq();
        editPane.setSeq(model.getSelection().getSeq());
        Note noteToSelect = model.getFromTitle(startingTitle);
        selectPositionAndDisplay(noteToSelect);
      }
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
    } else {
      collectionTabs.getSelectionModel().select(TAGS_TAB_INDEX);
    }
  }

  private void importFile () {

    dirChooser.setTitle ("Import Notes");
    if (FileUtils.isGoodInputDirectory(currentDirectory)) {
      fileChooser.setInitialDirectory (currentDirectory);
    }
    File selectedFile = dirChooser.showDialog(primaryStage);
    if (selectedFile != null) {
      preImport();
      File importFile = selectedFile;
      currentDirectory = importFile;
      NoteIO importer = new NoteIO (
          importFile, 
          NoteParms.DEFINED_TYPE, 
          model.getRecDef());
      try {
        importer.load(model, true);
      } catch (IOException e) {
        ioException(e);
      }
      postImport();
      // setUnsavedChanges(true);
    }
    // noteList.fireTableDataChanged();
    // firstNote();
  }
  
  private void importXMLFile () {

    fileChooser.setTitle ("Import Notes from XML");
    if (FileUtils.isGoodInputDirectory(currentDirectory)) {
      fileChooser.setInitialDirectory (currentDirectory);
    }
    File selectedFile = fileChooser.showOpenDialog(primaryStage);
    if (selectedFile != null) {
      preImport();
      File importFile = selectedFile;
      NoteImportXML importer = new NoteImportXML(this);
      importer.parse(importFile, model);
      postImport();
    }
    // noteList.fireTableDataChanged();
    // firstNote();
  }
  
  private void importTabDelimited() {
    fileChooser.setTitle("Import Notes from a Tab-Delimited File");
    if (FileUtils.isGoodInputDirectory(currentDirectory)) {
      fileChooser.setInitialDirectory(currentDirectory);
    }
    File selectedFile = fileChooser.showOpenDialog(primaryStage);
    if (selectedFile != null) {
      preImport();
      NoteImportTabDelim importer = new NoteImportTabDelim(this);
      importer.parse(selectedFile, model);
      postImport();
    }
    // noteList.fireTableDataChanged();
    // firstNote();
  }
  
  /**
   Let's get ready to import a bunch of notes. 
  */
  private void preImport() {
    closeFile();
    FileSpec fileSpec = model.getFileSpec();
    model.openStart(fileSpec, false);
    model.openFinish();
  }
  
  /**
   Let's rebuild the UI now that the import has completed. 
  */
  private void postImport() {
    newCollection();
  }
  
  /**
   Import info about Mac applications. 
  */
  private void importMacAppInfo() {
    
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.importMacAppInfo operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }

		if (modOK) {
      dirChooser.setTitle("Import Info about Mac Applications");
      File top = new File ("/");
      dirChooser.setInitialDirectory (top);
      File selectedFile = dirChooser.showDialog(primaryStage);
      if (selectedFile != null) {
        preImport();
        TextMergeInputMacApps macApps = new TextMergeInputMacApps();
        RecordDefinition recDef = model.getRecDef();
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
        postImport();
      } // end if user specified a valid directory
      
      // noteList.fireTableDataChanged();
      //firstNote();
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

    RecordDefinition recDef = model.getRecDef();
    Note appNote = new Note(model.getRecDef(), appName);
    StringBuilder body = new StringBuilder();
    Note existingNote = model.getFromTitle(appName);
    if (existingNote == null) {
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
      appNote = existingNote;
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
  
  /**
   Add an imported note to the collection. 
  
   @param importNote The note being imported. 
  
   @return True if import worked out ok.
  */
  private boolean addImportedNote(Note importNote) {
    boolean added = false;
    if ((! importNote.hasTitle()) 
        || importNote.getTitle().length() == 0
        || (! importNote.hasUniqueKey())) {
      // do nothing
    } else {
      importNote.setLastModDateToday();
      model.add (importNote);
      // noteList.fireTableDataChanged();
      added = true;
    }
    return added;
  }
  
  /*
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
  } */

  /**
   Save the current collection to a location specified by the user.
   */
  private void userSaveFileAs () {
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.userSaveFileAs operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      dirChooser.setTitle ("Save Notes to File");
      if (FileUtils.isGoodInputDirectory(currentDirectory)) {
        dirChooser.setInitialDirectory (currentDirectory);
      }
      File selectedFile = dirChooser.showDialog (primaryStage);
      if(NoteCollectionModel.goodFolder(selectedFile)) {
        File chosenFile = selectedFile;
        saveFileAs(chosenFile);
      }
    }
  }
  
  /**
   Allow the user to create a new collection.
  */
  public void userNewFile() {
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.userNewFile operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
    if (modOK) {
      dirChooser.setTitle ("Select Folder for New Note Collection");
      if (FileUtils.isGoodInputDirectory(currentDirectory)) {
        dirChooser.setInitialDirectory (currentDirectory);
      }
      File selectedFile = dirChooser.showDialog (primaryStage);
      if (selectedFile != null) {
        if (NoteCollectionModel.goodFolder(selectedFile)) {
          closeFile();
          FileSpec fileSpec = new FileSpec(selectedFile);
          model.openStart(fileSpec, false);
          model.getTemplate().showAndWait();
          savePrefs();
          publishWindow.closeSource();
          model.close();
          noteDisplayed = false;
          openFile(fileSpec, false);
        } else {
          trouble.report ("Trouble opening new file " + selectedFile.toString(),
              "New File Open Error");
        }
      } // end if user selected a file
    } // end if mods ok
  } // end method userNewFile
  
  /**
   Save the current collection of Notes to the specified file. 
  
   @param asFile The file to save the noteList to.  
  */
  private void saveFileAs(File toFolder) {
    if (model.isOpen()) {
      File fromFolder = model.getFolder();
      String Title = model.getSelectedTitle();
      boolean ok = FileUtils.copyFolder(fromFolder, toFolder);
      if (ok) {
        closeFile();
        openFile(toFolder);
      }
    }
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
    if (model.hasSelection()) {
      Note note = model.getSelection();
      if (note != null) {
        if (note.hasLink()) {
          String link = note.getLinkAsString();
          if (link.startsWith("file:")) {
            fileInfoWindow = new FileInfoWindow(this);
            fileInfoWindow.setFile(link);
            displayAuxiliaryWindow(fileInfoWindow);
            displayed = true;
          } // end if we have a file
        } // end if we have a link
      } // end if we have a newNote
    } // end if we have a position
    if (! displayed) {
      trouble.report ("No file to display",
          "No File Specified in Link Field");
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
    String linkText = editPane.getLink();
    if (linkText != null
        && linkText.length() > 0
        && linkTweaker != null
        && linkTweaker instanceof WindowToManage) {
      linkTweaker.setLink(linkText, "Link");
      WindowMenuManager.getShared().makeVisible(linkTweaker);
    }
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

  /**
    Validate the Links associated with the notes in the current list.
   */
  public void validateURLs () {

    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.validateURLs operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }

    if (modOK) {

      initialSelection = model.getSelection();
      // Prepare Auxiliary List to track invalid Notes
      webPageGroup = new ThreadGroup("URL Validation threads");
      urlValidators = new ArrayList<>();
      linksChecked = 0;
      progressWindow = new URLValidationProgressWindow(primaryStage,
          "Progress Validating Links", this);

      // Go through sorted items looking for Web Pages
      // Build a list of URL validator tasks
      Note workNote;
      String address;
      URLValidator validator;
      for (
          int workIndex = model.firstNote(); 
          workIndex >= 0 && workIndex < model.size(); 
          workIndex = model.nextNote(workIndex)) {
        workNote = model.get (workIndex);
        address = workNote.getURLasString();
        if (address.length() > 0) {
          validator = new URLValidator (workNote, workIndex);
          validator.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent evt) {
              linksChecked++;
              progressWindow.setLinksChecked(linksChecked);
            }
          });
          validator.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent evt) {
              linksChecked++;
              deadLinks++;
              progressWindow.setLinksChecked(linksChecked);
              progressWindow.setBadLinks(deadLinks);
              Worker badWorker = evt.getSource();
              if (badWorker instanceof URLValidator) {
                URLValidator badValidator = (URLValidator)badWorker;
                ItemWithURL badItem = badValidator.getItemWithURL();
                if (badItem instanceof Note) {
                  Note badNote = (Note)badItem;
                  if (! badNote.getTagsAsString().contains(INVALID_URL_TAG)) {
                    badNote.getTags().merge (INVALID_URL_TAG);
                    updateTagsOnNote(badNote);
                  } // End if we don't already have an invalid URL tag
                } // end if we have a good note instance
              } // end if we have a good url validator instance
            }
          });
          urlValidators.add (validator);
        }
      } // end of list

      linksToCheck = urlValidators.size();
      deadLinks = 0;
      progressWindow.setLinksToCheck(linksToCheck);
      windowMenuManager.add(progressWindow);
      windowMenuManager.makeVisible(progressWindow);

    }
  } // end validateURLs method
  
  /**
   Make an update to a note whose tags have been modified. 
  
   @param noteToUpdate The note to update. 
  */
  private void updateTagsOnNote(Note noteToUpdate) {
    if (model.getSelection().getTitle().equals(noteToUpdate.getTitle())) {
      model.modifySelection();
      editPane.setTags(model.getSelection().getTagsAsString());
    } else {
      String priorTitle = noteToUpdate.getTitle();
      String priorSortKey = noteToUpdate.getSortKey(model.getSortParm());
      String priorTags = noteToUpdate.getTagsAsString();
      model.modify(noteToUpdate, priorTitle, priorSortKey, priorTags);
    }
  }
  
  public void startLinkValidation() {

    progressWindow.validationStarting();
    // Now start threads to check Web pages
    URLValidator validator;
    for (int i = 0; i < urlValidators.size(); i++) {
      validator = urlValidators.get(i);
      Thread validatorThread = new Thread(webPageGroup, validator);
      validatorThread.setDaemon(true);
      validatorThread.start();
    } // end for each page being validated

  }
  
  public void stopLinkValidation() {
    if (! progressWindow.allDone()) {
      URLValidator validator;
      for (int i = 0; i < urlValidators.size(); i++) {
        validator = urlValidators.get(i);
        if (! validator.isDone()) {
          validator.cancel(true);
        }
      }
    }
    windowMenuManager.hide(progressWindow);
    selectPositionAndDisplay(initialSelection);
  }

  public File getCurrentDirectory () {
    return currentDirectory;
  }
  
  private void generalExport(int exportType) {
    boolean modOK = false;
    if (opInProgress) {
      System.out.println("Notenik.generalExport operation in progress = " 
          + String.valueOf(opInProgress));
    } else {
      modOK = modIfChanged();
    }
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
          fileChooser.setInitialDirectory(model.getFolder().getParentFile());
          fileChooser.setInitialFileName (model.getFolder().getName() + ".opml");
          selectedFile = fileChooser.showSaveDialog(primaryStage);
          break;
        
        case NoteExport.XML_EXPORT:
          fileChooser.setInitialDirectory(model.getFolder().getParentFile());
          fileChooser.setInitialFileName (model.getFolder().getName() + ".xml");
          selectedFile = fileChooser.showSaveDialog(primaryStage);
          break;
          
        case NoteExport.TABDELIM_EXPORT_MS_LINKS:
          fileChooser.setInitialDirectory(model.getFolder().getParentFile());
          fileChooser.setInitialFileName (model.getFolder().getName() + ".txt");
          selectedFile = fileChooser.showSaveDialog(primaryStage);
          break;

        case NoteExport.TABDELIM_EXPORT:
        default:
          fileChooser.setInitialDirectory(model.getFolder().getParentFile());
          fileChooser.setInitialFileName (model.getFolder().getName() + ".txt");
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
              model);
        } else {
          exported = 
            exporter.generalExport(
              selectedFile,
              model.getFolder(),
              model.getRecDef(),
              model,
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
  
  public void genHTMLtoClipboard() {
    boolean noNoteSelected = true;
    boolean ok = true;
    Note note = null;
    if (model.hasSelection()) {
      note = model.getSelection();
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
    if (model.hasSelection()) {
      note = model.getSelection();
      if (note != null) {
        noNoteSelected = false;
        String htmlFolderStr = model.getFileSpec().getHTMLFolder();
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
