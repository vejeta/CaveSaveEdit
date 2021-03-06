package com.leo.cse.frontend.ui.components.box;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.function.Function;
import java.util.function.Supplier;

import com.leo.cse.frontend.FrontUtils;
import com.leo.cse.frontend.MCI;
import com.leo.cse.frontend.Main;
import com.leo.cse.frontend.Resources;
import com.leo.cse.frontend.ui.components.Component;

public class BooleanBox extends Component {

	private boolean labelIsMCI, missingMCI;
	private String label;
	private Supplier<Boolean> vSup;
	private Function<Boolean, Boolean> update;

	public BooleanBox(String label, boolean labelIsMCI, int x, int y, Supplier<Boolean> vSup,
			Function<Boolean, Boolean> update) {
		super(label, x, y, 15, 15);
		this.label = label;
		this.labelIsMCI = labelIsMCI;
		this.vSup = vSup;
		this.update = update;
	}

	@Override
	public void render(Graphics g, Rectangle viewport) {
		String t = label;
		if (labelIsMCI) {
			t = MCI.getNullable(t);
			if (t == null) {
				missingMCI = true;
				return;
			} else
				missingMCI = false;
		}
		if (hover)
			g.setColor(new Color(Main.lineColor.getRed(), Main.lineColor.getGreen(), Main.lineColor.getBlue(), 31));
		else
			g.setColor(Main.COLOR_BG);
		g.fillRect(x, y, getWidth(), getHeight());
		g.setColor(Main.lineColor);
		g.setFont(Resources.font);
		BufferedImage chkImage = (vSup.get() ? Resources.checkboxOn : Resources.checkboxOff);
		boolean bEnabled = enabled.get();
		if (!bEnabled || missingMCI)
			chkImage = Resources.checkboxDisabled;
		g.drawImage(chkImage, x, y, null);
		FrontUtils.drawString(g, t, x + 18, y - 2, !bEnabled || missingMCI);
	}

	@Override
	public void onClick(int x, int y, boolean shiftDown, boolean ctrlDown) {
		if (!enabled.get() || missingMCI)
			return;
		update.apply(!vSup.get());
	}

}
