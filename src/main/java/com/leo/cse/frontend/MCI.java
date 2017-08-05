package com.leo.cse.frontend;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

import com.leo.cse.backend.exe.MapInfo.PxeEntry;

public class MCI {

	public static class MCIException extends Exception {
		private static final long serialVersionUID = 2809309984939251682L;

		public MCIException(String message) {
			super(message);
		}
	}

	private MCI() {
	}

	private static Properties mci = new Properties();

	private static void readList(Properties p, String group, Object obj) {
		if (obj instanceof NativeArray) {
			NativeArray na = (NativeArray) obj;
			for (int d = 0; d < na.size(); d++) {
				if (na.get(d) == Scriptable.NOT_FOUND || na.get(d) == null)
					continue;
				p.put(group + "." + d, na.get(d));
			}
		} else if (obj instanceof String[]) {
			String[] list = (String[]) obj;
			for (int d = 0; d < list.length; d++) {
				if (list[d] == null)
					continue;
				p.put(group + "." + d, list[d]);
			}
		} else
			throw new RuntimeException("Unsupported list type: " + obj.getClass().getName());
	}

	private static Object invokeFunction(Context cx, Scriptable scope, String name, Object... args)
			throws NoSuchMethodException {
		Object fObj = scope.get(name, scope);
		if (!(fObj instanceof Function))
			throw new NoSuchMethodException(name);
		Function f = (Function) fObj;
		ContextFactory.getGlobal().enterContext(cx);
		Object result = f.call(cx, scope, scope, args);
		Context.exit();
		return result;
	}

	private static Context cx;
	private static Scriptable scope;

	private static Object invokeFunction(String name, Object... args) throws NoSuchMethodException {
		return invokeFunction(cx, scope, name, args);
	}

	private static void read0(InputStream is, File src) throws Exception {
		Context tcx = Context.enter();
		Scriptable tscope = tcx.initStandardObjects();
		try (InputStreamReader isr = new InputStreamReader(is);) {
			tcx.evaluateReader(tscope, isr, src.getName(), 0, null);
		} catch (IOException e) {
			System.err.println("MCI: Error while parsing script!");
			e.printStackTrace();
			JOptionPane.showMessageDialog(Main.window, "An exception occured while parsing the MCI file:\n" + e,
					"Error while parsing MCI file", JOptionPane.ERROR_MESSAGE);
			throw e;
		}
		Properties tmp = new Properties();
		try {
			// Metadata
			tmp.put("Meta.Name", invokeFunction(tcx, tscope, "getName"));
			tmp.put("Meta.Author", invokeFunction(tcx, tscope, "getAuthor"));
			// Game information
			tmp.put("Game.ExeName", invokeFunction(tcx, tscope, "getExeName"));
			tmp.put("Game.ArmsImageYStart", invokeFunction(tcx, tscope, "getArmsImageYStart"));
			tmp.put("Game.ArmsImageSize", invokeFunction(tcx, tscope, "getArmsImageSize"));
			tmp.put("Game.FPS", invokeFunction(tcx, tscope, "getFPS"));
			tmp.put("Game.GraphicsResolution", invokeFunction(tcx, tscope, "getGraphicsResolution"));
			// Special support
			Object oss = invokeFunction(tcx, tscope, "getSpecials");
			if (oss instanceof NativeArray) {
				NativeArray specials = (NativeArray) oss;
				if (specials.contains("MimHack"))
					tmp.put("Special.MimHack", true);
				if (specials.contains("VarHack"))
					tmp.put("Special.VarHack", true);
				if (specials.contains("PhysVarHack"))
					tmp.put("Special.PhysVarHack", true);
				if (specials.contains("BuyHack"))
					tmp.put("Special.BuyHack", true);
			}
			// Map names
			readList(tmp, "Map", invokeFunction(tcx, tscope, "getMapNames"));
			// Song names
			readList(tmp, "Song", invokeFunction(tcx, tscope, "getSongNames"));
			// Equip names
			readList(tmp, "Equip", invokeFunction(tcx, tscope, "getEquipNames"));
			// Weapon names
			readList(tmp, "Weapon", invokeFunction(tcx, tscope, "getWeaponNames"));
			// Item names
			readList(tmp, "Item", invokeFunction(tcx, tscope, "getItemNames"));
			// Warp menu names
			readList(tmp, "Warp", invokeFunction(tcx, tscope, "getWarpNames"));
			// Warp location names
			readList(tmp, "WarpLoc", invokeFunction(tcx, tscope, "getWarpLocNames"));
			// Flag descriptions
			tmp.put("Flag.SaveID", invokeFunction(tcx, tscope, "getSaveFlagID"));
			readList(tmp, "Flag", invokeFunction(tcx, tscope, "getFlagDescriptions"));
		} catch (NoSuchMethodException e) {
			JOptionPane.showMessageDialog(Main.window,
					"Could not find definition for \"" + e.getMessage() + "\" in MCI file!", "Missing definition",
					JOptionPane.ERROR_MESSAGE);
			throw e;
		} catch (Exception e) {
			System.err.println("MCI: Exception while assigning script results to properties object!");
			e.printStackTrace();
			JOptionPane.showMessageDialog(Main.window, "Something went wrong with the MCI file:\n" + e,
					"Something went wrong", JOptionPane.ERROR_MESSAGE);
			throw e;
		} finally {
			Context.exit();
			cx = tcx;
			scope = tscope;
			mci.clear();
			mci.putAll(tmp);
		}
	}

