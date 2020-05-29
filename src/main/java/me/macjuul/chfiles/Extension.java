package me.macjuul.chfiles;


import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;

@MSExtension("CHFiles")
public class Extension extends AbstractExtension {
	private final Version VERSION = new SimpleVersion(2, 2, 6);

	public Version getVersion() {
		return VERSION;
	}

	@Override
	public void onStartup() {
		if(!Implementation.GetServerType().equals(Implementation.Type.SHELL)) {
			System.out.println("CHFiles " + getVersion() + " loaded.");
		}
	}

	@Override
	public void onShutdown() {
		if(!Implementation.GetServerType().equals(Implementation.Type.SHELL)) {
			System.out.println("CHFiles " + getVersion() + " unloaded.");
		}
	}
}
