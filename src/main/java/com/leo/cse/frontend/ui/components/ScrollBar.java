package com.leo.cse.frontend.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import com.leo.cse.frontend.FrontUtils;
import com.leo.cse.frontend.Main;
import com.leo.cse.frontend.Resources;

public class ScrollBar extends Component implements IDraggable, IScrollable {

	public static final int WIDTH = 20;

	protected int scrollbarY;
	protected boolean scrolling;
	protected static final int ELEM_UP = 0, ELEM_DOWN = 1, ELEM_BAR = 2;
	protected int hover;

	public ScrollBar(int x, int y, int height) {
		super("ScrollBar", x, y, WIDTH, height);
		limitScroll();
	}

	protected void limitScroll() {
		scrollbarY = Math.max(y + 2 + width, Math.min(y + height - 18 - width, scrollbarY));
	}

	public float getValue() {
		float sby = scrollbarY;
		sby -= y + 2 + width;
		sby = sby / (height - width * 3);
		return sby;
	}

	@Override
	public void onScroll(int rotations, boolean shiftDown, boolean ctrlDown) {
		if (!enabled.get())
			return;
		if (shiftDown)
			rotations *= 10;
		if (ctrlDown)
			rotations *= 100;
		scrollbarY += rotations;
		limitScroll();
	}

	@Override
	public void onClick(int x, int y, boolean shiftDown, boolean ctrlDown) {
		if (!enabled.get())
			return;
		scrolling = false;
		int amount = 1;
		if (shiftDown)
			amount *= 10;
		if (ctrlDown)
			amount *= 100;
		if (y < this.y + width)
			scrollbarY -= amount;
		else if (y > this.y + height - width)
			scrollbarY += amount;
		else if (y > this.y + width && y < this.y + height - width)
			onDrag(x, y + 17, shiftDown, ctrlDown);
		limitScroll();
	}

	@Override
	public void onKey(int code, boolean shiftDown, boolean ctrlDown) {
		if (!enabled.get())
			return;
		if (code == KeyEvent.VK_HOME)
			scrollbarY = 0;
		else if (code == KeyEvent.VK_END)
			scrollbarY = Integer.MAX_VALUE;
		limitScroll();
	}

	@Override
	public void onDrag(int x, int y, boolean shiftDown, boolean ctrlDown) {
		if (!enabled.get())
			return;
		y -= 25;
		if (!scrolling)
			if (y < this.y + width || y > this.y + height - width)
				return;
		scrolling = true;
		scrollbarY = y;
		limitScroll();
	}

	@Override
	public void onDragEnd(int px, int py, boolean shiftDown, boolean ctrlDown) {
		scrolling = false;
	}

	@Override
	public void updateHover(int x, int y, boolean hover, boolean shiftDown, boolean ctrlDown) {
		if (!hover) {
			this.hover = -1;
			return;
		}
		x -= this.x;
		y -= this.y + 17;
		if (y <= width)
			this.hover = ELEM_UP;
		else if (y > height - width)
			this.hover = ELEM_DOWN;
		else if (x > 2 && x <= 18 && y > scrollbarY && y <= scrollbarY + 16)
			this.hover = ELEM_BAR;
		else
			this.hover = -1;
	}

	@Override
	public void render(Graphics g, Rectangle viewport) {
		Color lc2 = new Color(Main.lineColor.getRed(), Main.lineColor.getGreen(), Main.lineColor.getBlue(), 31);
		g.setColor(Main.lineColor);
		g.drawRect(x, y, width, height);
		g.drawLine(x, y + width, x + width, y + width);
		g.drawLine(x, y + height - width, x + width, y + height - width);
		if (enabled.get()) {
			if (hover == ELEM_BAR) {
				g.setColor(lc2);
				g.fillRect(x + 2, scrollbarY, 16, 16);
				g.setColor(Main.lineColor);
			}
			g.drawRect(x + 2, scrollbarY, 16, 16);
		} else {
			g.setColor(lc2);
			FrontUtils.drawCheckeredGrid(g, x + 7, y + 7, 7, 7);
			FrontUtils.drawCheckeredGrid(g, x + 7, y + height - width + 7, 7, 7);
			g.setColor(Main.lineColor);
		}
		if (hover == ELEM_UP) {
			g.setColor(lc2);
			g.fillRect(x, y, width, width);
			g.setColor(Main.lineColor);
		}
		g.drawImage(Resources.arrowUp, x + 6, y + 6, null);
		if (hover == ELEM_DOWN) {
			g.setColor(lc2);
			g.fillRect(x, y + height - width, width, width);
			g.setColor(Main.lineColor);
		}
		g.drawImage(Resources.arrowDown, x + 6, y + height - width + 6, null);
	}

}
