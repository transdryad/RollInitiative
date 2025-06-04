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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.*;

public class TerrainGenerator implements Generator {
    final long seed;
    final JNoise noise;
    public InstanceContainer parent;
    public List<WorldBlock> worldBlocks =  new ArrayList<>();
    //change these later based on choice.
    final int octaves = 8;
    final int lacunarity = 3;
    final double persistence = 0.3;
    final int scale = 640;
    final int height_scale = 384;
    final double waterHeight = 73;

    public TerrainGenerator(long seed, InstanceContainer parent) {
        this.seed = seed;
        this.noise = JNoise.newBuilder().perlin(seed, Interpolation.COSINE, FadeFunction.QUINTIC_POLY).build();
        this.parent = parent;
        defineWorldBlocks();
    }

    @Override
    public void generate(@NotNull GenerationUnit generationUnit) {
        Point start = generationUnit.absoluteStart();
        for (int x = 0; x < generationUnit.size().x(); x++) {
            for (int z = 0; z < generationUnit.size().z(); z++) {
                Point bottom = start.add(x, 0, z);
                boolean grass = false;
                double height = (fractalPerlin(bottom.x(), bottom.z()) * height_scale) + 64;
                generationUnit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height), Block.STONE);
                for (WorldBlock worldBlock : worldBlocks) {
                    if (worldBlock.block == Block.WATER) {
                        if (worldBlock.maxHeight >= bottom.y() && worldBlock.maxHeight <= generationUnit.absoluteEnd().y() && height < worldBlock.maxHeight) {
                            generationUnit.modifier().fill(bottom.withY(height), bottom.add(1, 0, 1).withY(worldBlock.maxHeight), Block.WATER);
                        }
                    } else if (height >= worldBlock.minHeight && height <= worldBlock.maxHeight) {
                        if (worldBlock.noiseEnabled) {
                            double blockValue = noise.evaluateNoise((double) x / worldBlock.noiseScale, (double) z / worldBlock.noiseScale);
                            blockValue = (blockValue + 1) / 2;
                            if (blockValue > worldBlock.threshold) {
                                if (worldBlock.block == Block.GRASS_BLOCK) {
                                    if (blockValue > worldBlock.threshold + (height * 0.004)) {
                                        grass = true;
                                        generationUnit.modifier().fill(bottom.withY(height - 1), bottom.add(1, 0, 1).withY(height), worldBlock.block);
                                    }
                                } else if (worldBlock.block == Block.SNOW_BLOCK) {
                                    if (blockValue > worldBlock.threshold + ((worldBlock.minHeight - height) * 0.01)) {
                                        generationUnit.modifier().fill(bottom.withY(height - 2), bottom.add(1, 0, 1).withY(height), worldBlock.block);
                                    } else {
                                        System.out.println(worldBlock.threshold - ((height - worldBlock.minHeight) * 0.06) + ", " + blockValue);
                                    }
                                } else {
                                    generationUnit.modifier().fill(bottom.withY(height - 3), bottom.add(1, 0, 1).withY(height), worldBlock.block);
                                }
                            }
                        } else {
                            if (worldBlock.block == Block.DIRT && grass) {
                                generationUnit.modifier().fill(bottom.withY(height - 3), bottom.add(1, 0, 1).withY(height - 1), worldBlock.block);
                            } else if (worldBlock.block == Block.DIRT) {
                                double blockValue = noise.evaluateNoise((double) x / worldBlock.noiseScale, (double) z / worldBlock.noiseScale);
                                blockValue = (blockValue + 1) / 2;
                                if (blockValue > worldBlock.threshold + (height * 0.0054)) {
                                    generationUnit.modifier().fill(bottom.withY(height - 3), bottom.add(1, 0, 1).withY(height), worldBlock.block);
                                }
                            } else {
                                generationUnit.modifier().fill(bottom.withY(height - 3), bottom.add(1, 0, 1).withY(height), worldBlock.block);
                            }
                        }
                    }
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

    private void defineWorldBlocks() {
        worldBlocks.add(new WorldBlock(Block.WATER, 0, false, 0, 64, waterHeight));
        worldBlocks.add(new WorldBlock(Block.SAND, 1000, false, 0.4, 63, waterHeight + 2));
        worldBlocks.add(new WorldBlock(Block.GRASS_BLOCK, 1000, true, 0.15, waterHeight + 2, waterHeight + 22));
        worldBlocks.add(new WorldBlock(Block.DIRT, 1000, false, 0, waterHeight + 2, waterHeight + 30));
        worldBlocks.add(new WorldBlock(Block.SNOW_BLOCK, 101, true, 0, 130, 385));
    }
}
