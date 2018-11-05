package me.macjuul.chfiles.Functions;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;

import java.io.File;
import java.io.IOException;

/**
 * Created by Junhyeong Lim on 2017-07-05.
 */
@api
public class async_write_file extends AbstractFunction {
	@Override
	public Construct exec(final Target t, final Environment env, final Construct... args) throws ConfigRuntimeException {
		final File loc = Static.GetFileFromArgument(args[0].val(), env, t, null);
		if (!Security.CheckSecurity(loc)) {
			throw new CRESecurityException("You do not have permission to access the file '" + loc.getAbsolutePath() + "'", t);
		}

		new Thread(() -> {
			try {
				if (!loc.exists()) {
					loc.createNewFile();
				}
				if (args.length >= 3 && args[2].val().toUpperCase().equals("OVERWRITE")) {
					FileUtil.write(args[1].val(), loc, 0);
				} else {
					FileUtil.write(args[1].val(), loc, 1);
				}

				if (args.length >= 4) {
					final CClosure closure = Static.getObject(args[3], t, CClosure.class);
					StaticLayer.GetConvertor().runOnMainThreadLater(env.getEnv(GlobalEnv.class).GetDaemonManager(), closure::execute);
				}
			} catch (IOException e) {
				throw new CREIOException("File could not be written.", t);
			}
		}).start();

		return CVoid.VOID;
	}

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
	public Class<? extends CREThrowable>[] thrown() {
		return new Class[]{
				CREIOException.class, 
				CRESecurityException.class, 
				CRECastException.class
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
	public Version since() {
		return CHVersion.V3_3_2;
	}
}
