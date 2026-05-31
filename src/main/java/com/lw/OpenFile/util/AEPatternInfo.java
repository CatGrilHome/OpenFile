package com.lw.OpenFile.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Static helpers for reading and writing custom pattern encoding metadata.
 * All data is stored under the namespaced "OpenFile" NBT compound key
 * to avoid collisions with AE2's own NBT and other mods.
 */
public final class AEPatternInfo {

    private static final String TAG_ROOT = "OpenFile";
    private static final String TAG_ENCODED_AT = "EncodedAt";
    private static final String TAG_PATTERN_TYPE = "PatternType";

    private AEPatternInfo() {}

    /** Write the name of the machine/structure where the pattern was encoded. */
    public static void setEncodedAt(ItemStack stack, String location) {
        getOrCreateSubTag(stack).setString(TAG_ENCODED_AT, location);
    }

    /** Read the name of the machine/structure where the pattern was encoded. */
    public static String getEncodedAt(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(TAG_ROOT)) return null;
        NBTTagCompound root = tag.getCompoundTag(TAG_ROOT);
        return root.hasKey(TAG_ENCODED_AT) ? root.getString(TAG_ENCODED_AT) : null;
    }

    /** Write the pattern type (Crafting Pattern / Processing Pattern). */
    public static void setPatternType(ItemStack stack, String type) {
        getOrCreateSubTag(stack).setString(TAG_PATTERN_TYPE, type);
    }

    /** Get or create the "OpenFile" sub-compound in the item's NBT. */
    private static NBTTagCompound getOrCreateSubTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        if (!tag.hasKey(TAG_ROOT)) {
            tag.setTag(TAG_ROOT, new NBTTagCompound());
        }
        return tag.getCompoundTag(TAG_ROOT);
    }
}
