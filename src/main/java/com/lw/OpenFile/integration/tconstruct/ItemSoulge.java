package com.lw.OpenFile.integration.tconstruct;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.List;

public class ItemSoulge extends SwordCore {

    private static final String TAG_TARGETED = "targeted";
    private static final String TAG_READY_TO_DIE = "ready_to_die";
    private static final int DEFAULT_EXERT_TIMES = 36;
    private static final int DEFAULT_ATTACK_INTERVAL = 4;
    private static final int MAX_TARGETS = 10;
    private static final float DEFAULT_DETECTION_RANGE = 36.0F;
    private static final float DEFAULT_EXECUTE_THRESHOLD = 0.36F;
    private static final float EXECUTE_DAMAGE = 200.0F;

    public ItemSoulge() {
        super(
                PartMaterialType.head(TinkerTools.knifeBlade),
                PartMaterialType.extra(OpenFileTinkerTools.soulgeHeart),
                PartMaterialType.head(TinkerTools.knifeBlade),
                PartMaterialType.head(TinkerTools.largeSwordBlade),
                PartMaterialType.handle(TinkerTools.toughToolRod)
        );
        addCategory(Category.WEAPON);
    }

    @Override
    public float damagePotential() {
        return 0.8F;
    }

    @Override
    public double attackSpeed() {
        return 1.0D;
    }

    @Override
    public float damageCutoff() {
        return 20.0F;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
            return ActionResult.newResult(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase user, int count) {
        if (!user.world.isRemote) {
            SoulgeHeartStats stats = getSoulgeHeartStats(stack);
            EntityLivingBase target = findPointedEntity(user.world, user, stats.detectionRange);
            if (target != null && target.isEntityAlive()) {
                addTargetMark(target, stats);
            }
            if (user.ticksExisted % stats.attackInterval == 0 && user instanceof EntityPlayer) {
                attackMarkedTargets(stack, (EntityPlayer) user, stats);
            }
        }
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        HeadMaterialStats firstBlade = materials.get(0).getStats(HeadMaterialStats.TYPE);
        ExtraMaterialStats heart = materials.get(1).getStats(ExtraMaterialStats.TYPE);
        HeadMaterialStats secondBlade = materials.get(2).getStats(HeadMaterialStats.TYPE);
        HeadMaterialStats broadBlade = materials.get(3).getStats(HeadMaterialStats.TYPE);
        HandleMaterialStats handle = materials.get(4).getStats(HandleMaterialStats.TYPE);

        ToolNBT data = new ToolNBT();
        data.head(firstBlade, secondBlade, broadBlade);
        data.extra(heart);
        data.handle(handle);
        data.attack = data.attack * 0.8F + 0.8F;
        data.durability = (int) (data.durability * 1.2F);
        data.attackSpeedMultiplier = 1.0F;
        data.modifiers = 3;
        return data;
    }

    private static void addTargetMark(EntityLivingBase target, SoulgeHeartStats stats) {
        NBTTagCompound data = target.getEntityData();
        int marks = data.getInteger(TAG_TARGETED);
        if (marks < stats.maxMarks) {
            data.setInteger(TAG_TARGETED, Math.min(stats.maxMarks, marks + stats.exertTimes));
        }
    }

    private void attackMarkedTargets(ItemStack stack, EntityPlayer player, SoulgeHeartStats stats) {
        AxisAlignedBB box = player.getEntityBoundingBox().grow(stats.detectionRange);
        List<EntityLivingBase> entities = player.world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        int attacked = 0;
        float damage = ToolHelper.getActualAttack(stack);
        for (EntityLivingBase target : entities) {
            if (attacked >= MAX_TARGETS) {
                return;
            }
            if (target == player) {
                continue;
            }
            NBTTagCompound data = target.getEntityData();
            int marks = data.getInteger(TAG_TARGETED);
            if (marks <= 0 || data.hasKey(TAG_READY_TO_DIE)) {
                continue;
            }
            if (dealDamage(stack, player, target, damage)) {
                ToolHelper.damageTool(stack, 1, player);
                data.setInteger(TAG_TARGETED, marks - 1);
                if (target.getHealth() <= target.getMaxHealth() * stats.executeThreshold && target.isEntityAlive()) {
                    executeTarget(player, target);
                }
                if (!target.isEntityAlive()) {
                    data.removeTag(TAG_TARGETED);
                }
                attacked++;
            }
        }
    }

    private static void executeTarget(EntityPlayer player, EntityLivingBase target) {
        NBTTagCompound data = target.getEntityData();
        if (data.hasKey(TAG_READY_TO_DIE)) {
            return;
        }
        data.setInteger(TAG_READY_TO_DIE, 9);
        target.attackEntityFrom(DamageSource.causePlayerDamage(player), EXECUTE_DAMAGE);
    }

    private static EntityLivingBase findPointedEntity(World world, EntityLivingBase user, float range) {
        Vec3d eyes = user.getPositionEyes(1.0F);
        Vec3d look = user.getLook(1.0F);
        Vec3d reach = eyes.add(look.x * range, look.y * range, look.z * range);
        AxisAlignedBB box = user.getEntityBoundingBox().expand(look.x * range, look.y * range, look.z * range).grow(1.0D);
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        EntityLivingBase closest = null;
        double closestDistance = range;

        for (EntityLivingBase entity : entities) {
            if (entity == user || !entity.canBeCollidedWith()) {
                continue;
            }
            RayTraceResult hit = entity.getEntityBoundingBox().grow(0.3D).calculateIntercept(eyes, reach);
            if (hit != null) {
                double distance = eyes.distanceTo(hit.hitVec);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = entity;
                }
            }
        }
        return closest;
    }

