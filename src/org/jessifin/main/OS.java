package org.jessifin.main;

public enum OS {
	WINDOWS("windows"), MAC("macosx"), LINUX("linux"), SOLARIS("solaris"), FREE_BSD("freebsd"), NULL("");
	
	public final String nativePath;
	
	private OS(String nativePath) {
		this.nativePath = nativePath;
	}
	
	static OS getOS() {
		String osName = System.getProperty("os.name");
		if(osName.contains("Windows")) {
			return WINDOWS;
		} else if(osName.contains("Mac")) {
			return MAC;
		} else if(osName.contains("Linux")) {
			return LINUX;
		} else if(osName.contains("Solaris")) {
			return SOLARIS;
		} else if(osName.contains("FreeBSD")) {
			return FREE_BSD;
		} else {
			System.err.println("Your OS is not supported. Sorry.");
			Main.RUNNING = false;
			return NULL;
		}
	}
}