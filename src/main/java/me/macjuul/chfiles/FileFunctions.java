package me.macjuul.chfiles;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.SSHWrapper;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.libs.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class FileFunctions {

	public static String docs() {
		return "A set of functions for interacting with files and directories.";
	}

	public static abstract class FileFunction extends AbstractFunction {
		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CRESecurityException.class};
		}
	}

	@api
	public static class async_read_file extends FileFunction {

		@Override
		public String getName() {
			return "async_read_file";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {file, callback} Asynchronously reads in a file. ---- "
					+ " This may be a remote file accessed with an SCP style path. (See the [[CommandHelper/SCP|wiki article]]"
					+ " about SCP credentials for more information.) If the file is not found, or otherwise can't be read in, an IOException is thrown."
					+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
					+ " (This is not applicable for remote files)"
					+ " The line endings for the string returned will always be \\n, even if they originally were \\r\\n."
					+ " This method will immediately return, and asynchronously read in the file, and finally send the contents"
					+ " to the callback once the task completes. The callback should have the following signature: closure(@contents, @exception){ &lt;code&gt; }."
					+ " If @contents is null, that indicates that an exception occurred, and @exception will not be null, but instead have an"
					+ " exception array. Otherwise, @contents will contain the file's contents, and @exception will be null. This method is useful"
					+ " to use in two cases, either you need a remote file via SCP, or a local file is big enough that you notice a delay when"
					+ " simply using the read() function.";
		}
		
		@Override
		public Mixed exec(final Target t, final Environment env, Mixed... args) throws ConfigRuntimeException {
			final String file = args[0].val();
			final CClosure callback;
			if (!(args[1] instanceof CClosure)) {
				throw new CRECastException("Expected parameter 2 of " + getName() + " to be a closure!", t);
			} else {
				callback = ArgumentValidation.getObject(args[1], t, CClosure.class);
			}
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(file)) {
					throw new CRESecurityException("You do not have permission to access the file '" + file + "'", t);
				}
			} catch (Exception e) {
				throw new CREIOException(e.getMessage(), t);
			}
			new Thread(() -> {
				String returnString = null;
				ConfigRuntimeException exception = null;
				if (file.contains("@")) {
					try {
						//It's an SCP transfer
						returnString = SSHWrapper.SCPReadString(file);
					} catch (IOException ex) {
						exception = new CREIOException(ex.getMessage(), t, ex);
					}
				} else {
					try {
						//It's a local file read
						File _file = Static.GetFileFromArgument(file, env, t, null);
						returnString = FileUtil.read(_file);
					} catch (IOException ex) {
						exception = new CREIOException(ex.getMessage(), t, ex);
					}
				}
				final Mixed cret;
				if (returnString == null) {
					cret = CNull.NULL;
				} else {
					cret = new CString(returnString, t);
				}
				final Mixed cex;
				if (exception == null) {
					cex = CNull.NULL;
				} else {
					cex = ObjectGenerator.GetGenerator().exception(exception, env, t);
				}
				StaticLayer.GetConvertor().runOnMainThreadLater(null, () ->
						callback.executeCallable(cret, cex));
			}).start();
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CRESecurityException.class, CRECastException.class};
		}
	}

	@api
	public static class async_write_file extends FileFunction {

		@Override
		public String getName() {
			return "async_write_file";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public String docs() {
			return "void {file, string, [mode], [callback]} Writes text to a file asynchronously."
					+ " The mode parameter can be OVERWRITE or APPEND."
					+ " The optional callback must be a closure. It will be executed upon write completion.";
		}

		@Override
		public Mixed exec(final Target t, final Environment env, final Mixed... args) throws ConfigRuntimeException {
			final File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc + "'", t);
				}
			} catch (Exception e) {
				throw new CREIOException(e.getMessage(), t);
			}
			new Thread(() -> {
				try {
					if (!loc.exists()) {
						loc.createNewFile();
					}
					if (args.length >= 3 && args[2].val().equalsIgnoreCase("OVERWRITE")) {
						FileUtil.write(args[1].val(), loc, 0);
					} else {
						FileUtil.write(args[1].val(), loc, 1);
					}

					if (args.length >= 4) {
						final CClosure closure = ArgumentValidation.getObject(args[3], t, CClosure.class);
						StaticLayer.GetConvertor().runOnMainThreadLater(null, closure::executeCallable);
					}
				} catch (IOException e) {
					throw new CREIOException("File could not be written.", t);
				}
			}).start();

			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CRESecurityException.class, CRECastException.class};
		}
	}

	@api
	public static class copy_file extends FileFunction {

		@Override
		public String getName() {
			return "copy_file";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {file, dir | dir, dir} Copies a file or directory to another directory.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File fromLoc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			File toLoc = Static.GetFileFromArgument(args[1].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && (!Security.CheckSecurity(fromLoc) || !Security.CheckSecurity(toLoc))) {
					throw new CRESecurityException("You do not have access to some of the files", t);
				}
				if (fromLoc.isDirectory()) {
					FileUtils.copyDirectory(fromLoc, toLoc);
				} else if (fromLoc.isFile()) {
					FileUtils.copyFile(fromLoc, toLoc);
				}
				return CVoid.VOID;
			} catch (IOException e) {
				throw new CREIOException(e.getMessage(), t);
			}
		}

	}

	@api
	public static class create_dir extends FileFunction {

		@Override
		public String getName() {
			return "create_dir";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {dir} Create a new directory.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
				if (loc.exists()) {
					throw new CREIOException(loc.getAbsolutePath() + "Already Exists", t);
				}
				loc.mkdir();
				return CVoid.VOID;
			} catch (Exception e) {
				throw new CREIOException(e.getMessage(), t);
			}
		}

	}

	@api
	public static class create_file extends FileFunction {

		@Override
		public String getName() {
			return "create_file";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {file} Creates a new file.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
				if (loc.exists()) {
					throw new CREIOException(loc.getAbsolutePath() + " already exists", t);
				}
				loc.createNewFile();
				return CVoid.VOID;
			} catch (IOException e) {
				throw new CREIOException(e.getMessage(), t);
			}
		}
		
	}

	@api
	public static class delete_file extends FileFunction {

		@Override
		public String getName() {
			return "delete_file";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {file} Deletes a file or directory.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
				if (!loc.exists()) {
					throw new CREIOException(loc.getAbsolutePath() + "Doesn't exists", t);
				}
				if (loc.isDirectory()) {
					FileUtils.deleteDirectory(loc);
				} else if (loc.isFile()) {
					FileUtils.forceDelete(loc);
				}
				return CVoid.VOID;
			} catch (IOException e) {
				throw new CREIOException(e.getMessage(), t);
			}
		}

	}

	@api
	public static class file_exists extends FileFunction {

		@Override
		public String getName() {
			return "file_exists";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {path} Check if a file exists.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
			} catch (Exception e) {
				throw new CREIOException(e.getMessage(), t);
			}
			return CBoolean.get(loc.exists());
		}

	}

	@api
	public static class get_absolute_path extends FileFunction {

		@Override
		public String getName() {
			return "get_absolute_path";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[file]} Gets the absolute path of a file, or this script file if none is specified.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String path = args.length == 0 ?
					t.file().getParentFile().getAbsolutePath() :
					Static.GetFileFromArgument(args[0].val(), env, t, null).getAbsolutePath();

			return new CString(path, t);
		}

	}

	@api
	public static class is_dir extends FileFunction {

		@Override
		public String getName() {
			return "is_dir";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {path} Checks if a path is a directory.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
			} catch (Exception e) {
				throw new CREIOException(e.getMessage(), t);
			}
			return CBoolean.get(loc.isDirectory());
		}

	}

	@api
	public static class is_file extends FileFunction {

		@Override
		public String getName() {
			return "is_file";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {path} Checks if a path is a file.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
			} catch (Exception e) {
				throw new CREIOException(e.getMessage(), t);
			}
			return CBoolean.get(loc.isFile());
		}

	}

	@api
	public static class list_files extends FileFunction {

		@Override
		public String getName() {
			return "list_files";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {dir} Lists all files and directories in given directory";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
			} catch (Exception e) {
				throw new CREIOException(e.getMessage(), t);
			}
			CArray ret = new CArray(t);
			if (loc.exists() && loc.isDirectory()) {
				String[] list = loc.list();
				for (String file : list) {
					ret.push(new CString(file, t), t);
				}
			} else {
				throw new CREIOException("This path is not a directory.", t);
			}
			return ret;
		}

	}

	@api
	public static class rename_file extends FileFunction {

		@Override
		public String getName() {
			return "rename_file";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {file, name} Renames a file.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
			} catch (Exception e) {
				throw new CREIOException(e.getMessage(), t);
			}
			if (!loc.exists()) {
				throw new CREIOException(loc.getAbsolutePath() + " doesn't exist", t);
			}
			if (loc.isDirectory()) {
				loc.renameTo(new File(loc.getParent() + File.separator + args[1].val() + File.separator));
			} else if (loc.isFile()) {
				loc.renameTo(new File(loc.getParent() + File.separator + args[1].val()));
			}
			return CVoid.VOID;
		}

	}

	@api
	public static class ungz_file extends FileFunction {

		@Override
		public String getName() {
			return "ungz_file";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {gzfile, tofile} Unzip a file and write it to another file.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {

			File gz = Static.GetFileFromArgument(args[0].val(), env, t, null);
			File tofile = Static.GetFileFromArgument(args[1].val(), env, t, null);

			try {
				if (!Static.InCmdLine(env, true) && (!Security.CheckSecurity(gz) || !Security.CheckSecurity(tofile))) {
					throw new CRESecurityException("You do not have access to some of the files", t);
				}

				FileInputStream fis = new FileInputStream(gz);
				GZIPInputStream gzis = new GZIPInputStream(fis);

				byte[] buffer = new byte[1024];
				int length;

				FileOutputStream fos = new FileOutputStream(tofile);

				while ((length = gzis.read(buffer)) > 0) {
					fos.write(buffer, 0, length);
				}

				fos.close();
				gzis.close();
				fis.close();

			} catch (IOException e) {
				throw new CREIOException(e.getMessage(), t);
			}

			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class};
		}
	}

	@api
	public static class write_file extends FileFunction {

		@Override
		public String getName() {
			return "write_file";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		@Override
		public String docs() {
			return "void {file, content, [mode]} Writes text to a file."
					+ " Accepts a byte array or a string as the content."
					+ " The mode parameter can be OVERWRITE or APPEND.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
				if (!loc.exists()) {
					loc.createNewFile();
				}
				byte[] content;
				if(args[1].isInstanceOf(CByteArray.TYPE)) {
					content = ArgumentValidation.getByteArray(args[1], t).asByteArrayCopy();
				} else {
					content = args[1].val().getBytes(StandardCharsets.UTF_8);
				}
				if (args.length >= 3 && args[2].val().equalsIgnoreCase("OVERWRITE")) {
					FileUtil.write(content, loc, 0, false);
				} else {
					FileUtil.write(content, loc, 1, false);
				}

				return CVoid.VOID;
			} catch (IOException e) {
				throw new CREIOException(e.getMessage(), t);
			}
		}

	}

	@api
	public static class file_last_modified extends FileFunction {

		@Override
		public String getName() {
			return "file_last_modified";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {file} Returns the time a file was last modified as a unix time stamp."
					+ " Will return 0 if the file does not exist or an I/O error occurs.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if (!Static.InCmdLine(env, true) && !Security.CheckSecurity(loc)) {
					throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
				}
				return new CInt(loc.lastModified(), t);
			} catch (IOException e) {
				throw new CREIOException(e.getMessage(), t);
			}
		}
	}

}
