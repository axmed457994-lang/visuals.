package com.visuals;
import com.visuals.mixin.SimpleOptionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public final class Visuals {
    static boolean hud = true, fullbright = false, particles = false, zoom = false;
    static final boolean[] prev = new boolean[4];
    static Double origGamma; static Integer origFov;

    static boolean pressed(MinecraftClient mc, int key) {
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), key);
    }
    static boolean edge(MinecraftClient mc, int i, int key) {
        boolean now = mc.currentScreen == null && pressed(mc, key);
        boolean hit = now && !prev[i]; prev[i] = now; return hit;
    }
    @SuppressWarnings("unchecked")
    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        if (edge(mc, 0, GLFW.GLFW_KEY_H)) hud = !hud;
        if (edge(mc, 1, GLFW.GLFW_KEY_B)) fullbright = !fullbright;
        if (edge(mc, 2, GLFW.GLFW_KEY_P)) particles = !particles;

        SimpleOption<Double> g = mc.options.getGamma();
        if (fullbright) {
            if (origGamma == null) origGamma = g.getValue();
            ((SimpleOptionAccessor) (Object) g).visuals$setRaw(16.0);
        } else if (origGamma != null) {
            ((SimpleOptionAccessor) (Object) g).visuals$setRaw(origGamma); origGamma = null;
        }
        SimpleOption<Integer> f = mc.options.getFov();
        zoom = mc.currentScreen == null && pressed(mc, GLFW.GLFW_KEY_C);
        if (zoom) {
            if (origFov == null) origFov = f.getValue();
            ((SimpleOptionAccessor) (Object) f).visuals$setRaw(25);
        } else if (origFov != null) {
            ((SimpleOptionAccessor) (Object) f).visuals$setRaw(origFov); origFov = null;
        }
        if (particles && mc.world.getTime() % 2 == 0) {
            var r = mc.world.random;
            for (int i = 0; i < 3; i++) {
                double a = r.nextDouble() * Math.PI * 2, d = 1 + r.nextDouble() * 1.5;
                mc.world.addParticle(i == 0 ? ParticleTypes.END_ROD : ParticleTypes.HAPPY_VILLAGER,
                    mc.player.getX() + Math.cos(a) * d, mc.player.getY() + r.nextDouble() * 2.2,
                    mc.player.getZ() + Math.sin(a) * d, 0, 0.03, 0);
            }
        }
    }
    static int rainbow(float off) {
        float h = ((System.currentTimeMillis() % 4000L) / 4000f + off) % 1f;
        return 0xFF000000 | MathHelper.hsvToRgb(h, 0.7f, 1f);
    }
    static void line(DrawContext c, MinecraftClient mc, String s, int y, float off) {
        int w = mc.textRenderer.getWidth(s);
        c.fill(2, y - 1, 6 + w, y + 9, 0x90000000);
        c.fill(2, y - 1, 3, y + 9, rainbow(off));
        c.drawTextWithShadow(mc.textRenderer, s, 5, y, rainbow(off));
    }
    public static void renderHud(DrawContext c) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!hud || mc.player == null || mc.options.hudHidden) return;
        int y = 4; float o = 0;
        line(c, mc, "FPS: " + mc.getCurrentFps(), y, o); y += 12; o += .08f;
        line(c, mc, String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()), y, o); y += 12; o += .08f;
        line(c, mc, "Facing: " + mc.player.getHorizontalFacing().asString(), y, o); y += 12; o += .08f;
        line(c, mc, "Fullbright[B]: " + (fullbright ? "ON" : "off"), y, o); y += 12; o += .08f;
        line(c, mc, "Particles[P]: " + (particles ? "ON" : "off") + "  Zoom[C hold]", y, o);
    }
  }
