package com.lw.OpenFile.integration.tconstruct;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;
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

    private final Random rand = new Random();
    private int pendingDamage = 0;

    private static final String TAG_TARGETED = "targeted";
    private static final String TAG_READY_TO_DIE = "ready_to_die";
    private static final int DEFAULT_EXERT_TIMES = 36;
    private static final int DEFAULT_ATTACK_INTERVAL = 4;
    private static final int MAX_TARGETS = 10;
    private static final float DEFAULT_DETECTION_RANGE = 36.0F;
    private static final float DEFAULT_EXECUTE_THRESHOLD = 0.36F;

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
    public List<String> getInformation(ItemStack stack, boolean detailed) {
        List<String> info = super.getInformation(stack, detailed);
        if (detailed) {
            SoulgeHeartStats stats = getSoulgeHeartStats(stack);
            info.add("");
            info.add(net.minecraft.util.text.TextFormatting.WHITE + "检测范围 " + net.minecraft.util.text.TextFormatting.GREEN + Math.round(stats.detectionRange));
            info.add(net.minecraft.util.text.TextFormatting.WHITE + "施加印记层数 " + net.minecraft.util.text.TextFormatting.GREEN + stats.exertTimes);
            info.add(net.minecraft.util.text.TextFormatting.WHITE + "攻击间隔 " + net.minecraft.util.text.TextFormatting.GREEN + stats.attackInterval);
            info.add(net.minecraft.util.text.TextFormatting.WHITE + "斩杀线 " + net.minecraft.util.text.TextFormatting.GREEN + Math.round(stats.executeThreshold * 100) + "%");
        }
        return info;
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
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
        if (pendingDamage > 0 && entity instanceof EntityPlayer) {
            int dmg = pendingDamage;
            pendingDamage = 0;
            ToolHelper.damageTool(stack, dmg, (EntityPlayer) entity);
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase user, int count) {
        if (!user.world.isRemote) {
            SoulgeHeartStats stats = getSoulgeHeartStats(stack);
            EntityLivingBase target = findPointedEntity(user.world, user, stats.detectionRange);
            if (target != null && target.isEntityAlive()) {
                addTargetMark(target, stats);
            }
        }
    }

    private static final String TAG_PENDING_DAMAGE = "soulge_dmg";

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (!isSelected || world.isRemote || !(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;

        if (!player.isHandActive()) {
            return;
        }
        SoulgeHeartStats stats = getSoulgeHeartStats(stack);
        if (player.ticksExisted % stats.attackInterval == 0) {
            attackMarkedTargets(stack, player, stats);
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
            if (target == player || !(target instanceof IMob)) {
                continue;
            }
            NBTTagCompound data = target.getEntityData();
            int marks = data.getInteger(TAG_TARGETED);
            if (marks <= 0 || data.hasKey(TAG_READY_TO_DIE)) {
                continue;
            }
            float newHealth = target.getHealth() - damage;
            if (newHealth <= 0) {
                target.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
            } else {
                target.setHealth(newHealth);
                target.hurtTime = 10;
                target.hurtResistantTime = target.maxHurtResistantTime;
            }
            drawParticleBeam(player, target);
            for (int p = 0; p < 5; p++) {
                target.world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                        target.posX + (rand.nextDouble() - 0.5D) * target.width,
                        target.posY + target.height * rand.nextDouble(),
                        target.posZ + (rand.nextDouble() - 0.5D) * target.width,
                        0.2D, 0.6D, 1.0D, new int[0]);
            }
            pendingDamage++;
            data.setInteger(TAG_TARGETED, marks - 1);
            if (target.getHealth() <= target.getMaxHealth() * stats.executeThreshold && target.isEntityAlive()) {
                executeTarget(target);
            }
            if (!target.isEntityAlive()) {
                data.removeTag(TAG_TARGETED);
            }
            attacked++;
        }
    }

    private void executeTarget(EntityLivingBase target) {
        NBTTagCompound data = target.getEntityData();
        if (data.hasKey(TAG_READY_TO_DIE)) {
            return;
        }
        target.clearActivePotions();
        data.setInteger(TAG_READY_TO_DIE, 9);
        for (int p = 0; p < 15; p++) {
            target.world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    target.posX + (rand.nextDouble() - 0.5D) * target.width * 2,
                    target.posY + target.height * rand.nextDouble(),
                    target.posZ + (rand.nextDouble() - 0.5D) * target.width * 2,
                    1.0D, 0.3D, 0.3D, new int[0]);
        }
    }

    private void drawParticleBeam(EntityLivingBase from, EntityLivingBase to) {
        double dx = to.posX - from.posX;
        double dy = (to.posY + to.height * 0.5D) - (from.posY + from.height * 0.5D);
        double dz = to.posZ - from.posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist == 0) {
            return;
        }
        dx /= dist;
        dy /= dist;
        dz /= dist;

        double traveled = 0;
        while (traveled < dist) {
            traveled += rand.nextDouble();
            double x = from.posX + dx * traveled;
            double y = from.posY + from.height * 0.5D + dy * traveled;
            double z = from.posZ + dz * traveled;
            from.world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y, z, 0.2D, 0.6D, 1.0D, new int[0]);
        }
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
            case "obsidian":
            case "cobalt":
            case "manyullyn":
                return true;
            default:
                return false;
        }
    }

    static SoulgeHeartStats getSoulgeHeartStats(String material) {
        switch (material) {
            case "obsidian":
                return new SoulgeHeartStats(20.0F, 8, 6, 0.16F);
            case "cobalt":
                return new SoulgeHeartStats(18.0F, 30, 10, 0.2F);
            case "manyullyn":
                return new SoulgeHeartStats(30.0F, 40, 5, 0.27F);
            // 以下材料数值保留，后续启用时取消注释
            // case "elysia":      return new SoulgeHeartStats(36.0F, 36, 4, 0.36F);
            // case "aetherium":    return new SoulgeHeartStats(80.0F, 60, 3, 0.36F);
            // case "heart_of_steel": return new SoulgeHeartStats(35.0F, 60, 10, 0.0F);
            // case "true_infinity":  return new SoulgeHeartStats(100.0F, 100, 1, 0.9F);
            // case "violium":     return new SoulgeHeartStats(45.0F, 45, 4, 0.28F);
            // case "exotic_matter": return new SoulgeHeartStats(60.0F, 60, 4, 0.45F);
            // case "exo_alloy":   return new SoulgeHeartStats(100.0F, 100, 1, 0.72F);
            // case "ultra_dense": return new SoulgeHeartStats(70.0F, 100, 10, 0.38F);
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
