package net.glowstone.entity.objects;

import com.flowpowered.network.Message;
import net.glowstone.EventFactory;
import net.glowstone.chunk.GlowChunk;
import net.glowstone.chunk.GlowChunk.Key;
import net.glowstone.entity.GlowHangingEntity;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.entity.meta.MetadataIndex;
import net.glowstone.net.message.play.entity.EntityMetadataMessage;
import net.glowstone.net.message.play.entity.EntityTeleportMessage;
import net.glowstone.net.message.play.entity.SpawnObjectMessage;
import net.glowstone.net.message.play.player.InteractEntityMessage;
import net.glowstone.net.message.play.player.InteractEntityMessage.Action;
import net.glowstone.util.InventoryUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;


public class GlowItemFrame extends GlowHangingEntity implements ItemFrame {

    public GlowItemFrame(GlowPlayer player, Location location, BlockFace facing) {

        super(location, facing);
        if (player != null) { // could be Anvil loading....
            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack is = player.getItemInHand();
                int amount = is.getAmount();
                is.setAmount(amount - 1);
                if (is.getAmount() <= 0) {
                    is = null;
                }
                player.setItemInHand(is);
            }
        }

        setRotation(Rotation.NONE);
        setItem(InventoryUtil.createEmptyStack());
    }

    // //////////////////////////////////////////////////////////////////////////
    // Overrides

    @Override
    public boolean entityInteract(GlowPlayer player, InteractEntityMessage message) {
        if (message.getAction() == Action.INTERACT.ordinal() && message.getHandSlot() == EquipmentSlot.HAND) {
            if (InventoryUtil.isEmpty(getItem())) {
                ItemStack isInHand = player.getItemInHand();
                if (isInHand != null) {
                    setItem(isInHand);
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        int amount = player.getItemInHand().getAmount();
                        isInHand.setAmount(amount - 1);
                        if (isInHand.getAmount() <= 0) {
                            isInHand = null;
                        }
                        player.setItemInHand(isInHand);
                    }
                }
            } else {
                int rot = (getRotation().ordinal() + 1) % 8;
                setRotation(Rotation.values()[rot]);
            }
        }
        if (message.getAction() == Action.ATTACK.ordinal()) {
            if (isEmpty()) {
                // TODO: use correct RemoveCause
                if (EventFactory.callEvent(new HangingBreakEvent(this, RemoveCause.DEFAULT)).isCancelled()) {
                    return false;
                }
                remove();
            } else {
                world.dropItemNaturally(location, getItem().clone());
                setItem(InventoryUtil.createEmptyStack());
                setRotation(Rotation.NONE);
            }
        }
        return true;
    }

    @Override
    public void pulse() {
        super.pulse();

        if (ticksLived % (20 * 5) == 0) {

            if (location.getBlock().getRelative(getAttachedFace()).getType() == Material.AIR) {
                if (EventFactory.callEvent(new HangingBreakEvent(this, RemoveCause.PHYSICS)).isCancelled()) {
                    return;
                }
                world.dropItemNaturally(location, new ItemStack(Material.ITEM_FRAME));
                if (!isEmpty()) {
                    world.dropItemNaturally(location, getItem().clone());
                }
                remove();
            }
        }
    }

    @Override
    protected void pulsePhysics() {
        // item frames aren't affected by physics
    }

    @Override
    public List<Message> createSpawnMessage() {
        int yaw = getYaw();

        return Arrays.asList(
            new SpawnObjectMessage(id, getUniqueId(), SpawnObjectMessage.ITEM_FRAME, location.getBlockX(), location.getBlockY(), location.getBlockZ(), 0, yaw, HangingFace.getByBlockFace(getFacing()).ordinal()),
            new EntityMetadataMessage(id, metadata.getEntryList())
        );
    }

    @Override
    public boolean isEmpty() {
        return InventoryUtil.isEmpty(getItem());
    }

    private void createTeleportMessage(BlockFace face) {
        int xoffset = 0;
        int zoffset = 0;
        int yaw = getYaw();
        switch (face) {
            case WEST:
                xoffset = -32;
                break;
            case NORTH:
                zoffset = -32;
                break;
            case EAST:
                xoffset = 32;
                break;
            case SOUTH:
                zoffset = 32;
                break;
        }

        Key key = GlowChunk.ChunkKeyStore.get(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        for (GlowPlayer player : getWorld().getRawPlayers()) {
            if (player.canSeeChunk(key)) {
                double x = location.getX();
                double y = location.getY();
                double z = location.getZ();
                player.getSession().send(new EntityTeleportMessage(id, x + xoffset, y, z + zoffset, yaw, 0));
            }
        }
    }


    @Override
    public EntityType getType() {
        return EntityType.ITEM_FRAME;
    }

    @Override
    public Location getOrigin() {
        return null;
    }

    @Override
    public ItemStack getItem() {
        return metadata.getItem(MetadataIndex.ITEM_FRAME_ITEM);
    }

    @Override
    public boolean setFacingDirection(BlockFace blockface, boolean force) {
        facing = HangingFace.getByBlockFace(blockface);
        createTeleportMessage(blockface);
        return true;
    }

    @Override
    public void setFacingDirection(BlockFace blockface) {
        facing = HangingFace.getByBlockFace(blockface);
        createTeleportMessage(blockface);
    }

    @Override
    public void setItem(ItemStack is) {
        is = InventoryUtil.itemOrEmpty(is).clone();
        is.setAmount(1);

        metadata.set(MetadataIndex.ITEM_FRAME_ITEM, is);
        setRotation(Rotation.NONE);
    }

    @Override
    public Rotation getRotation() {
        int rot = metadata.getInt(MetadataIndex.ITEM_FRAME_ROTATION);

        if (rot < Rotation.values().length) {
            return Rotation.values()[rot];
        }

        return Rotation.NONE;
    }

    @Override
    public void setRotation(Rotation rotation) {
        metadata.set(MetadataIndex.ITEM_FRAME_ROTATION, rotation.ordinal());
    }
}
