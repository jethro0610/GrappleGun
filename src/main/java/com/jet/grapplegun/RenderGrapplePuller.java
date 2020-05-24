package com.jet.grapplegun;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

public class RenderGrapplePuller extends RenderEntity {
    ModelBase mainModel;
    EntityGrapplePuller grapplePuller;
    private static final ResourceLocation HEAD_TEXTURE = new ResourceLocation("grapplegun:grapplehead.png");
    private static final double SEGMENT_LENGTH = 0.5;
    public RenderGrapplePuller(RenderManager renderManagerIn, ModelBase model) {
        super(renderManagerIn);
        mainModel = model;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        grapplePuller = (EntityGrapplePuller) entity;

        Vec3d prevPos = new Vec3d(grapplePuller.getParentEntity().prevPosX, grapplePuller.getParentEntity().prevPosY, grapplePuller.getParentEntity().prevPosZ);
        Vec3d renderPos = getRenderPosition(prevPos, grapplePuller.getParentEntity().getPositionVector(), Minecraft.getMinecraft().getRenderPartialTicks());
        Vec3d drawOrigin = new Vec3d(x, y, z);

        Vec3d renderEndPointPos = getRenderPosition(grapplePuller.getLastPullLocaiton(), grapplePuller.getPullLocation(), partialTicks);
        Vec3d vectorToEndPoint = renderEndPointPos.subtract(renderPos);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Color c = new Color(200, 80, 40, 255);
        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());

        // Shrink the line width based on the distance of the camera
        float cameraDist = (float) renderManager.getDistanceToCamera(x, y, z);
        if(cameraDist > 12)
            cameraDist = 12;
        GL11.glLineWidth(15 - cameraDist);

        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        // Draw the rope in segments
        int segments = (int)Math.floor(vectorToEndPoint.lengthVector() / SEGMENT_LENGTH);
        int colorAdd = 0;
        for(int i = segments; i >= 0; i--) {
            Vec3d segVec = vectorToEndPoint.scale(1/vectorToEndPoint.lengthVector());
            segVec = segVec.scale(i * SEGMENT_LENGTH);

            vertexbuffer.pos(drawOrigin.x + segVec.x, drawOrigin.y + segVec.y,drawOrigin.z + segVec.z);
            vertexbuffer.color(c.getRed() + colorAdd, c.getGreen() + colorAdd, c.getBlue() + colorAdd, c.getAlpha());
            vertexbuffer.endVertex();

            colorAdd = (i % 2) * 50;
        }
        vertexbuffer.pos(drawOrigin.x, drawOrigin.y, drawOrigin.z);
        vertexbuffer.color(c.getRed() + colorAdd, c.getGreen() + colorAdd, c.getBlue() + colorAdd, c.getAlpha());
        vertexbuffer.endVertex();
        tessellator.draw();

        GL11.glDepthMask(true);
        GL11.glPopAttrib();

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.translate(drawOrigin.x + vectorToEndPoint.x, drawOrigin.y + vectorToEndPoint.y, drawOrigin.z + vectorToEndPoint.z);

        bindTexture(HEAD_TEXTURE);
        mainModel.render(entity, 0, 0, 0, (float)grapplePuller.getPitch(), (float)grapplePuller.getYaw(), 1);

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    private Vec3d getRenderPosition(Vec3d prevPos, Vec3d curPos, float partialTicks) {
        double x = getRenderDouble(prevPos.x, curPos.x, partialTicks);
        double y = getRenderDouble(prevPos.y, curPos.y, partialTicks);
        double z = getRenderDouble(prevPos.z, curPos.z, partialTicks);

        return new Vec3d(x, y, z);
    }

    private double getRenderDouble(double prevPos, double curPos, float partialTicks) {
        return prevPos + (curPos - prevPos) * partialTicks;
    }
}