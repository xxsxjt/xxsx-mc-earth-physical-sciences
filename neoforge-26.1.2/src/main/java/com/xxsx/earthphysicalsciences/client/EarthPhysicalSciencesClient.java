package com.xxsx.earthphysicalsciences.client;

import com.xxsx.earthphysicalsciences.EarthPhysicalSciences;
import com.xxsx.earthphysicalsciences.PhysicalMenus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class EarthPhysicalSciencesClient {
    private EarthPhysicalSciencesClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(EarthPhysicalSciencesClient::onRegisterScreens);
        modBus.addListener(EarthPhysicalSciencesClient::onRegisterRenderers);
    }

    private static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(EarthPhysicalSciences.PHYSICAL_MACHINE_MENU.get(), PhysicalMachineScreen::new);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EarthPhysicalSciences.LAUNCH_VEHICLE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EarthPhysicalSciences.ANDROID_WORKER.get(), NoopRenderer::new);
    }

    public static class PhysicalMachineScreen extends AbstractContainerScreen<PhysicalMenus.PhysicalMachineMenu> {
        public PhysicalMachineScreen(PhysicalMenus.PhysicalMachineMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            super.extractRenderState(graphics, mouseX, mouseY, partialTick);
            this.extractTooltip(graphics, mouseX, mouseY);
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            super.extractBackground(graphics, mouseX, mouseY, partialTick);
            int x = this.leftPos;
            int y = this.topPos;
            graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFDFE6EC);
            graphics.fill(x + 4, y + 18, x + this.imageWidth - 4, y + 76, 0xFF1F2D3A);
            int prog = menu.getProgress();
            int maxProg = menu.getMaxProgress();
            int barW = Math.min(120, (prog * 120) / maxProg);
            graphics.fill(x + 28, y + 60, x + 28 + 120, y + 66, 0xFF334455);
            graphics.fill(x + 28, y + 60, x + 28 + barW, y + 66, 0xFF00D4FF);
        }
    }
}
