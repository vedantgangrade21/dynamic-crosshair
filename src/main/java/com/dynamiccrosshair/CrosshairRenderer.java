package com.dynamiccrosshair;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Draws the crosshair with plain filled rectangles, which is the one GUI call that exists
 * unchanged from 1.20 to 26.x.
 *
 *  normal : small white plus
 *  entity : red plus with small red corner brackets
 *  block  : bigger white plus with bigger white corner brackets
 */
public final class CrosshairRenderer {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int RED = 0xFFE0202A;

	private CrosshairRenderer() {}

	public static void render(Canvas g) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.level == null || !mc.options.getCameraType().isFirstPerson()) return;

		HitResult hit = mc.hitResult;
		boolean onEntity = hit instanceof EntityHitResult;
		boolean onBlock = !onEntity && isUsableBlock(mc, player, hit);

		if (mc.gameMode != null && mc.gameMode.getPlayerMode() == GameType.SPECTATOR && !onEntity) return;

		int cx = (g.width() - 15) / 2 + 7;
		int cy = (g.height() - 15) / 2 + 7;

		if (onEntity) {
			plus(g, cx, cy, 4, RED);
			brackets(g, cx, cy, 6, 2, RED);
		} else if (onBlock) {
			plus(g, cx, cy, 6, WHITE);
			brackets(g, cx, cy, 10, 3, WHITE);
		} else {
			plus(g, cx, cy, 4, WHITE);
		}

		attackIndicator(g, mc, player, cx, cy + (onBlock ? 13 : 9));
	}

	private static boolean isUsableBlock(Minecraft mc, LocalPlayer player, HitResult hit) {
		if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) return false;
		// Sneaking with something in hand skips block interaction in vanilla.
		if (player.isSecondaryUseActive()
				&& (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())) return false;
		BlockState state = mc.level.getBlockState(bhr.getBlockPos());
		return Interactables.isInteractable(state, mc.level, bhr.getBlockPos());
	}

	private static void attackIndicator(Canvas g, Minecraft mc, LocalPlayer player, int cx, int y) {
		if (mc.options.attackIndicator().get() != AttackIndicatorStatus.CROSSHAIR) return;
		float charge = player.getAttackStrengthScale(0f);
		if (charge >= 1f) return;
		g.fill(cx - 8, y, cx + 8, y + 2, 0x80000000);
		g.fill(cx - 8, y, cx - 8 + Math.round(16 * charge), y + 2, 0xFFFFFFFF);
	}

	private static void plus(Canvas g, int cx, int cy, int arm, int color) {
		rect(g, cx - arm, cy, cx + arm + 1, cy + 1, color);
		rect(g, cx, cy - arm, cx + 1, cy, color);
		rect(g, cx, cy + 1, cx + 1, cy + arm + 1, color);
	}

	private static void brackets(Canvas g, int cx, int cy, int half, int len, int color) {
		int l = cx - half, r = cx + half + 1, t = cy - half, b = cy + half + 1;
		// top-left
		rect(g, l, t, l + len, t + 1, color);
		rect(g, l, t + 1, l + 1, t + len, color);
		// top-right
		rect(g, r - len, t, r, t + 1, color);
		rect(g, r - 1, t + 1, r, t + len, color);
		// bottom-left
		rect(g, l, b - 1, l + len, b, color);
		rect(g, l, b - len, l + 1, b - 1, color);
		// bottom-right
		rect(g, r - len, b - 1, r, b, color);
		rect(g, r - 1, b - len, r, b - 1, color);
	}

	/** Filled rect with a soft 1px drop shadow so the crosshair stays readable on bright blocks. */
	private static void rect(Canvas g, int x1, int y1, int x2, int y2, int color) {
		int alpha = color >>> 24;
		g.fill(x1 + 1, y1 + 1, x2 + 1, y2 + 1, (alpha * 3 / 8) << 24);
		g.fill(x1, y1, x2, y2, color);
	}
}
