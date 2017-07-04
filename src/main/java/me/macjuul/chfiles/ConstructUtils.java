package me.macjuul.chfiles;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;

import java.io.File;

/**
 * Created by Junhyeong Lim on 2017-07-04.
 */
public class ConstructUtils {
    public static boolean getBoolean(Construct[] constructs, int index, boolean def) {
        if (constructs.length > index) {
            return Static.getBoolean(constructs[index]);
        }
        return def;
    }

    public static File getFile(Construct conzt, Target t) {
        String val = conzt.val();
        return isAbsolutePath(val) ?
                new File(val) : new File(t.file().getParentFile(), val);
    }

    private static boolean isAbsolutePath(String fileName) {
        // Linux
        if (fileName.startsWith("/")) {
            return true;
        }

        // Windows
        if (fileName.length() > 2) {
            char[] prefixArr = fileName.substring(0, 2).toLowerCase().toCharArray();
            if ('a' <= prefixArr[0] && prefixArr[0] <= 'z' && prefixArr[1] == ':') {
                return true;
            }
        }
        return false;
    }
}
