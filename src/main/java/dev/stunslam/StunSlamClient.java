package dev.stunslam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Client-side only. When you left-click a player whose shield is actually
 * blocking you, the mod swaps your selected hotbar slot to an axe for that hit,
 * then returns to the slot you were on a couple of ticks later (so you're back
 * on your mace for the stun slam follow-up).
 */
public class StunSlamClient implements ClientModInitializer {

    /** Ticks to stay on the axe before swapping back. 2 = back on the next tick. */
    private static final int SWAP_BACK_TICKS = 2;

    /** Slot to return to, or -1 when no swap is pending. */
    private static int returnSlot = -1;
    private static int ticksLeft = 0;

    @Override
    public void onInitializeClient() {
        AttackEntityCallback.EVENT.register(StunSlamClient::onAttack);
        ClientTickEvents.END_CLIENT_TICK.register(StunSlamClient::onTick);
    }

    private static ActionResult onAttack(PlayerEntity player, World world, Hand hand,
                                         Entity target, EntityHitResult hit) {
        if (!world.isClient() || hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!(target instanceof PlayerEntity victim) || victim == player) return ActionResult.PASS;
        if (!isShieldBlockingUs(victim, player)) return ActionResult.PASS;

        // Already holding an axe: just keep it out until the pending swap-back (if any) fires.
        if (player.getMainHandStack().isIn(ItemTags.AXES)) {
            if (returnSlot >= 0) ticksLeft = SWAP_BACK_TICKS;
            return ActionResult.PASS;
        }

        PlayerInventory inv = player.getInventory();
        int axeSlot = findAxeInHotbar(inv);
        if (axeSlot < 0) return ActionResult.PASS;

        if (returnSlot < 0) returnSlot = inv.getSelectedSlot();
        inv.setSelectedSlot(axeSlot);
        ticksLeft = SWAP_BACK_TICKS;
        return ActionResult.PASS; // let the attack continue, now with the axe selected
    }

    private static void onTick(MinecraftClient client) {
        if (returnSlot < 0) return;
        if (--ticksLeft > 0) return;

        if (client.player != null) {
            client.player.getInventory().setSelectedSlot(returnSlot);
        }
        returnSlot = -1;
    }

    private static int findAxeInHotbar(PlayerInventory inv) {
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isIn(ItemTags.AXES)) return i;
        }
        return -1;
    }

    /** Same front-facing test vanilla uses: shield only blocks attacks from the front. */
    private static boolean isShieldBlockingUs(PlayerEntity victim, PlayerEntity attacker) {
        if (!victim.isBlocking()) return false;
        Vec3d view = victim.getRotationVec(1.0F);
        double dx = victim.getX() - attacker.getX();
        double dz = victim.getZ() - attacker.getZ();
        return dx * view.x + dz * view.z < 0.0;
    }
}
