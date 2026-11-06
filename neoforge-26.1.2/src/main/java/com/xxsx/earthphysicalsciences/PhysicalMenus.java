package com.xxsx.earthphysicalsciences;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PhysicalMenus {
    private PhysicalMenus() {}

    public static class PhysicalMachineMenu extends AbstractContainerMenu {
        private final Container container;
        private final ContainerData data;

        public PhysicalMachineMenu(int containerId, Inventory playerInventory) {
            this(containerId, playerInventory, new SimpleContainer(6), new SimpleContainerData(4));
        }

        public PhysicalMachineMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
            super(EarthPhysicalSciences.PHYSICAL_MACHINE_MENU.get(), containerId);
            this.container = container;
            this.data = data;

            for (int i = 0; i < 6; i++) {
                this.addSlot(new Slot(container, i, 26 + i * 22, 35));
            }

            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
                }
            }
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
            }

            this.addDataSlots(data);
        }

        public int getProgress() { return data.get(0); }
        public int getMaxProgress() { return Math.max(1, data.get(1)); }
        public int getEnergy() { return data.get(2); }
        public int getMaxEnergy() { return Math.max(1, data.get(3)); }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            ItemStack itemstack = ItemStack.EMPTY;
            Slot slot = this.slots.get(index);
            if (slot != null && slot.hasItem()) {
                ItemStack stackInSlot = slot.getItem();
                itemstack = stackInSlot.copy();
                if (index < 6) {
                    if (!this.moveItemStackTo(stackInSlot, 6, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stackInSlot, 0, 6, false)) {
                    return ItemStack.EMPTY;
                }
                if (stackInSlot.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
            }
            return itemstack;
        }

        @Override
        public boolean stillValid(Player player) {
            return container.stillValid(player);
        }
    }
}
