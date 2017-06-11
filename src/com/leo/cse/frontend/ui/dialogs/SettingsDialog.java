package com.leo.cse.frontend.ui.dialogs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.leo.cse.backend.ExeData;
import com.leo.cse.frontend.Config;
import com.leo.cse.frontend.FrontUtils;
import com.leo.cse.frontend.Main;
import com.leo.cse.frontend.Resources;
import com.leo.cse.frontend.ui.SaveEditorPanel;

public class SettingsDialog extends BaseDialog {

	private static final byte[] TEST_STRING = new byte[] { (byte) 'T', (byte) 'e', (byte) 's', (byte) 't' };

	public SettingsDialog() {
		super("Settings", 300, 80);
	}

	@Override
	public void render(Graphics g) {
		super.render(g);
		final int x = getWindowX(), y = getWindowY();
		g.setColor(Main.lineColor);
		g.drawRect(x + 4, y + 4, 292, 17);
		FrontUtils.drawStringCentered(g, "MCI Settings", x + 150, y + 4);
		g.drawImage(Resources.toolbarIcons[Resources.toolbarIcons.length - 2], x + 5, y + 5, null);
		g.drawRect(x + 4, y + 23, 292, 17);
		FrontUtils.drawStringCentered(g, "Change Line Color", x + 150, y + 23);
		g.drawImage(Resources.toolbarIcons[Resources.toolbarIcons.length - 1], x + 5, y + 24, null);
		Image chkImage = (ExeData.doLoadNpc() ? Resources.checkboxOn : Resources.checkboxOff);
		g.drawImage(chkImage, x + 4, y + 43, null);
		FrontUtils.drawString(g, "Load NPCs?", x + 22, y + 41);
		FrontUtils.drawString(g, "Encoding:", x + 4, y + 58);
		g.drawRect(x + 52, y + 59, 244, 17);
		FrontUtils.drawString(g, Main.encoding, x + 54, y + 58);
	}

	@Override
	public boolean onClick(int x, int y) {
		if (super.onClick(x, y))
			return true;
		final int wx = getWindowX(), wy = getWindowY();
		if (FrontUtils.pointInRectangle(x, y, wx + 4, wy + 4, 292, 17)) {
			SaveEditorPanel.panel.setDialogBox(new MCIDialog());
		} else if (FrontUtils.pointInRectangle(x, y, wx + 4, wy + 23, 292, 17)) {
			setLineColor();
		} else if (FrontUtils.pointInRectangle(x, y, wx + 4, wy + 43, 16, 16)) {
			ExeData.setLoadNpc(!ExeData.doLoadNpc());
			SaveEditorPanel.panel.setLoading(true);
			Main.window.repaint();
			SwingUtilities.invokeLater(() -> {
				try {
					ExeData.reload();
				} catch (IOException ignore) {
				} finally {
					Config.setBoolean(Config.KEY_LOAD_NPCS, ExeData.doLoadNpc());
					SwingUtilities.invokeLater(() -> {
						SaveEditorPanel.panel.setLoading(false);
						SaveEditorPanel.panel.addComponents();
						Main.window.repaint();
					});
				}
			});
		} else if (FrontUtils.pointInRectangle(x, y, wx + 52, wy + 59, 244, 17)) {
			String e = null;
			while (e == null) {
				e = JOptionPane.showInputDialog(SaveEditorPanel.panel, "Enter new encoding:", Main.encoding);
				if (e != null) {
					try {
						new String(TEST_STRING, e);
					} catch (UnsupportedEncodingException e1) {
						JOptionPane.showMessageDialog(SaveEditorPanel.panel, "Encoding \"" + e + "\" is unsupported!",
								"Unsupported encoding", JOptionPane.ERROR_MESSAGE);
						e = null;
					}
				} else
					return false;
			}
			Main.encoding = e;
			try {
				ExeData.reload();
			} catch (IOException ignore) {
			}
		}
		return false;
	}

	private void setLineColor() {
		Color temp = FrontUtils.showColorChooserDialog(SaveEditorPanel.panel, "Select new line color", Main.lineColor,
				false);
		if (temp != null) {
			Main.lineColor = temp;
			Resources.colorImages(Main.lineColor);
		}
	}

}
