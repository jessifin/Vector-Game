package org.jessifin.main;

import java.awt.Canvas;
import java.awt.Container;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.lwjgl.opengl.Display;

/**
 * 
 * Useful for mac-specific tasks.
 *
 */
public class MacUtil {

	private static Class<?> macApplicationClass;
	private static Object macApplicationInstance;
	private static Method setBadgeMethod, requestUserMethod, setIconMethod, requestForegroundMethod, toggleFullscreenMethod;

	/**
	 * Only works on macs.
	 * Lotsa Exceptions.
	 */
	public static void setupMacApp() {
		if(macApplicationClass == null || macApplicationInstance == null) {
			try {
				macApplicationClass = Class.forName("com.apple.eawt.Application");
				Method getApplication = macApplicationClass.getMethod("getApplication");
				macApplicationInstance = getApplication.invoke(null);
				setBadgeMethod = macApplicationClass.getMethod("setDockIconBadge", new Class[]{String.class});
				requestUserMethod = macApplicationClass.getMethod("requestUserAttention", new Class[]{Boolean.TYPE});
				setIconMethod = macApplicationClass.getMethod("setDockIconImage", new Class[]{Image.class});
				requestForegroundMethod = macApplicationClass.getMethod("requestForeground", new Class[]{Boolean.TYPE});
				toggleFullscreenMethod = macApplicationClass.getMethod("requestToggleFullScreen", new Class[]{Window.class});
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			} catch(InvocationTargetException e) {
				e.printStackTrace();
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
			} catch(NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void setBadgeMac(String message) {
		setupMacApp();
		try {
			setBadgeMethod.invoke(macApplicationInstance, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setIconMac(BufferedImage icon) {
		setupMacApp();
		try {
			setIconMethod.invoke(macApplicationInstance, icon);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void requestUserAttentionMac(boolean annoyUser) {
		setupMacApp();
		try {
			requestUserMethod.invoke(macApplicationInstance, annoyUser);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void requestForeground(boolean allWindows) {
		setupMacApp();
		try {
			requestForegroundMethod.invoke(macApplicationInstance, allWindows);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void toggleFullscreen() {
		setupMacApp();
		try {
			Canvas c = Display.getParent();
			Container cont = c.getParent();
			Window w = (Window)cont;
			toggleFullscreenMethod.invoke(macApplicationInstance, w);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
		
}
