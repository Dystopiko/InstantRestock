package xyz.memothelemo.instantrestock.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.memothelemo.instantrestock.ModRunner;
import xyz.memothelemo.instantrestock.interfaces.IRMerchantOffer;

@Mixin(Villager.class)
public abstract class VillagerEntityMixin extends AbstractVillager {
    @Unique private boolean hasInstantRestockEffect = false;

    public VillagerEntityMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private void enableInstantRestock() {
        for (MerchantOffer offer : this.getOffers()) {
            ((IRMerchantOffer) offer).ir$applyIREffect();
        }
    }

    @Unique
    private void disableInstantRestock() {
        for (MerchantOffer offer : this.getOffers()) {
            ((IRMerchantOffer) offer).ir$revertIREffect();
        }
    }

    @Inject(method = "onReputationEventFrom", at = @At("HEAD"), cancellable = true)
    private void cancelGossipReward(ReputationEventType type, Entity target, CallbackInfo ci) {
        if (this.hasInstantRestockEffect && type == ReputationEventType.TRADE && target == this.getTradingPlayer()) {
            ci.cancel();
        }
    }

    @Inject(method = "getPlayerReputation", at = @At("HEAD"), cancellable = true)
    private void getPlayerReputation(Player player, CallbackInfoReturnable<Integer> cir) {
        // Return reputation to 0 if instant restock effect has applied
        //
        // So we don't have to get insane low prices if the player for
        // example traded with the same villager dozens of times.
        if (this.hasInstantRestockEffect && this.getTradingPlayer() == player) {
            cir.setReturnValue(0);
        }
    }

    // It is not possible to start trading for a different plr
    // if the plr has already trading with the same villager.
    @Inject(method = "startTrading", at = @At("HEAD"))
    private void onStartedTrading(Player player, CallbackInfo ci) {
        this.hasInstantRestockEffect = ModRunner.canInstantlyRestock(player);
        if (this.hasInstantRestockEffect) {
            this.enableInstantRestock();
        }
    }

    @Inject(method = "stopTrading", at = @At("HEAD"))
    private void onStoppedTrading(CallbackInfo ci) {
        this.hasInstantRestockEffect = false;
        this.disableInstantRestock();
    }
}
