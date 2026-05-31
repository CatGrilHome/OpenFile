package com.lw.OpenFile.mixin.ae2;

import appeng.container.implementations.ContainerPatternEncoder;
import appeng.items.misc.ItemEncodedPattern;
import com.lw.OpenFile.util.AEPatternInfo;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for AE2-UEL's ContainerPatternEncoder — the parent class that
 * actually defines encode() and encodeAndMoveToInventory().
 *
 * In AE2-UEL, ContainerPatternTerm extends ContainerPatternEncoder,
 * which extends ContainerMEMonitorable.
 * The encode()/encodeAndMoveToInventory() methods live in
 * ContainerPatternEncoder, not in ContainerPatternTerm.
 * We target the parent so Mixin can find the methods.
 */
@Mixin(value = ContainerPatternEncoder.class, remap = false)
public abstract class MixinContainerPatternTerm {

    /**
     * Inject after encode() — creates the pattern in the output slot.
     */
    @Inject(method = "encode", at = @At("TAIL"), remap = false)
    private void onPatternEncoded(CallbackInfo ci) {
        tagPatternInSlots();
    }

    /**
     * Inject after encodeAndMoveToInventory() — the GUI button handler.
     */
    @Inject(method = "encodeAndMoveToInventory", at = @At("TAIL"), remap = false)
    private void onPatternEncodedAndMoved(CallbackInfo ci) {
        tagPatternInSlots();
    }

    private void tagPatternInSlots() {
        ContainerPatternEncoder container = (ContainerPatternEncoder) (Object) this;
        for (Slot slot : container.inventorySlots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemEncodedPattern
                    && AEPatternInfo.getEncodedAt(stack) == null) {
                AEPatternInfo.setEncodedAt(stack, "ME样板终端");
                AEPatternInfo.setPatternType(stack,
                        container.craftingMode ? "合成样板" : "处理样板");
            }
        }
    }
}
