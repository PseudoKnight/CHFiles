package me.macjuul.chfiles;


import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;

@MSExtension("CHFiles")
public class LifeCycle extends AbstractExtension {
	public Version getVersion() {
		return new SimpleVersion(2, 2, 3);
	}


	public void onStartup() {
		System.out.println("CHFiles " + getVersion() + " loaded.");
	}

	public void onShutdown() {
		System.out.println("CHFiles " + getVersion() + " unloaded.");

	}
}
