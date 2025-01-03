CHFiles is a CommandHelper extension that allows you to work with files and directories.

## Compatibility and Downloads

CHFiles 2.3.2 for CommandHelper 3.3.5  
[CHFiles 2.2.7](https://github.com/PseudoKnight/CHFiles/releases/tag/v2.2.7) for CommandHelper 3.3.4

## Functions:

* void **copy_file(FromFile, ToFile)** - Copies a file or directory to another directory.

* void **create_file(PathToFile)** - Creates a new file.

* void **delete_file(PathToFile)** - Deletes a file or directory.

* array **list_files(PathToDirectory)** - Lists all files and directories in given directory.

* void **rename_file(File, Name)** - Renames a file.

* void **ungz_file(GZFile, ToFile)** - Unzip a file and write it to another file.

* void **write_file(PathToFile, content, [mode])** - Writes data to a file.  
  Accepts a byte array or a string as the content.  
  The mode parameter can be OVERWRITE or APPEND.

* void **async_write_file(PathToFile, content, [mode], [callback])** - Writes data to a file asynchronously.  
  Accepts a byte array or a string as the content.  
* The mode parameter can be OVERWRITE or APPEND. The optional callback must be a closure. It will be executed upon write completion.

* void **async_read_file(PathToFile, callback)** - Asynchronously reads in a file.
  Check [here](https://methodscript.com/docs/3.3.3/API/functions/async_read).

* boolean **file_exists(Path)** - Check if a file exists.

* void **create_dir(PathToDir)** - Create a new directory.

* boolean **is_dir(PathToFile)** - Checks if a path is a directory.

* boolean **is_file(PathToFile)** - Checks if a path is a file.

* string **get_absolute_path([Path])** - Gets the absolute path of a file, or this script file if none is specified.

* int **file_last_modified(PathToFile)** - Returns the time a file was last modified as a unix time stamp.  
  Will return 0 if the file does not exist or an I/O error occurs.
