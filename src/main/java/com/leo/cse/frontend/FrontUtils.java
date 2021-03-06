package com.leo.cse.frontend;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.filechooser.FileFilter;

public class FrontUtils {

	public static String showSelectionDialog(Component parent, String title, String[] selections,
			String initialSelection, DefaultListCellRenderer listRender) {
		JList<String> list = new JList<>(selections);
		if (listRender != null) {
			list.setBackground(Main.COLOR_BG);
			list.setCellRenderer(listRender);
		}
		JScrollPane scrollpane = new JScrollPane();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(scrollpane);
		scrollpane.getViewport().add(list);
		list.setSelectedValue(initialSelection, true);
		String ret = null;
		final boolean[] dc = new boolean[1];
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					dc[0] = true;
					SwingUtilities.windowForComponent(list).dispose();
				}
			}
		});
		int msg = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (dc[0] || msg == JOptionPane.OK_OPTION)
			ret = list.getSelectedValue();
		return ret;
	}

	public static String showSelectionDialog(Component parent, String title, String[] selections,
			String initialSelection) {
		return showSelectionDialog(parent, title, selections, initialSelection, null);
	}

	public static String showSelectionDialog(Component parent, String title, Collection<String> selections,
			String initialSelection, DefaultListCellRenderer listRender) {
		return showSelectionDialog(parent, title, selections.toArray(new String[] {}), initialSelection, listRender);
	}

	public static String showSelectionDialog(Component parent, String title, Collection<String> selections,
			String initialSelection) {
		return showSelectionDialog(parent, title, selections.toArray(new String[] {}), initialSelection, null);
	}

	public static void downloadFile(String url, File dest) throws IOException {
		URL site = new URL(url);
		try (InputStream siteIn = site.openStream();
				ReadableByteChannel rbc = Channels.newChannel(siteIn);
				FileOutputStream out = new FileOutputStream(dest)) {
			out.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}
	}

	private static void drawString0(Graphics g, String str, int x, int y, boolean shadowOnly) {
		Color oc = g.getColor();
		Color sc = oc.darker().darker().darker().darker();
		g.setColor(sc);
		g.drawString(str, x + 1, y + 1);
		g.setColor(oc);
		if (!shadowOnly)
			g.drawString(str, x, y);
	}

	public static void drawString(Graphics g, String str, int x, int y, boolean shadowOnly) {
		final int lineSpace = g.getFontMetrics().getHeight();
		for (String line : str.split("\n")) {
			y += lineSpace;
			drawString0(g, line, x, y - 1, shadowOnly);
		}
	}

	public static void drawString(Graphics g, String str, int x, int y) {
		drawString(g, str, x, y, false);
	}

	public static void drawStringRight(Graphics g, String str, int x, int y, boolean shadowOnly) {
		final int lineSpace = g.getFontMetrics().getHeight();
		for (String line : str.split("\n")) {
			y += lineSpace;
			drawString0(g, line, x - g.getFontMetrics().stringWidth(line), y - 1, shadowOnly);
		}
	}

	public static void drawStringRight(Graphics g, String str, int x, int y) {
		drawStringRight(g, str, x, y, false);
	}

	public static void drawStringCentered(Graphics g, String str, int x, int y, boolean vert, boolean shadowOnly) {
		if (vert)
			y -= (g.getFontMetrics().getHeight() / 4) * 3;
		final int lineSpace = g.getFontMetrics().getHeight();
		if (str == null)
			return;
		for (String line : str.split("\n")) {
			y += lineSpace;
			drawString0(g, line, x - g.getFontMetrics().stringWidth(line) / 2, y - 1, shadowOnly);
		}
	}

	public static void drawNineSlice(Graphics g, Image img, int x, int y, int w, int h) {
		final int iw = img.getWidth(null), ih = img.getHeight(null);
		final int sw = (int) (iw / 3f), sh = (int) (ih / 3f);
		// top left
		g.drawImage(img, x, y, x + sw, y + sh, 0, 0, sw, sh, null);
		// top middle
		g.drawImage(img, x + sw, y, x + (w - sw), y + sh, sw, 0, sw * 2, sh, null);
		// top right
		g.drawImage(img, x + (w - sw), y, x + w, y + sh, sw * 2, 0, sw * 3, sh, null);
		// center left
		g.drawImage(img, x, y + sh, x + sw, y + (h - sh), 0, sh, sw, sh * 2, null);
		// center middle
		g.drawImage(img, x + sw, y + sh, x + (w - sw), y + (h - sh), sw, sh, sw * 2, sh * 2, null);
		// center right
		g.drawImage(img, x + (w - sw), y + sh, x + w, y + (h - sh), sw * 2, sh, sw * 3, sh * 2, null);
		// bottom left
		g.drawImage(img, x, y + (h - sh), x + sw, y + h, 0, sh * 2, sw, sh * 3, null);
		// bottom middle
		g.drawImage(img, x + sw, y + (h - sh), x + (w - sw), y + h, sw, sh * 2, sw * 2, sh * 3, null);
		// bottom right
		g.drawImage(img, x + (w - sw), y + (h - sh), x + w, y + h, sw * 2, sh * 2, sw * 3, sh * 3, null);
	}

	public static void drawCheckeredGrid(Graphics g, int x, int y, int w, int h) {
		for (int py = 0; py < h; py += 2)
			for (int px = 0; px < w; px += 2)
				g.drawImage(Resources.grid, x + px, y + py, null);
	}

	public static String intsToString(int... nums) {
		String ret = "(";
		for (int i = 0; i < nums.length; i++) {
			ret += nums[i];
			if (i != nums.length - 1)
				ret += ",";
		}
		ret += ")";
		return ret;
	}

	public static boolean pointInRectangle(int px, int py, int rx, int ry, int rw, int rh) {
		return (px >= rx && px <= rx + rw) && (py >= ry && py <= ry + rh);
	}

	// code from https://stackoverflow.com/a/2581754
	public static <K extends Comparable<? super K>, V> Map<K, V> sortMapByKey(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	// end code from stack overflow

	public static <K> Map<K, String> sortStringMapByValue(Map<K, String> map, Function<String, String> processor) {
		List<Map.Entry<K, String>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, (o1, o2) -> {
			String s1 = processor.apply(o1.getValue());
			String s2 = processor.apply(o2.getValue());
			return s1.compareTo(s2);
		});
		Map<K, String> result = new LinkedHashMap<>();
		for (Map.Entry<K, String> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static String padLeft(String str, String pad, int length) {
		while (str.length() < length)
			str = pad + str;
		return str;
	}

	public static <K, V> K getKey(Map<K, V> map, V value) {
		if (!map.containsValue(value))
			return null;
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue().equals(value))
				return entry.getKey();
		}
		return null;
	}

	// code from https://stackoverflow.com/a/14225857
	public static GraphicsConfiguration getGraphicsConfiguration() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	}

	public static BufferedImage createCompatibleImage(int width, int height, int transparency) {
		BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width, height, transparency);
		image.coerceData(true);
		return image;
	}

	public static void applyQualityRenderingHints(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	}

	public static BufferedImage generateMask(BufferedImage imgSource, Color color, float alpha) {
		int imgWidth = imgSource.getWidth();
		int imgHeight = imgSource.getHeight();

		BufferedImage imgMask = createCompatibleImage(imgWidth, imgHeight, Transparency.TRANSLUCENT);
		Graphics2D g2 = imgMask.createGraphics();
		applyQualityRenderingHints(g2);

		g2.drawImage(imgSource, 0, 0, null);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
		g2.setColor(color);

		g2.fillRect(0, 0, imgSource.getWidth(), imgSource.getHeight());
		g2.dispose();

		return imgMask;
	}

	public static BufferedImage generateMask(BufferedImage imgSource, Color color, int alpha) {
		return generateMask(imgSource, color, alpha / 255f);
	}

	public static BufferedImage tint(BufferedImage master, BufferedImage tint) {
		int imgWidth = master.getWidth();
		int imgHeight = master.getHeight();

		BufferedImage tinted = createCompatibleImage(imgWidth, imgHeight, Transparency.TRANSLUCENT);
		Graphics2D g2 = tinted.createGraphics();
		applyQualityRenderingHints(g2);

		g2.drawImage(master, 0, 0, null);
		g2.drawImage(tint, 0, 0, null);
		g2.dispose();

		return tinted;
	}

	public static BufferedImage colorImage(BufferedImage image, int red, int green, int blue, int alpha) {
		return tint(image, generateMask(image, new Color(red, green, blue), alpha));
	}

	public static BufferedImage colorImage(BufferedImage image, Color color) {
		return colorImage(image, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
	// end code from stack overflow

	// code from https://stackoverflow.com/a/24875569
	private static JColorChooser cc;

	/**
	 * Hides controls for configuring color transparency on the specified color
	 * chooser.
	 */
	public static void hideTransparencyControls(JColorChooser cc) {
		AbstractColorChooserPanel[] colorPanels = cc.getChooserPanels();
		for (int i = 0; i < colorPanels.length; i++) {
			AbstractColorChooserPanel cp = colorPanels[i];
			try {
				Field f = cp.getClass().getDeclaredField("panel");
				f.setAccessible(true);
				Object colorPanel = f.get(cp);

				Field f2 = colorPanel.getClass().getDeclaredField("spinners");
				f2.setAccessible(true);
				Object sliders = f2.get(colorPanel);

				Object transparencySlider = java.lang.reflect.Array.get(sliders, 3);
				if (i == colorPanels.length - 1)
					transparencySlider = java.lang.reflect.Array.get(sliders, 4);

				Method setVisible = transparencySlider.getClass().getDeclaredMethod("setVisible", boolean.class);
				setVisible.setAccessible(true);
				setVisible.invoke(transparencySlider, false);
			} catch (Throwable t) {
			}
		}
	}

	/**
	 * Shows a modal color chooser dialog and blocks until the dialog is closed.
	 *
	 * @param component
	 *            the parent component for the dialog; may be null
	 * @param title
	 *            the dialog's title
	 * @param initialColor
	 *            the initial color set when the dialog is shown
	 * @param showTransparencyControls
	 *            whether to show controls for configuring the color's transparency
	 * @return the chosen color or null if the user canceled the dialog
	 */
	public static Color showColorChooserDialog(Component component, String title, Color initialColor,
			boolean showTransparencyControls) {
		cc = new JColorChooser(initialColor != null ? initialColor : Color.white);
		if (!showTransparencyControls)
			hideTransparencyControls(cc);
		Color[] result = new Color[1];
		ActionListener okListener = e -> result[0] = cc.getColor();
		JDialog dialog = JColorChooser.createDialog(component, title, true, cc, okListener, null);
		dialog.setVisible(true);
		dialog.dispose();
		return result[0];
	}
	// end code from stack overflow

	private static JFileChooser fc;

	public static int openFileChooser(String title, FileFilter[] filters, int currentFilter, File currentDirectory,
			boolean allowAllFilesFilter, boolean openOrSave) {
		fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		boolean noFilters = false;
		if (filters == null || filters.length == 0)
			noFilters = true;
		fc.setAcceptAllFileFilterUsed(allowAllFilesFilter || noFilters);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		for (FileFilter filter : filters)
			fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filters[currentFilter]);
		fc.setDialogTitle(title);
		fc.setCurrentDirectory(currentDirectory);
		int ret = 0;
		if (openOrSave)
			ret = fc.showSaveDialog(Main.window);
		else
			ret = fc.showOpenDialog(Main.window);
		return ret;
	}

	public static int openFileChooser(String title, FileFilter filter, File currentDirectory,
			boolean allowAllFilesFilter, boolean openOrSave) {
		FileFilter[] filters = new FileFilter[1];
		filters[0] = filter;
		return openFileChooser(title, filters, 0, currentDirectory, allowAllFilesFilter, openOrSave);
	}

	public static File getSelectedFile() {
		if (fc == null)
			return null;
		return fc.getSelectedFile();
	}

	public static Rectangle str2Rect(String s) {
		if (!s.contains(":"))
			return new Rectangle(0, 0, 0, 0);
		Scanner sc = new Scanner(s);
		sc.useDelimiter(":");
		int l, u, r, d;
		l = sc.nextInt();
		u = sc.nextInt();
		r = sc.nextInt();
		d = sc.nextInt();
		sc.close();
		return new Rectangle(l, u, r, d);
	}

	public static String rect2Str(Rectangle r) {
		return r.x + ":" + r.y + ":" + r.width + ":" + r.height;
	}

}
