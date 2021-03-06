Title:  Publish

Seq:    11.4

Tags:   Cmd.P, File.Publish

Body:   
 
The Publish option allows you to easily publish your Notes in a variety of useful formats, using the power of [PSTextMerge](http://www.powersurgepub.com/products/pstextmerge/user-guide.html). For example, you can easily publish your notes as a series of web pages.

To begin the publication process, select the Publish command from the File menu.

You will then see a window with the following fields available to you.

**Publish to**: You may use the Browse button above and to the right to select a folder on your computer to which you wish to publish your Notes. You may also enter or modify the path directly in the text box. When modifying this field, you will be prompted to specify whether you wish to update the existing publication location, or add a new one. By specifying that you wish to add a new one, you may create multiple publications, and then later select the publication of interest by using the drop-down arrow to the right of this field.

**Equivalent URL**: If the folder to which you are publishing will be addressable from the World Wide Web, then enter its Web address here.

**Templates**: This is the address of a folder containing one or more publishing templates. This will default to the location of the templates provided along with the application executable. You may use the Browse button above and to the right to pick a different location, if you have your own templates you wish to use for publishing.

**Select**: Use the drop-down list to select the template you wish to use.

* **Favorites Plus**: This template will produce the following files and formats.

	1. index.html -- This file is an index file with links to the other files. You can browse this locally by selecting **Browse local index** from the **File** menu.
	2. favorites.html -- This file tries to arrange all of the Notes you have tagged as "Favorites" into a four-column format that will fit on a single page.
	3. bookmark.html -- This file formats your URLs in the time-honored Netscape bookmarks format, suitable for import into almost any Web browser or URL manager.
	4. outline.html -- This is a dynamic html file that organizes your URLs within your tags, allowing you to reveal/disclose selected tags.

**Apply**: Press this button to apply the selected template. This will copy the contents of the template folder to the location specified above as the Publish to location.

**Publish Script**: Specify the location of the script to be used. The [PSTextMerge](http://www.powersurgepub.com/products/pstextmerge/user-guide.html) templating system is the primary scripting language used for publishing. A PSTextMerge script will usually end with a '.tcz' file extension.

**Publish when**: You may specify publication 'On Close' (whenever you Quit the application or close a Collection), or 'On Demand'.

**Publish Now**: Press this button to publish to the currently displayed location. Note that, if you've specified 'On Demand', then this is the only time that publication will occur.

**View**: Select the local file location or the equivalent URL location.

**View Now**: Press this button to view the resulting Web site in your Web browser.

