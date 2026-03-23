package com.muguett.moonforge.entity;

import com.muguett.moonforge.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ArsenalSpellEntity extends ThrownItemEntity {
    public ArsenalSpellEntity(EntityType<? extends ArsenalSpellEntity> entityType, World world) {
        super(entityType, world);
    }

    public ArsenalSpellEntity(EntityType<? extends ArsenalSpellEntity> entityType, World world, LivingEntity owner, ItemStack stack) {
        super(entityType, owner, world, stack.copy());
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SOLARIS_STAFF;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            spawnTrail(serverWorld);
        }
        this.setYaw((float) Math.toDegrees(Math.atan2(this.getVelocity().x, this.getVelocity().z)));
        this.setPitch((float) Math.toDegrees(Math.atan2(this.getVelocity().y, Math.sqrt(this.getVelocity().x * this.getVelocity().x + this.getVelocity().z * this.getVelocity().z))));
        if (this.age > 80) {
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        resolveImpact(entityHitResult.getPos(), entityHitResult.getEntity() instanceof LivingEntity living ? living : null);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        resolveImpact(blockHitResult.getPos(), null);
    }

    private void resolveImpact(Vec3d impact, LivingEntity directTarget) {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld) || !(this.getOwner() instanceof PlayerEntity player)) {
            this.discard();
            return;
        }

        Item item = this.getStack().getItem();
        if (item == ModItems.SOLARIS_STAFF) {
            serverWorld.createExplosion(this, impact.x, impact.y, impact.z, 1.5F, true, World.ExplosionSourceType.NONE);
            affectArea(serverWorld, player, impact, 3.5D, 8.0F, entity -> entity.setOnFireFor(4));
            burst(serverWorld, impact, ParticleTypes.FLAME, 24);
            play(serverWorld, SoundEvents.ITEM_FIRECHARGE_USE);
        } else if (item == ModItems.FROSTBITE_SCEPTER) {
            affectArea(serverWorld, player, impact, 2.5D, 6.0F, entity -> {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 0));
            });
            burst(serverWorld, impact, ParticleTypes.SNOWFLAKE, 26);
            play(serverWorld, SoundEvents.BLOCK_GLASS_BREAK);
        } else if (item == ModItems.THUNDERCHAIN_BATON) {
            if (directTarget != null) {
                directTarget.damage(serverWorld, player.getDamageSources().playerAttack(player), 14.0F);
            }
            affectArea(serverWorld, player, impact, 4.0D, 6.0F, entity -> { });
            burst(serverWorld, impact, ParticleTypes.ELECTRIC_SPARK, 30);
            play(serverWorld, SoundEvents.ITEM_TRIDENT_THUNDER.value());
        } else if (item == ModItems.GRAVITON_MAUL) {
            Box box = new Box(impact, impact).expand(5.0D);
            serverWorld.getOtherEntities(player, box, entity -> entity instanceof LivingEntity).forEach(entity -> {
                Vec3d pull = impact.subtract(new Vec3d(entity.getX(), entity.getY(), entity.getZ())).normalize().multiply(0.8D);
                entity.addVelocity(pull.x, 0.4D, pull.z);
                entity.damage(serverWorld, player.getDamageSources().playerAttack(player), 8.0F);
            });
            burst(serverWorld, impact, ParticleTypes.PORTAL, 28);
            play(serverWorld, SoundEvents.ITEM_MACE_SMASH_AIR);
        } else if (item == ModItems.BLOOMLASH_WHIP) {
            affectArea(serverWorld, player, impact, 3.0D, 4.0F, entity -> {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 0));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1));
            });
            burst(serverWorld, impact, ParticleTypes.SPORE_BLOSSOM_AIR, 24);
            play(serverWorld, SoundEvents.BLOCK_VINE_STEP);
        } else if (item == ModItems.RIFTSTEP_DAGGER) {
            player.teleport(impact.x, impact.y, impact.z, false);
            affectArea(serverWorld, player, impact, 3.0D, 8.0F, entity -> entity.addVelocity(0.0D, 0.25D, 0.0D));
            burst(serverWorld, impact, ParticleTypes.REVERSE_PORTAL, 24);
            play(serverWorld, SoundEvents.ENTITY_ENDERMAN_TELEPORT);
        } else if (item == ModItems.ECHO_DISC_LAUNCHER) {
            affectArea(serverWorld, player, impact, 2.5D, 10.0F, entity -> entity.addVelocity(this.getVelocity().x * 0.6D, 0.15D, this.getVelocity().z * 0.6D));
            burst(serverWorld, impact, ParticleTypes.SCULK_SOUL, 24);
            play(serverWorld, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK);
        } else if (item == ModItems.STARFALL_TOME) {
            serverWorld.createExplosion(this, impact.x, impact.y + 1.0D, impact.z, 3.0F, true, World.ExplosionSourceType.NONE);
            affectArea(serverWorld, player, impact, 5.0D, 16.0F, entity -> entity.setOnFireFor(6));
            burst(serverWorld, impact, ParticleTypes.END_ROD, 40);
            play(serverWorld, SoundEvents.ITEM_TRIDENT_THUNDER.value());
        }

        this.discard();
    }

    private void spawnTrail(ServerWorld world) {
        Item item = this.getStack().getItem();
        if (item == ModItems.SOLARIS_STAFF) {
            world.spawnParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 2, 0.02D, 0.02D, 0.02D, 0.001D);
        } else if (item == ModItems.FROSTBITE_SCEPTER) {
            world.spawnParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 2, 0.04D, 0.04D, 0.04D, 0.001D);
        } else if (item == ModItems.THUNDERCHAIN_BATON) {
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 3, 0.06D, 0.06D, 0.06D, 0.001D);
        } else if (item == ModItems.GRAVITON_MAUL) {
            world.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 3, 0.08D, 0.08D, 0.08D, 0.01D);
        } else if (item == ModItems.BLOOMLASH_WHIP) {
            world.spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(), this.getZ(), 3, 0.05D, 0.05D, 0.05D, 0.001D);
        } else if (item == ModItems.RIFTSTEP_DAGGER) {
            world.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(), this.getZ(), 3, 0.05D, 0.05D, 0.05D, 0.01D);
        } else if (item == ModItems.ECHO_DISC_LAUNCHER) {
            world.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(), this.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.01D);
        } else if (item == ModItems.STARFALL_TOME) {
            world.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 4, 0.05D, 0.05D, 0.05D, 0.02D);
        }
    }

    private void affectArea(ServerWorld world, PlayerEntity player, Vec3d center, double radius, float damage, java.util.function.Consumer<LivingEntity> extra) {
        Box box = new Box(center, center).expand(radius);
        world.getOtherEntities(player, box, entity -> entity instanceof LivingEntity).forEach(entity -> {
            LivingEntity living = (LivingEntity) entity;
            if (new Vec3d(living.getX(), living.getY(), living.getZ()).distanceTo(center) <= radius) {
                living.damage(world, player.getDamageSources().playerAttack(player), damage);
                extra.accept(living);
            }
        });
    }

    private void burst(ServerWorld world, Vec3d center, net.minecraft.particle.ParticleEffect particle, int count) {
        world.spawnParticles(particle, center.x, center.y, center.z, count, 0.35D, 0.35D, 0.35D, 0.02D);
    }

    private void play(ServerWorld world, SoundEvent sound) {
        world.playSound(null, this.getX(), this.getY(), this.getZ(), sound, net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
    }
}
