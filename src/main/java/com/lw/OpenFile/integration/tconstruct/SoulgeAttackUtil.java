package com.lw.OpenFile.integration.tconstruct;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.lang.reflect.Field;

public final class SoulgeAttackUtil {

    private static Field attackingPlayerField;
    private static Field lastAttackerField;
    private static Field lastDamageField;

    static {
        try {
            attackingPlayerField = EntityLivingBase.class.getDeclaredField("attackingPlayer");
            lastAttackerField = EntityLivingBase.class.getDeclaredField("lastAttacker");
            lastDamageField = EntityLivingBase.class.getDeclaredField("lastDamage");
        } catch (Exception e) {
            try {
                attackingPlayerField = EntityLivingBase.class.getDeclaredField("field_70717_bb");
                lastAttackerField = EntityLivingBase.class.getDeclaredField("field_190535_bF");
                lastDamageField = EntityLivingBase.class.getDeclaredField("field_110153_bc");
            } catch (Exception ignored) {
            }
        }
        if (attackingPlayerField != null) attackingPlayerField.setAccessible(true);
        if (lastAttackerField != null) lastAttackerField.setAccessible(true);
        if (lastDamageField != null) lastDamageField.setAccessible(true);
    }

    /**
     * 完全独立于原版战斗系统的攻击方法，不触发挥臂动画。
     * 参考 1.19 TCon ToolAttackUtil.attackEntity 的设计。
     */
    public static boolean attackEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, float rawDamage) {
        if (target.world.isRemote) {
            return true;
        }
        if (target.hurtResistantTime > target.maxHurtResistantTime / 2.0F) {
            // 还在受伤无敌帧内，跳过
            return false;
        }

        // 1. 计算最终伤害（模拟 LivingHurtEvent 处理链）
        DamageSource source = DamageSource.causePlayerDamage(player);
        float armor = target.getTotalArmorValue();
        float toughness = (float) target.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue();
        float damage = CombatRules.getDamageAfterAbsorb(rawDamage, armor, toughness);
        damage = Math.max(damage - target.getAbsorptionAmount(), 0);

        // 2. Forge LivingHurtEvent
        LivingHurtEvent hurtEvent = new LivingHurtEvent(target, source, damage);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(hurtEvent)) {
            return false;
        }
        damage = hurtEvent.getAmount();

        // 3. 应用伤害
        float currentHealth = target.getHealth();
        float newHealth = currentHealth - damage;

        // 受击动画 + 无敌帧（hurtTime/maxHurtResistantTime 在 1.12 是 public）
        target.hurtTime = 10;
        target.hurtResistantTime = target.maxHurtResistantTime;

        if (newHealth <= 0) {
            // 击杀：手动标记死亡归属，触发 onDeath
            target.getCombatTracker().trackDamage(source, currentHealth, damage);
            try {
                if (attackingPlayerField != null) attackingPlayerField.set(target, player);
                if (lastAttackerField != null) lastAttackerField.set(target, player);
                if (lastDamageField != null) lastDamageField.setFloat(target, damage);
            } catch (Exception ignored) {
            }
            target.setHealth(0);
            target.onDeath(source);
        } else {
            target.getCombatTracker().trackDamage(source, damage, damage);
            try {
                if (attackingPlayerField != null) attackingPlayerField.set(target, player);
                if (lastAttackerField != null) lastAttackerField.set(target, player);
                if (lastDamageField != null) lastDamageField.setFloat(target, damage);
            } catch (Exception ignored) {
            }
            target.setHealth(newHealth);
        }

        // 4. Forge LivingDamageEvent
        LivingDamageEvent damageEvent = new LivingDamageEvent(target, source, damage);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(damageEvent);

        // 5. 扣工具耐久（不通过原版战斗系统，不会有动画副作用）
        ToolHelper.damageTool(stack, 1, player);

        return true;
    }
}
