package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.SpaceDissectorItem;
import it.hurts.sskirillss.relics.particles.spark.SparkTintData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.awt.*;

public class SpaceDissectorEntity extends ThrowableEntity {
    private static final DataParameter<Integer> UPDATE_TIME = EntityDataManager.defineId(SpaceDissectorEntity.class, DataSerializers.INT);
    public static final DataParameter<Boolean> IS_RETURNING = EntityDataManager.defineId(SpaceDissectorEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> BOUNCES = EntityDataManager.defineId(SpaceDissectorEntity.class, DataSerializers.INT);
    private static final DataParameter<String> OWNER = EntityDataManager.defineId(SpaceDissectorEntity.class, DataSerializers.STRING);

    private static final String TAG_UPDATE_TIME = "time";
    private static final String TAG_IS_RETURNING = "returning";
    private static final String TAG_BOUNCES_AMOUNT = "bounces";
    private static final String TAG_OWNER_UUID = "owner";

    private static boolean bounced = false;
    public ItemStack stack = ItemStack.EMPTY;
    private PlayerEntity owner;
    private static int time;

    public SpaceDissectorEntity(EntityType<? extends SpaceDissectorEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public SpaceDissectorEntity(World world, LivingEntity throwerIn) {
        super(EntityRegistry.SPACE_DISSECTOR.get(), throwerIn, world);
    }

    public void setOwner(PlayerEntity playerIn) {
        this.owner = playerIn;

        if (playerIn != null)
            entityData.set(OWNER, playerIn.getUUID().toString());
    }

    @Override
    public void tick() {
        Vector3d defaultMotion = getDeltaMovement();

        super.tick();

        SpaceDissectorItem.Stats config = SpaceDissectorItem.INSTANCE.getConfig();

        for (int i = 0; i < 3; i++)
            level.addParticle(new SparkTintData(new Color(255 - random.nextInt(100), 0, 255 - random.nextInt(100)), 0.2F, 30),
                    this.xo, this.yo, this.zo, MathUtils.randomFloat(random) * 0.01F, 0, MathUtils.randomFloat(random) * 0.01F);

        if (!level.isClientSide() && this.tickCount % 20 == 0) {
            if (entityData.get(UPDATE_TIME) > config.maxThrownTime) {
                if (owner != null && stack != null && !stack.isEmpty())
                    NBTUtils.setBoolean(stack, SpaceDissectorItem.TAG_IS_THROWN, false);

                this.remove();
            }

            if (!entityData.get(IS_RETURNING)) {
                if (entityData.get(UPDATE_TIME) < config.timeBeforeReturn)
                    entityData.set(UPDATE_TIME, entityData.get(UPDATE_TIME) + 1);
                else
                    entityData.set(IS_RETURNING, true);
            }
        }

        if (!entityData.get(IS_RETURNING)) {
            if (!bounced)
                setDeltaMovement(defaultMotion);
        } else if (!level.isClientSide()){
            if (owner != null) {
                EntityUtils.moveTowardsPosition(this, new Vector3d(owner.getX(),
                        owner.getY() + 1.0F, owner.getZ()), config.projectileSpeed);

                for (PlayerEntity player : level.getEntitiesOfClass(PlayerEntity.class, this.getBoundingBox().inflate(2.0F))) {
                    if (owner.getUUID().equals(player.getUUID()) && stack != null && !stack.isEmpty()) {
                        this.remove();

                        NBTUtils.setBoolean(stack, SpaceDissectorItem.TAG_IS_THROWN, false);
                    }
                }
            } else {
                if (stack != null && stack != ItemStack.EMPTY)
                    NBTUtils.setBoolean(stack, SpaceDissectorItem.TAG_IS_THROWN, false);
                this.remove();
            }
        }

        if (this.isOnFire())
            this.clearFire();

        time++;
        bounced = false;
    }

    @Override
    protected void onHit(@Nonnull RayTraceResult rayTraceResult) {
        SpaceDissectorItem.Stats config = SpaceDissectorItem.INSTANCE.getConfig();

        switch (rayTraceResult.getType()) {
            case BLOCK: {
                BlockRayTraceResult result = (BlockRayTraceResult) rayTraceResult;

                if (!level.getBlockState(result.getBlockPos()).getMaterial().blocksMotion())
                    return;

                if (entityData.get(BOUNCES) < config.maxBounces) {
                    if (entityData.get(IS_RETURNING))
                        return;

                    Direction dir = result.getDirection();
                    Vector3d normalVector = new Vector3d(-2 * dir.getStepX(), -2 * dir.getStepY(), -2 * dir.getStepZ()).normalize();
                    double delta = -1.91 * this.getDeltaMovement().dot(normalVector);

                    setDeltaMovement(normalVector.multiply(delta, delta, delta).add(this.getDeltaMovement()));

                    for (int i = 0; i < 20; i++)
                        level.addParticle(new SparkTintData(new Color(255, 255 - random.nextInt(50), 0), 0.15F,
                                        random.nextInt(7) + 1), this.getX() + MathUtils.randomFloat(random) * 0.25F,
                                this.getY() + 0.1F, this.getZ() + MathUtils.randomFloat(random) * 0.25F,
                                this.getDeltaMovement().x * 1.25F + MathUtils.randomFloat(random) * 0.35F, this.getDeltaMovement().y,
                                this.getDeltaMovement().z * 1.25F + MathUtils.randomFloat(random) * 0.35F);

                    if (time > 2) {
                        level.playSound(null, getX(), getY(), getZ(), SoundRegistry.RICOCHET, SoundCategory.MASTER,
                                0.5F, 0.75F + (random.nextFloat() * 0.5F));

                        time = 0;
                    }

                    bounced = true;

                    entityData.set(BOUNCES, entityData.get(BOUNCES) + 1);
                    entityData.set(UPDATE_TIME, Math.max(entityData.get(UPDATE_TIME) - config.additionalTimeAfterBounce, 0));
                } else
                    entityData.set(IS_RETURNING, true);

                break;
            }
            case ENTITY: {
                EntityRayTraceResult result = (EntityRayTraceResult) rayTraceResult;

                if (!(result.getEntity() instanceof LivingEntity))
                    return;

                LivingEntity entity = (LivingEntity) result.getEntity();

                if (owner != null && owner.getUUID().equals(entity.getUUID())) {
                    if (stack != null && stack != ItemStack.EMPTY)
                        NBTUtils.setBoolean(stack, SpaceDissectorItem.TAG_IS_THROWN, false);

                    this.remove();
                } else
                    entity.hurt(owner != null ? DamageSource.playerAttack(owner) : DamageSource.GENERIC,
                            config.baseDamage + (entityData.get(BOUNCES) * config.damageMultiplierPerBounce));

                break;
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(UPDATE_TIME, 0);
        entityData.define(IS_RETURNING, false);
        entityData.define(BOUNCES, 0);
        entityData.define(OWNER, "");
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        tag.putInt(TAG_UPDATE_TIME, entityData.get(UPDATE_TIME));
        tag.putString(TAG_OWNER_UUID, entityData.get(OWNER));
        tag.putBoolean(TAG_IS_RETURNING, entityData.get(IS_RETURNING));
        tag.putInt(TAG_BOUNCES_AMOUNT, entityData.get(BOUNCES));

        stack.save(tag);

        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        entityData.set(UPDATE_TIME, tag.getInt(TAG_UPDATE_TIME));
        entityData.set(OWNER, tag.getString(TAG_OWNER_UUID));
        entityData.set(IS_RETURNING, tag.getBoolean(TAG_IS_RETURNING));
        entityData.set(BOUNCES, tag.getInt(TAG_BOUNCES_AMOUNT));

        stack = ItemStack.of(tag);

        super.readAdditionalSaveData(tag);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected float getGravity() {
        return 0;
    }

    @Nonnull
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}