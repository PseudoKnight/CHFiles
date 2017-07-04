CHFiles is a CommandHelper extension that allows you to work with files and directory and interact with them from whithin your server.

Latest release: [v2.1.2](https://github.com/itstake/CHFiles/releases/tag/v2.1.2)

# Functions:

* **copy_file(FromFile, ToFile)** - Copy a file (or directory) to another location, with a new name

* **create_file(PathToFile)** - Create a new file

* **delete_file(PathToFile)** - Delete an extension file or directory

* **list_files(PathToDirectory)** - Lists all files and directories in given directory

* **rename_file(PathToFile)** - Rename a file

* **write_file(PathToFile, content, [mode])** - Write text to a file. mode is optional, can be OVERWRITE or APPEND.

* **async_write_file(PathToFile, content, [mode], [callback])** - Write text to a file asynchronously. mode is optional, can be OVERWRITE or APPEND.

* **async_read_file(PathToFile, callback)** - Asynchronously reads in a file. check [here](http://wiki.sk89q.com/wiki/CommandHelper/Staged/API/async_read)

* **file_exists(Path)** - Check if a file exists, returns true or false

* **create_dir(PathToDir)** - Create a new directory

* **is_dir(PathToFile)** - if a file is directory, return true.

* **is_file(PathToFile)** - if a file is not file, return false.

* **get_absolute_path([Path])** - Returns the absolute path