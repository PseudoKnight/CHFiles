package me.macjuul.chfiles.Functions;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.SSHWrapper;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
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

// CH async_read()

@api
public class async_read_file extends AbstractFunction {
    @Override
    public Class<? extends CREThrowable>[] thrown() {
        return new Class[]{
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
    public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
        final String file = args[0].val();
        final CClosure callback;
        if (!(args[1] instanceof CClosure)) {
            throw new CRECastException("Expected parameter 2 of " + getName() + " to be a closure!", t);
        } else {
            callback = Static.getObject(args[1], t, CClosure.class);
        }
        if (!Static.InCmdLine(environment)) {
            if (!Security.CheckSecurity(file)) {
                throw new CRESecurityException("You do not have permission to access the file '" + file + "'", t);
            }
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
					File _file = Static.GetFileFromArgument(file, environment, t, null);
					returnString = FileUtil.read(_file);
				} catch (IOException ex) {
					exception = new CREIOException(ex.getMessage(), t, ex);
				}
			}
			final Construct cret;
			if (returnString == null) {
				cret = CNull.NULL;
			} else {
				cret = new CString(returnString, t);
			}
			final Construct cex;
			if (exception == null) {
				cex = CNull.NULL;
			} else {
				cex = ObjectGenerator.GetGenerator().exception(exception, environment, t);
			}
			StaticLayer.GetConvertor().runOnMainThreadLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), () -> 
					callback.execute(cret, cex));
		}).start();
        return CVoid.VOID;
    }

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
    public CHVersion since() {
        return CHVersion.V3_3_1;
    }

}