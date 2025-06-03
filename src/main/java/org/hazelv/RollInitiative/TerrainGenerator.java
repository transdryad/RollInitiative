package org.hazelv.RollInitiative;

import de.articdive.jnoise.core.api.functions.Interpolation;
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class TerrainGenerator implements Generator {
    final long seed;
    final JNoise noise;

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.noise = JNoise.newBuilder().perlin(seed, Interpolation.COSINE, FadeFunction.QUINTIC_POLY).build();
    }

    @Override
    public void generate(@NotNull GenerationUnit generationUnit) {
        //generationUnit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK);
        Point start = generationUnit.absoluteStart();
        for (int x = 0; x < generationUnit.size().x(); x++) {
            for (int z = 0; z < generationUnit.size().z(); z++) {
                Point bottom = start.add(x, 0, z);
                synchronized (noise) {
                    double height1 = (noise.evaluateNoise(bottom.x()*0.005, bottom.z()*0.005) * 1024);
                    // * 16 means the height will be between -16 and +16
                    generationUnit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height1), Block.STONE);
                }
            }
        }
    }

    @Override
    public void generateAll(@NotNull Collection<@NotNull GenerationUnit> units) {
        units.forEach(this::generate);
    }
}