	public static void readDefault() throws Exception {
		read0(MCI.class.getResourceAsStream("default.mci"), new File("default.mci"));
		validate();
	}

	public static void read(File file) throws Exception {
		read0(new FileInputStream(file), file);
		validate();
	}

	private static void validate() throws MCIException {
		int fps = getInteger("Game.FPS", 50);
		if (fps == 0)
			throw new MCIException("Game.FPS cannot be equal to 0!");
	}

	public static boolean contains(String key) {
		return mci.containsKey(key);
	}

	public static String get(String key) {
		return mci.getProperty(key, key);
	}

	public static String get(String type, String value) {
		final String key = type + "." + value;
		return mci.getProperty(key, mci.getProperty(type + ".None", key));
	}

	public static String get(String type, int id) {
		return get(type, Integer.toString(id));
	}

	public static String getNullable(String key) {
		return mci.getProperty(key);
	}

	public static String getNullable(String type, String value) {
		return mci.getProperty(type + "." + value);
	}

	public static String getNullable(String type, int id) {
		return getNullable(type, Integer.toString(id));
	}

	public static int getInteger(String key, int def) {
		String val = getNullable(key);
		if (val == null)
			return def;
		Integer ret;
		try {
			ret = Integer.parseUnsignedInt(val);
		} catch (NumberFormatException ignore) {
			return def;
		}
		return ret;
	}

	public static int getNumber(String type) {
		int ret = 0;
		for (Object obj : mci.keySet()) {
			if (!(obj instanceof String))
				continue;
			String key = (String) obj;
			if (key.startsWith(type + "."))
				ret++;
		}
		return ret;
	}

	public static Map<Integer, String> getAll(String type) {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		for (Map.Entry<Object, Object> entry : mci.entrySet())
			if (((String) entry.getKey()).startsWith(type + ".")) {
				String key = (String) entry.getKey();
				String id = key.substring(key.lastIndexOf('.') + 1);
				try {
					ret.put(Integer.parseInt(id), (String) entry.getValue());
				} catch (Exception e) {
					continue;
				}
			}
		return ret;
	}

	public static int getId(String type, String value) {
		Integer i = FrontUtils.getKey(getAll(type), value);
		if (i == null)
			return -1;
		return i;
	}

	public static boolean getSpecial(String value) {
		return Boolean.parseBoolean(mci.getProperty("Special." + value, "false"));
	}

	public static String getSpecials() {
		String ret = "None";
		if (getSpecial("VarHack")) {
			ret = "TSC+ <VAR Hack";
			if (getSpecial("PhysVarHack"))
				ret += " + <PHY Addon";
		} else if (getSpecial("MimHack"))
			ret = "<MIM Hack";
		else if (getSpecial("BuyHack"))
			ret = "<BUY Hack";
		int res = getInteger("Game.GraphicsResolution", 1);
		if (res != 1) {
			if (ret.equals("None"))
				ret = res + "x Resolution";
			else
				ret += ", " + res + "x Res";
		}
		return ret;
	}

	public static class EntityExtras {
		private Rectangle frameRect;
		private Point offset;

		public EntityExtras(Rectangle frameRect, Point offset) {
			this.frameRect = frameRect;
			this.offset = offset;
		}
		
		public Rectangle getFrameRect() {
			return frameRect;
		}
		
		public Point getOffset() {
			return offset;
		}
	}
	
	public static class WrappedPxeEntry {
		public short x;
		public short y;
		public short flagID;
		public short eventNum;
		public short type;
		public boolean[] flags;
		public WrappedPxeEntry(PxeEntry e) {
			x = e.getX();
			y = e.getY();
			flagID = e.getFlagID();
			eventNum = e.getEvent();
			type = e.getType();
			short fi = e.getFlags();
			fi |= e.getInfo().getFlags();
			flags = new boolean[16];
			for (int i = 0; i < flags.length; i++)
				flags[i] = (fi & (1 << i)) != 0;
		}
	}

	public static EntityExtras getEntityExtras(PxeEntry e) throws NoSuchMethodException {
		WrappedPxeEntry we = new WrappedPxeEntry(e);
		Object oe1 = Context.jsToJava(invokeFunction("getEntityFrame", we), Rectangle.class);
		if (!(oe1 instanceof Rectangle)) {
			System.err.println("oe1 is not Rectangle: " + oe1.getClass().getName());
			return null;
		}
		Rectangle frameRect = (Rectangle) oe1;
		Object oe2 = Context.jsToJava(invokeFunction("getEntityOffset", we), Point.class);
		if (!(oe2 instanceof Point)) {
			System.err.println("oe2 is not Point: " + oe1.getClass().getName());
			return null;
		}
		Point offset = (Point) oe2;
		return new EntityExtras(frameRect, offset);
	}

}