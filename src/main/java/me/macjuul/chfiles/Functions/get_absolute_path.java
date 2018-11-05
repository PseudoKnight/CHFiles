package me.macjuul.chfiles.Functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;

/**
 * Created by Junhyeong Lim on 2017-07-05.
 */
@api
public class get_absolute_path extends AbstractFunction {
	@Override
	public Class<? extends CREThrowable>[] thrown() {
		return new Class[0];
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
		String path = args.length == 0 ?
				t.file().getParentFile().getAbsolutePath() :
				Static.GetFileFromArgument(args[0].val(), env, t, null).getAbsolutePath();

		return new CString(path, t);
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_2;
	}

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
}
