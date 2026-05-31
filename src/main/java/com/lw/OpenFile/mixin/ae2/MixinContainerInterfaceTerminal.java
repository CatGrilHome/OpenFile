package com.lw.OpenFile.mixin.ae2;

import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.items.misc.ItemEncodedPattern;
import com.lw.OpenFile.util.AEPatternInfo;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for AE2's ContainerInterfaceTerminal to attach machine/structure name
 * to encoded patterns when they are created in the Interface Terminal.
 *
 * Interface Terminals only create processing patterns.
 */
@Mixin(value = ContainerInterfaceTerminal.class, remap = false)
public abstract class MixinContainerInterfaceTerminal {

    /**
     * Injects at the tail of detectAndSendChanges to catch newly encoded patterns.
     * ContainerInterfaceTerminal has no dedicated encode() method;
     * encoding is handled via doAction/InventoryAction.
     * We scan slots each tick and tag any untagged encoded patterns.
     */
    @Inject(method = "detectAndSendChanges", at = @At("TAIL"), remap = false)
    private void onDetectAndSendChanges(CallbackInfo ci) {
        ContainerInterfaceTerminal container = (ContainerInterfaceTerminal) (Object) this;
        for (Slot slot : container.inventorySlots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.getItem() instanceof ItemEncodedPattern
                    && AEPatternInfo.getEncodedAt(stack) == null) {
                AEPatternInfo.setEncodedAt(stack, "ME接口终端");
                AEPatternInfo.setPatternType(stack, "处理样板");
            }
        }
    }
}
