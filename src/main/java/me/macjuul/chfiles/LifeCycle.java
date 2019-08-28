package me.macjuul.chfiles;


import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;

@MSExtension("CHFiles")
public class LifeCycle extends AbstractExtension {
	public Version getVersion() {
		return new SimpleVersion(2, 2, 5);
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
