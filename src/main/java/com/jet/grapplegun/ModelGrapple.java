package com.jet.grapplegun;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelGrapple extends ModelBase {
    public ModelRenderer renderer = new ModelRenderer(this, 0 ,0);
    public ModelRenderer plane1;
    public ModelRenderer plane2;
    public ModelGrapple() {
        textureHeight = 16;
        textureWidth = 16;

        plane1 = new ModelRenderer(this, 0,0).setTextureSize(16,16);
        plane1.addBox(-8f, -8f, 0, 16,16, 0);
        plane1.rotateAngleY = (float)Math.PI / 4;
        plane2 = new ModelRenderer(this, 0,0).setTextureSize(16, 16);
        plane2.addBox(-8f, -8f, 0, 16,16, 0);
        plane2.rotateAngleY = (float)Math.toRadians(90);

        plane1.addChild(plane2);
        renderer.addChild(plane1);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        plane1.rotateAngleZ = headPitch;
        renderer.rotateAngleY = netHeadYaw;
        renderer.render(scale/16);
    }
}
