package com.craisinlord.antarchy.mixins.client;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererPortalViewAreaAccessor {
    @Accessor("viewArea")
    ViewArea antarchy$getViewArea();

    @Accessor("viewArea")
    void antarchy$setViewArea(ViewArea viewArea);

    @Accessor("sectionOcclusionGraph")
    SectionOcclusionGraph antarchy$getSectionOcclusionGraph();

    @Mutable
    @Accessor("sectionOcclusionGraph")
    void antarchy$setSectionOcclusionGraph(SectionOcclusionGraph graph);

    @Accessor("visibleSections")
    ObjectArrayList<SectionRenderDispatcher.RenderSection> antarchy$getVisibleSections();

    @Mutable
    @Accessor("visibleSections")
    void antarchy$setVisibleSections(ObjectArrayList<SectionRenderDispatcher.RenderSection> sections);

    @Accessor("globalBlockEntities")
    Set<BlockEntity> antarchy$getGlobalBlockEntities();

    @Mutable
    @Accessor("globalBlockEntities")
    void antarchy$setGlobalBlockEntities(Set<BlockEntity> blockEntities);

    @Accessor("lastCameraSectionX")
    int antarchy$getLastCameraSectionX();

    @Accessor("lastCameraSectionX")
    void antarchy$setLastCameraSectionX(int value);

    @Accessor("lastCameraSectionY")
    int antarchy$getLastCameraSectionY();

    @Accessor("lastCameraSectionY")
    void antarchy$setLastCameraSectionY(int value);

    @Accessor("lastCameraSectionZ")
    int antarchy$getLastCameraSectionZ();

    @Accessor("lastCameraSectionZ")
    void antarchy$setLastCameraSectionZ(int value);

    @Accessor("prevCamX")
    double antarchy$getPrevCamX();

    @Accessor("prevCamX")
    void antarchy$setPrevCamX(double value);

    @Accessor("prevCamY")
    double antarchy$getPrevCamY();

    @Accessor("prevCamY")
    void antarchy$setPrevCamY(double value);

    @Accessor("prevCamZ")
    double antarchy$getPrevCamZ();

    @Accessor("prevCamZ")
    void antarchy$setPrevCamZ(double value);

    @Accessor("prevCamRotX")
    double antarchy$getPrevCamRotX();

    @Accessor("prevCamRotX")
    void antarchy$setPrevCamRotX(double value);

    @Accessor("prevCamRotY")
    double antarchy$getPrevCamRotY();

    @Accessor("prevCamRotY")
    void antarchy$setPrevCamRotY(double value);

    @Accessor("xTransparentOld")
    double antarchy$getTransparentSortX();

    @Accessor("xTransparentOld")
    void antarchy$setTransparentSortX(double value);

    @Accessor("yTransparentOld")
    double antarchy$getTransparentSortY();

    @Accessor("yTransparentOld")
    void antarchy$setTransparentSortY(double value);

    @Accessor("zTransparentOld")
    double antarchy$getTransparentSortZ();

    @Accessor("zTransparentOld")
    void antarchy$setTransparentSortZ(double value);

    @Accessor("cloudBuffer")
    VertexBuffer antarchy$getCloudBuffer();

    @Accessor("cloudBuffer")
    void antarchy$setCloudBuffer(VertexBuffer cloudBuffer);

    @Accessor("generateClouds")
    boolean antarchy$getGenerateClouds();

    @Accessor("generateClouds")
    void antarchy$setGenerateClouds(boolean generateClouds);

    @Accessor("prevCloudX")
    int antarchy$getPrevCloudX();

    @Accessor("prevCloudX")
    void antarchy$setPrevCloudX(int value);

    @Accessor("prevCloudY")
    int antarchy$getPrevCloudY();

    @Accessor("prevCloudY")
    void antarchy$setPrevCloudY(int value);

    @Accessor("prevCloudZ")
    int antarchy$getPrevCloudZ();

    @Accessor("prevCloudZ")
    void antarchy$setPrevCloudZ(int value);

    @Accessor("prevCloudColor")
    Vec3 antarchy$getPrevCloudColor();

    @Accessor("prevCloudColor")
    void antarchy$setPrevCloudColor(Vec3 value);

    @Accessor("prevCloudsType")
    CloudStatus antarchy$getPrevCloudsType();

    @Accessor("prevCloudsType")
    void antarchy$setPrevCloudsType(CloudStatus value);

    @Accessor("rainSoundTime")
    int antarchy$getRainSoundTime();

    @Accessor("rainSoundTime")
    void antarchy$setRainSoundTime(int value);

}
