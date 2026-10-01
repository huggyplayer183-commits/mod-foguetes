package com.bibliotecarios.foguetes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class BibliotecariosFoguetes implements ModInitializer {

    private static final Set<Villager> TRACKED =
            Collections.newSetFromMap(new WeakHashMap<>());
    private static int tickCounter = 0;

    @Override
    public void onInitialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Villager villager) {
                TRACKED.add(villager);
                tryAddTrade(villager);
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof Villager villager) {
                TRACKED.remove(villager);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tickCounter % 40 != 0) {
                return;
            }
            List<Villager> copy = new ArrayList<>(TRACKED);
            for (Villager villager : copy) {
                if (villager.isRemoved()) {
                    TRACKED.remove(villager);
                } else {
                    tryAddTrade(villager);
                }
            }
        });
    }

    private static void tryAddTrade(Villager villager) {
        if (villager.level().isClientSide() || villager.isBaby()) {
            return;
        }
        if (!villager.getVillagerData().profession().is(VillagerProfession.LIBRARIAN)) {
            return;
        }

        MerchantOffers offers = villager.getOffers();
        for (MerchantOffer offer : offers) {
            if (offer.getResult().is(Items.FIREWORK_ROCKET)
                    && offer.getItemCostA().itemStack().is(Items.EMERALD)) {
                return;
            }
        }

        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 1);
        rocket.set(DataComponents.FIREWORKS, new Fireworks(1, List.of()));

        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 1),
                rocket,
                Integer.MAX_VALUE,
                0,
                0.0f));
    }
                  }
