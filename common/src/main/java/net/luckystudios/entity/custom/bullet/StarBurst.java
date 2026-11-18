package net.luckystudios.entity.custom.bullet;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws a firework-star shaped burst of coloured particles on the server, so every nearby player sees
 * the shape the star was crafted with (ball, large ball, star, creeper face or burst).
 */
public final class StarBurst {
    private static final int DEFAULT_COLOR = 0xFFFFFF;

    private StarBurst() {}

    public static int pickColor(IntList colors, net.minecraft.util.RandomSource random) {
        return colors.isEmpty() ? DEFAULT_COLOR : colors.getInt(random.nextInt(colors.size()));
    }

    public static Vector3f toVector(int rgb) {
        return Vec3.fromRGB24(rgb).toVector3f();
    }

    public static void spawn(ServerLevel level, Vec3 center, Vec3 facing, FireworkExplosion explosion) {
        IntList colors = explosion.colors();
        IntList fades = explosion.fadeColors();
        var random = level.random;
        List<Vec3> points = shapePoints(explosion.shape(), facing, random);
        for (Vec3 p : points) {
            int color = pickColor(colors, random);
            int fade = fades.isEmpty() ? color : pickColor(fades, random);
            level.sendParticles(new DustColorTransitionOptions(toVector(color), toVector(fade), 1.2F),
                    center.x + p.x, center.y + p.y, center.z + p.z, 1, 0, 0, 0, 0);
        }
        // Outward sparks for punch
        int sparks = explosion.shape() == FireworkExplosion.Shape.LARGE_BALL ? 16 : 8;
        for (int i = 0; i < sparks; i++) {
            Vec3 dir = randomDirection(random);
            level.sendParticles(explosion.hasTwinkle() ? ParticleTypes.FIREWORK : ParticleTypes.POOF,
                    center.x, center.y, center.z, 0, dir.x, dir.y, dir.z, 0.25);
        }
        if (explosion.hasTrail()) {
            for (int i = 0; i < 6; i++) {
                Vec3 dir = randomDirection(random);
                level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 0, dir.x, dir.y, dir.z, 0.15);
            }
        }
    }

    private static Vec3 randomDirection(net.minecraft.util.RandomSource random) {
        return new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
    }

    private static List<Vec3> shapePoints(FireworkExplosion.Shape shape, Vec3 facing, net.minecraft.util.RandomSource random) {
        List<Vec3> out = new ArrayList<>();
        Vec3 f = facing.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : facing.normalize();
        Vec3 u = Math.abs(f.y) > 0.95 ? new Vec3(1, 0, 0) : f.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 v = u.cross(f).normalize();
        switch (shape) {
            case SMALL_BALL -> sphere(out, 20, 0.7);
            case LARGE_BALL -> sphere(out, 48, 1.4);
            case STAR -> {
                Vec3[] tips = new Vec3[10];
                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI / 2 + i * Math.PI / 5;
                    double r = (i % 2 == 0) ? 1.3 : 0.55;
                    tips[i] = u.scale(Math.cos(angle) * r).add(v.scale(Math.sin(angle) * r));
                }
                for (int i = 0; i < 10; i++) {
                    Vec3 a = tips[i];
                    Vec3 b = tips[(i + 1) % 10];
                    for (int s = 0; s < 4; s++) out.add(a.lerp(b, s / 4.0));
                }
            }
            case CREEPER -> {
                // A pixel face: two eyes and a frowning mouth, 0.25 blocks per pixel
                int[][] pixels = {
                        {-2, 3}, {-1, 3}, {-2, 2}, {-1, 2}, {1, 3}, {2, 3}, {1, 2}, {2, 2},
                        {0, 1}, {0, 0}, {-1, 0}, {1, 0}, {-1, -1}, {1, -1}, {-2, -1}, {2, -1}, {-2, -2}, {2, -2}
                };
                for (int[] px : pixels) out.add(u.scale(px[0] * 0.25).add(v.scale((px[1] - 0.5) * 0.25)));
            }
            case BURST -> {
                for (int ray = 0; ray < 14; ray++) {
                    Vec3 dir = randomDirection(random);
                    for (int s = 1; s <= 5; s++) out.add(dir.scale(s * 0.3));
                }
            }
        }
        return out;
    }

    private static void sphere(List<Vec3> out, int count, double radius) {
        double golden = Math.PI * (3 - Math.sqrt(5));
        for (int i = 0; i < count; i++) {
            double y = 1 - (i / (double) (count - 1)) * 2;
            double r = Math.sqrt(1 - y * y);
            double theta = golden * i;
            out.add(new Vec3(Math.cos(theta) * r * radius, y * radius, Math.sin(theta) * r * radius));
        }
    }
}
