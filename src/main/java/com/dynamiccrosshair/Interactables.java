package com.dynamiccrosshair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Decides whether a block does something on an empty-hand right click.
 *
 * A block is "right-clickable" when its class (or a superclass below Block) overrides the
 * vanilla "use" method. The method is found by its parameter types instead of its name, so
 * this works with every mapping and every version (onUse / use / useWithoutItem).
 */
public final class Interactables {
	private static final List<Method> USE_METHODS = findUseMethods();
	private static final Map<Class<?>, Boolean> CACHE = new ConcurrentHashMap<>();

	private Interactables() {}

	public static boolean isInteractable(BlockState state, Level level, BlockPos pos) {
		if (state.isAir()) return false;
		if (CACHE.computeIfAbsent(state.getBlock().getClass(), Interactables::overridesUse)) return true;
		return state.getMenuProvider(level, pos) != null;
	}

	private static List<Method> findUseMethods() {
		List<Method> found = new ArrayList<>();
		for (Method m : BlockBehaviour.class.getDeclaredMethods()) {
			List<Class<?>> params = Arrays.asList(m.getParameterTypes());
			// The item-in-hand variant (useItemOn, 1.20.5+) is skipped: those blocks only react to specific items.
			if (params.contains(BlockState.class) && params.contains(Player.class)
					&& params.contains(BlockHitResult.class) && !params.contains(ItemStack.class)) {
				found.add(m);
			}
		}
		return found;
	}

	private static boolean overridesUse(Class<?> cls) {
		for (Class<?> c = cls; c != null && c != Block.class && c != BlockBehaviour.class; c = c.getSuperclass()) {
			for (Method m : USE_METHODS) {
				try {
					c.getDeclaredMethod(m.getName(), m.getParameterTypes());
					return true;
				} catch (NoSuchMethodException ignored) {
				}
			}
		}
		return false;
	}
}
