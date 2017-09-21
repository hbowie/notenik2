/*
 * Copyright 1999 - 2016 Herb Bowie
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

  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.notenik.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.tabdelim.*;
  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.util.*;

/**
 Imports notes from a tab-delimited file. 

 @author Herb Bowie
 */
public class NoteImportTabDelim {
  
  private     File                importFile;
  private     NoteCollectionModel model;
  
  private     Notenik             notenik;
  
  private     boolean             notenikStarted = false;
  private     boolean             noteStarted = false;
  
  private     Note                workNote;
  
  private     String              fieldName = "";

  private     TabDelimFile        tabDelimFile;
  
  private     DataDictionary      dict;
  private     RecordDefinition    recDef;
  
  private     boolean             ok = true;
  private     int                 imported = 0;
  
  /** Log used to record events. */
  private     Logger              log = Logger.getShared();
  
  /** 
   Creates a new instance of NoteImportTabDelim 
   */
  public NoteImportTabDelim(Notenik mainFrame) {
    this.notenik = mainFrame;
  }
  
  /**
     Sets a logger to be used for logging operations.
    
     @param log Logger instance.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
  public int parse (
      File importFile,
      NoteCollectionModel model) {
    
    this.importFile = importFile;
    this.model = model;
    ok = true;
    imported = 0;
    DataSource importer;
    importer = new TabDelimFile(importFile);
    importer.setLog (Logger.getShared());
    // setActionMsg ("Importing " + importName + " ... ");
    int before = model.size();
    if (ok) {
      if (! this.importFile.canRead()) {
        ok = false;
        log.recordEvent (LogEvent.MEDIUM, 
            "Tab-Delimited File " + importFile.toString() + " cannot be read",
            false);       
      }
    }
    if (ok) {
      if (this.importFile.isDirectory()) {
        ok = false;
        log.recordEvent (LogEvent.MEDIUM, 
            "Directory found instead of Tab-Delimited file at " + importFile.toString(),
            false); 
      } // end if passed String identified a directory
    } // end if everything still OK
    if (ok) {
      try {
        importer.openForInput();
        while (! importer.isAtEnd()) {
          DataRecord dataRec = importer.nextRecordIn();
          if (dataRec != null) {
            Note note = new Note(model.getRecDef());
            for (int i = 0; i < dataRec.getNumberOfFields(); i++) {
              DataField field = dataRec.getField(i);
              note.setField(field.getCommonFormOfName(), field.getData());
            }
            if (note.hasTitle()) {
              model.add(note);
            }
          }
        }
        importer.close();
      } catch (IOException e) {
        ok = false;
        Trouble.getShared().report 
            ("Trouble Reading File "
            + importFile.toString(),
            "File I/O Problem");
      }
    }
    if (! ok) {
      imported = -1;
    }
    return imported;
  }  
  
}
