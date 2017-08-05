package com.leo.cse.frontend.ui.components;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JOptionPane;

import com.leo.cse.backend.exe.ExeData;
import com.leo.cse.backend.exe.MapInfo;
import com.leo.cse.backend.profile.Profile;
import com.leo.cse.frontend.FrontUtils;
import com.leo.cse.frontend.MCI;
import com.leo.cse.frontend.Main;

public class MapBox extends DefineBox {

	private Map<Integer, String> map;

	public MapBox(int x, int y, int width, int height, Supplier<Boolean> sSup) {
		super(x, y, width, height, new Supplier<Integer>() {
			@Override
			public Integer get() {
				return Profile.getMap();
			}
		}, new Function<Integer, Integer>() {
			@Override
			public Integer apply(Integer t) {
				Profile.setMap(t);
				return t;
			}
		}, sSup, "Map", "map");
	}

	@Override
	public void render(Graphics g) {
		loadMap();
		g.setColor(Main.COLOR_BG);
		g.fillRect(x, y, width, height - 1);
		g.setColor(Main.lineColor);
		g.drawRect(x, y, width, height - 1);
		FrontUtils.drawString(g, map.get(vSup.get()), x + 3, y - 1);
	}

	@Override
	public boolean onClick(int x, int y, boolean shiftDown, boolean ctrlDown) {
		String nVal = FrontUtils.showSelectionDialog(Main.window, "Select " + description, map.values(),
				map.get(vSup.get()));
		if (nVal == null)
			return true;
		Integer i = FrontUtils.getKey(map, nVal);
		if (i == null) {
			JOptionPane.showMessageDialog(Main.window, "Value \"" + nVal + "\" is unknown!", "Unknown value",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		update.apply(i);
		return true;
	}

	private void loadMap() {
		map = new HashMap<Integer, String>();
		if (ExeData.isLoaded()) {
			for (int i = 0; i < ExeData.getMapInfoCount(); i++) {
				MapInfo mi = ExeData.getMapInfo(i);
				map.put(i, mi.getFileName() + " - " + mi.getMapName());
			}
		} else {
			Map<Integer, String> temp = MCI.getAll(type);
			for (Map.Entry<Integer, String> entry : temp.entrySet())
				map.put(entry.getKey(), entry.getKey() + " - " + entry.getValue());
		}
		if (sSup.get())
			map = FrontUtils.sortStringMapByValue(map, new Function<String, String>() {
				@Override
				public String apply(String t) {
					int i = t.lastIndexOf('-');
					if (i < 0)
						return t;
					return t.substring(i + 1, t.length() - 1);
				}
			});
		for (Map.Entry<Integer, String> entry : map.entrySet())
			entry.setValue(entry.getKey() + " - " + entry.getValue());
	}

}