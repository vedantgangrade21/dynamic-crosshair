package com.dynamiccrosshair;

/** The three drawing calls the crosshair needs, so the renderer doesn't depend on GuiGraphics' name. */
public interface Canvas {
	int width();

	int height();

	void fill(int x1, int y1, int x2, int y2, int argb);
}