    private static SoulgeHeartStats getSoulgeHeartStats(ItemStack stack) {
        NBTTagList materials = TagUtil.getBaseMaterialsTagList(stack);
        if (materials.tagCount() > 1) {
            return getSoulgeHeartStats(materials.getStringTagAt(1));
        }
        return SoulgeHeartStats.DEFAULT;
    }

    public static boolean hasSoulgeHeartStats(String material) {
        switch (material) {
            case "aetherium":
            case "obsidian":
            case "heart_of_steel":
            case "true_infinity":
            case "violium":
            case "cobalt":
            case "manyullyn":
            case "exotic_matter":
            case "exo_alloy":
            case "ultra_dense":
            case "elysia":
                return true;
            default:
                return false;
        }
    }

    static SoulgeHeartStats getSoulgeHeartStats(String material) {
        switch (material) {
            case "aetherium":
                return new SoulgeHeartStats(80.0F, 60, 3, 0.36F);
            case "obsidian":
                return new SoulgeHeartStats(20.0F, 8, 6, 0.16F);
            case "heart_of_steel":
                return new SoulgeHeartStats(35.0F, 60, 10, 0.0F);
            case "true_infinity":
                return new SoulgeHeartStats(100.0F, 100, 1, 0.9F);
            case "violium":
                return new SoulgeHeartStats(45.0F, 45, 4, 0.28F);
            case "cobalt":
                return new SoulgeHeartStats(18.0F, 30, 10, 0.3F);
            case "manyullyn":
                return new SoulgeHeartStats(30.0F, 40, 5, 0.27F);
            case "exotic_matter":
                return new SoulgeHeartStats(60.0F, 60, 4, 0.45F);
            case "exo_alloy":
                return new SoulgeHeartStats(100.0F, 100, 1, 0.72F);
            case "ultra_dense":
                return new SoulgeHeartStats(70.0F, 100, 10, 0.38F);
            case "elysia":
            default:
                return SoulgeHeartStats.DEFAULT;
        }
    }

    static final class SoulgeHeartStats {
        private static final SoulgeHeartStats DEFAULT = new SoulgeHeartStats(
                DEFAULT_DETECTION_RANGE,
                DEFAULT_EXERT_TIMES,
                DEFAULT_ATTACK_INTERVAL,
                DEFAULT_EXECUTE_THRESHOLD
        );

        final float detectionRange;
        final int exertTimes;
        final int maxMarks;
        final int attackInterval;
        final float executeThreshold;

        private SoulgeHeartStats(float detectionRange, int exertTimes, int attackInterval, float executeThreshold) {
            this.detectionRange = detectionRange;
            this.exertTimes = exertTimes;
            this.maxMarks = exertTimes * 3;
            this.attackInterval = Math.max(1, attackInterval);
            this.executeThreshold = executeThreshold;
        }
    }
}
