package me.macjuul.chfiles.Functions;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Created by User on 2017-07-23.
 */

@api
public class ungz_file extends AbstractFunction {

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends CREThrowable>[] thrown() {
		return new Class[]{
				CREIOException.class
		};
	}

	@Override
	public boolean isRestricted() {
		return true;
	}

	@Override
	public Boolean runAsync() {
		return null;
	}

	@Override
	public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {

		File gz = Static.GetFileFromArgument(args[0].val(), env, t, null);
		File tofile = Static.GetFileFromArgument(args[1].val(), env, t, null);

		try {
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

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Version since() {
		return new SimpleVersion(1, 0, 0);
	}

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
		return "void (gzfile, tofile) Unzip a file and write it to another file.";
	}
}
