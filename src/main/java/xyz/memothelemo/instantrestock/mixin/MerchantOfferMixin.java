package xyz.memothelemo.instantrestock.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.memothelemo.instantrestock.ModRunner;
import xyz.memothelemo.instantrestock.interfaces.IRMerchantOffer;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferMixin implements IRMerchantOffer {
    @Shadow
    @Final
    private float priceMultiplier;

    @Shadow
    public abstract int getDemand();

    @Shadow
    public abstract float getPriceMultiplier();

    @Shadow
    public abstract int getSpecialPriceDiff();

    @Unique private boolean appliedIREffect = false;

    // This injected method is needed because `out of stock` byte
    // is included in the MerchantOffer codec.
    @Inject(method = "isOutOfStock", at = @At("HEAD"), cancellable = true)
    public void overrideIsOutOfStock(CallbackInfoReturnable<Boolean> cir) {
        if (this.appliedIREffect) cir.setReturnValue(false);
    }

    @Inject(method = "getModifiedCostCount", at = @At("HEAD"), cancellable = true)
    private void getModifiedCostCount(ItemCost itemCost, CallbackInfoReturnable<Integer> cir) {
        int i = itemCost.count();
        int j = Math.max(0, Mth.floor((float)(i * this.getDemand()) * this.getPriceMultiplier()));
        int count = Mth.clamp(i + j + this.getSpecialPriceDiff(), 1, itemCost.itemStack().getMaxStackSize());
        cir.setReturnValue(count);
    }

    // This injected method is needed to disable special price difference
    //
    // ik it's unfair
    @Inject(method = "getPriceMultiplier", at = @At("HEAD"), cancellable = true)
    public void overridePriceMultiplier(CallbackInfoReturnable<Float> cir) {
        // f(x) = 1.0468 * x^1.27247; x = current price multiplier
        if (this.appliedIREffect) {
            double result = 1.0468f * Math.pow(this.priceMultiplier, 1.27247);
            cir.setReturnValue((float) result);
        }
    }

    @Inject(method = "getMaxUses", at = @At("HEAD"), cancellable = true)
    public void overrideMaxUses(CallbackInfoReturnable<Integer> cir) {
        if (this.appliedIREffect) cir.setReturnValue(Integer.MAX_VALUE);
    }

    @Inject(method = "getUses", at = @At("HEAD"), cancellable = true)
    public void overrideUses(CallbackInfoReturnable<Integer> cir) {
        if (this.appliedIREffect) cir.setReturnValue(0);
    }

    @Inject(method = "needsRestock", at = @At("HEAD"), cancellable = true)
    public void overrideNeedsRestock(CallbackInfoReturnable<Boolean> cir) {
        if (this.appliedIREffect) cir.setReturnValue(false);
    }

    @Inject(method = "increaseUses", at = @At("HEAD"), cancellable = true)
    public void cancelIncreaseUses(CallbackInfo ci) {
        if (this.appliedIREffect) ci.cancel();
    }

    @Inject(method = "copy", at = @At("TAIL"))
    public void copyAppliedIREffect(CallbackInfoReturnable<MerchantOffer> cir) {
        MerchantOfferMixin offer = (MerchantOfferMixin) (IRMerchantOffer) cir.getReturnValue();
        assert offer != null;
        offer.appliedIREffect = this.appliedIREffect;
    }

    @Override
    public void ir$applyIREffect() {
        this.appliedIREffect = true;
    }

    @Override
    public void ir$revertIREffect() {
        this.appliedIREffect = false;
    }
}
