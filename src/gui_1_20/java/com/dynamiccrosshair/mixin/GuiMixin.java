package com.dynamiccrosshair.mixin;

import com.dynamiccrosshair.Canvas;
import com.dynamiccrosshair.CrosshairRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 1.20 - 1.20.4: Gui.renderCrosshair(GuiGraphics). */
@Mixin(Gui.class)
public abstract class GuiMixin {
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void dynamiccrosshair$render(GuiGraphics graphics, CallbackInfo ci) {
		CrosshairRenderer.render(new Canvas() {
			@Override public int width() { return graphics.guiWidth(); }
			@Override public int height() { return graphics.guiHeight(); }
			@Override public void fill(int x1, int y1, int x2, int y2, int argb) { graphics.fill(x1, y1, x2, y2, argb); }
		});
		ci.cancel();
	}
}
