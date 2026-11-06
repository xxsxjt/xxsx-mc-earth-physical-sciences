package com.xxsx.earthphysicalsciences.mixin;

import com.xxsx.earthphysicalsciences.PhysicalEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class PhysicalVillagerMixin {
    @Inject(method = "updateTrades", at = @At("HEAD"))
    private void earthPhysicalSciences$addProfessionTrades(ServerLevel level, CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        PhysicalEvents.addProfessionTrades(self, self.getVillagerData().level());
    }
}

