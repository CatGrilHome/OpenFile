package com.lw.OpenFile.mixin.ae2;

import com.lw.OpenFile.integration.jei.OpenFileJeiPlugin;
import mezz.jei.api.IJeiRuntime;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for AE2's GuiMEMonitorable — the base GUI class for AE2 terminals.
 * Allows pressing the F key while hovering over a JEI ingredient to
 * automatically set the AE2 terminal's search field to that item's name.
 */
@Mixin(targets = "appeng.client.gui.implementations.GuiMEMonitorable", remap = false)
public abstract class MixinGuiMEMonitorable {

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true, remap = false)
    private void onKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        // Key code 33 = F key
        if (keyCode != 33) {
            return;
        }

        IJeiRuntime runtime = OpenFileJeiPlugin.getRuntime();
        if (runtime == null) {
            return;
        }

        // Try to get the ingredient under the mouse from JEI
        Object ingredient = runtime.getIngredientListOverlay().getIngredientUnderMouse();

        // If not found, try the bookmark overlay
        if (ingredient == null) {
            ingredient = runtime.getBookmarkOverlay().getIngredientUnderMouse();
            if (ingredient != null) {
                try {
                    // Bookmarks wrap ingredients; unwrap via reflection
                    ingredient = ingredient.getClass().getField("ingredient").get(ingredient);
                } catch (Exception ignored) {
                    // If we can't unwrap, just use the ingredient as-is
                }
            }
        }

        if (ingredient == null) {
            return;
        }

        // Get the display name
        String name;
        if (ingredient instanceof ItemStack) {
            name = ((ItemStack) ingredient).getDisplayName();
        } else {
            name = ingredient.toString();
        }

        // Strip formatting codes
        name = TextFormatting.getTextWithoutFormattingCodes(name);

        if (name == null || name.isEmpty()) {
            return;
        }

        // Set the AE2 search field text
        setAe2Search(this, name);

        // Cancel the original key event so F doesn't get typed into the search field
        ci.cancel();
    }

    /**
     * Uses reflection to find and set the AE2 terminal's search field text.
     * Walks up the class hierarchy to find the "searchField" field.
     */
    private static void setAe2Search(Object gui, String text) {
        try {
            Class<?> clazz = gui.getClass();
            java.lang.reflect.Field searchField = null;

            while (clazz != null && searchField == null) {
                try {
                    searchField = clazz.getDeclaredField("searchField");
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }

            if (searchField == null) {
                return;
            }

            searchField.setAccessible(true);
            GuiTextField textField = (GuiTextField) searchField.get(gui);
            if (textField != null) {
                textField.setText(text);
            }
        } catch (Exception ignored) {
            // Silently fail if the search field can't be found or set
        }
    }
}
