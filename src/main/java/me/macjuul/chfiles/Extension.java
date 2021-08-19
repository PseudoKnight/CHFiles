package me.macjuul.chfiles;


import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;

import java.util.logging.Level;

@MSExtension("CHFiles")
public class Extension extends AbstractExtension {
	private final Version VERSION = new SimpleVersion(2, 2, 7);

	public Version getVersion() {
		return VERSION;
	}

	@Override
	public void onStartup() {
		if(!Implementation.GetServerType().equals(Implementation.Type.SHELL)) {
			Static.getLogger().log(Level.INFO, "CHFiles " + getVersion() + " loaded.");
		}
	}

	@Override
	public void onShutdown() {
		if(!Implementation.GetServerType().equals(Implementation.Type.SHELL)) {
			Static.getLogger().log(Level.INFO, "CHFiles " + getVersion() + " unloaded.");
		}
	}
}
