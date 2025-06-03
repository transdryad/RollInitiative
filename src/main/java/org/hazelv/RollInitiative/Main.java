package org.hazelv.RollInitiative;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionType;

import java.util.Objects;
import java.util.Random;

public class Main {
    public static Random random = new Random();

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.setBrandName("Hazel");
        System.setProperty("minestom.chunk-view-distance", "32");
        System.setProperty("minestom.entity-view-distance", "32");

        DimensionType initRealm = DimensionType.builder()
                .height(1024)
                //.logicalHeight(4096)
                //.minY(-2048)
                .hasRaids(false)
                .hasCeiling(false)
                .hasSkylight(true)
                .bedWorks(true)
                .piglinSafe(false)
                .natural(true)
                .respawnAnchorWorks(false)
                .ultrawarm(false)
                .build();


        DynamicRegistry<DimensionType> dimensionRegistry = MinecraftServer.getDimensionTypeRegistry();

        dimensionRegistry.register("rollinit:initrealm", initRealm);

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(Objects.requireNonNull(dimensionRegistry.getKey(initRealm)));

        instanceContainer.setGenerator(new TerrainGenerator(random.nextLong()));

        instanceContainer.setChunkSupplier(LightingChunk::new);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
            player.setGameMode(GameMode.CREATIVE);
        });

        MojangAuth.init();
        OpenToLAN.open();
        MinecraftServer.setDifficulty(Difficulty.PEACEFUL);
        minecraftServer.start("0.0.0.0", 25565);
    }
}
