
package com.optimise10sky.optimise10sky.mixins.megacells;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;

import gripe._90.megacells.misc.CompressionChain;

@Mixin(CompressionChain.class)
public abstract class CompressionChainMixin {

    @Unique
    private Set<AEItemKey> optimise10sky$variantKeys;

    @Unique
    private Map<AEItemKey, BigInteger> optimise10sky$factors;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void optimise10sky$init(
        List<ItemStack> variants,
        CallbackInfo ci
    ) {
        var keys = new ObjectOpenHashSet<AEItemKey>(variants.size());
        var factors = new Object2ObjectOpenHashMap<AEItemKey, BigInteger>(variants.size());

        var factor = BigInteger.ONE;

        for (var variant : variants) {
            factor = factor.multiply(BigInteger.valueOf(variant.getCount()));

            var key = AEItemKey.of(variant);

            if (key != null && keys.add(key)) {
                factors.put(key, factor);
            }
        }

        this.optimise10sky$variantKeys = keys;
        this.optimise10sky$factors = factors;
    }

    @Inject(
        method = "containsVariant",
        at = @At("HEAD"),
        cancellable = true
    )
    private void optimise10sky$containsVariant(
        AEItemKey item,
        CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(this.optimise10sky$variantKeys.contains(item));
    }

    @Inject(
        method = "unitFactor",
        at = @At("HEAD"),
        cancellable = true
    )
    private void optimise10sky$unitFactor(
        AEItemKey item,
        CallbackInfoReturnable<BigInteger> cir
    ) {
        cir.setReturnValue(
            item == null
                ? BigInteger.ONE
                : this.optimise10sky$factors.getOrDefault(item, BigInteger.ONE)
        );
    }
}