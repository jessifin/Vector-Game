package org.jessifin.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class WebUtil {

	public static void readWebsite(String siteName) throws IOException, MalformedURLException {
		URL site = new URL(siteName);
		URLConnection connect = site.openConnection();
		Scanner scan = new Scanner(connect.getInputStream());
		while(scan.hasNext()) {
			System.out.println(scan.nextLine());
		}
	}
}