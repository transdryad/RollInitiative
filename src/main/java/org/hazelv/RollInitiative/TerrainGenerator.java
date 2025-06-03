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

import static java.lang.Math.*;

public class TerrainGenerator implements Generator {
    final long seed;
    final JNoise noise;
    final int octaves = 6;
    final int lancularity = 3;
    final double persistence = 0.2;
    final int scale = 90;
    final int height_scale = 40;

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
                double height = (fractalPerlin(bottom.x(), bottom.z(), octaves, lancularity, persistence, scale) * height_scale) + 65;
                generationUnit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height), Block.STONE);
            }
        }
    }

    @Override
    public void generateAll(@NotNull Collection<@NotNull GenerationUnit> units) {
        units.forEach(this::generate);
    }

    private double fractalPerlin(double x, double z, int octaves, int lancularity, double persistence, int scale) {
        double value = 0;
        double x1 = x;
        double z1 = z;
        double amplitude = 1;
        for (int i = 0; i < octaves; i++) {
            value += abs((noise.evaluateNoise(x1 / scale, z1 / scale) * amplitude));
            x1 *= lancularity;
            z1 *= lancularity;
            amplitude = amplitude * persistence;
        }
        value = pow(value, 2);
        return clamp(value, -1, 1);
    }
}
