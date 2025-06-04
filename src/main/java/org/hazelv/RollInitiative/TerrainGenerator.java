package org.hazelv.RollInitiative;

import de.articdive.jnoise.core.api.functions.Interpolation;
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static java.lang.Math.*;

public class TerrainGenerator implements Generator {
    final long seed;
    final JNoise noise;
    public InstanceContainer parent;
    //change these later based on choice.
    final int octaves = 8;
    final int lacunarity = 3;
    final double persistence = 0.3;
    final int scale = 640;
    final int height_scale = 384;
    final double waterHeight = 80;

    public TerrainGenerator(long seed, InstanceContainer parent) {
        this.seed = seed;
        this.noise = JNoise.newBuilder().perlin(seed, Interpolation.COSINE, FadeFunction.QUINTIC_POLY).build();
        this.parent = parent;
    }

    @Override
    public void generate(@NotNull GenerationUnit generationUnit) {
        //generationUnit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK);
        Point start = generationUnit.absoluteStart();
        for (int x = 0; x < generationUnit.size().x(); x++) {
            for (int z = 0; z < generationUnit.size().z(); z++) {
                Point bottom = start.add(x, 0, z);
                double height = (fractalPerlin(bottom.x(), bottom.z()) * height_scale) + 64;
                generationUnit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height), Block.STONE);
                if (waterHeight >= bottom.y() && waterHeight <= generationUnit.absoluteEnd().y() && height < waterHeight) {
                    generationUnit.modifier().fill(bottom.withY(height + 1), bottom.add(1, waterHeight, 1), Block.WATER);
                }
            }
        }
    }

    @Override
    public void generateAll(@NotNull Collection<@NotNull GenerationUnit> units) {
        units.forEach(this::generate);
    }

    private double fractalPerlin(double x, double z) {
        double value = 0;
        double x1 = x;
        double z1 = z;
        double amplitude = 1;
        for (int i = 0; i < octaves; i++) {
            double noiseAt = abs((noise.evaluateNoise(x1 / scale, z1 / scale) * amplitude));
            value += noiseAt;
            //value += (noise.evaluateNoise(x1 / scale, z1 / scale) * amplitude);
            x1 *= lacunarity;
            z1 *= lacunarity;
            amplitude = amplitude * persistence;
        }
        value = pow(value, 2);
        return clamp(value, -1, 1);
    }
}
