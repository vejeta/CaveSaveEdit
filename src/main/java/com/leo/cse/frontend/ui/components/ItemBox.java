package com.leo.cse.frontend.ui.components;

//import java.awt.Color;
import java.awt.Graphics;
import java.util.function.Function;
import java.util.function.Supplier;

import com.leo.cse.backend.exe.ExeData;
import com.leo.cse.backend.profile.Profile;
import com.leo.cse.frontend.FrontUtils;
import com.leo.cse.frontend.MCI;
import com.leo.cse.frontend.Main;

public class ItemBox extends DefineBox {

	// private int id;

	public ItemBox(int x, int y, int width, int height, int itemId) {
		super(x, y, width, height, new Supplier<Integer>() {
			@Override
			public Integer get() {
				return Profile.getItem(itemId);
			}
		}, new Function<Integer, Integer>() {
			@Override
			public Integer apply(Integer t) {
				Profile.setItem(itemId, t);
				return t;
			}
		}, "Item", "item " + (itemId + 1));
		// id = itemId;
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Main.COLOR_BG);
		g.fillRect(x, y, width, height - 1);
		g.setColor(Main.lineColor);
		g.drawRect(x, y, width, height - 1);
		/*
		if (id != 0 && Profile.getItem(id - 1) == 0) {
			Color lc2 = new Color(Main.lineColor.getRed(), Main.lineColor.getGreen(), Main.lineColor.getBlue(), 31);
			g.setColor(lc2);
			g.fillRect(x, y, width, height - 1);
			return;
		}
		*/
		int item = vSup.get();
		FrontUtils.drawStringCentered(g, item + " - " + MCI.get(type, item), x + width / 2, y + 31);
		if (item == 0)
			return;
		if (!ExeData.isLoaded())
			return;
		int sourceX = (item % 8) * 64;
		int sourceY = (item / 8) * 32;
		g.drawImage(ExeData.getImage(ExeData.getItemImage()), x + width / 2 - 32, y + 1, x + width / 2 + 32, y + 33,
				sourceX, sourceY, sourceX + 64, sourceY + 32, null);
	}

	@Override
	public boolean onClick(int x, int y, boolean shiftDown, boolean ctrlDown) {
		/*
		if (id != 0 && Profile.getItem(id - 1) == 0)
			return false;
		*/
		return super.onClick(x, y, shiftDown, ctrlDown);
	}

}