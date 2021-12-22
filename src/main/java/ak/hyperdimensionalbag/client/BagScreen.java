package ak.hyperdimensionalbag.client;

import ak.hyperdimensionalbag.inventory.BagContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BagScreen extends AbstractContainerScreen<BagContainer> {

  private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
  private static final int ROW_SIZE = 6;

  public BagScreen(BagContainer bagContainer, Inventory inventory, Component component) {
    super(bagContainer, inventory, component);
    this.imageHeight = 114 + ROW_SIZE * 18;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(poseStack);
    super.render(poseStack, mouseX, mouseY, partialTicks);
    this.renderTooltip(poseStack, mouseX, mouseY);
  }

  @Override
  protected void renderBg(PoseStack poseStack, float partialTicks, int x, int y) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, GUI_TEXTURE);
    int i = (width - imageWidth) / 2;
    int j = (height - imageHeight) / 2;
    this.blit(poseStack, i, j, 0, 0, this.imageWidth, ROW_SIZE * 18 + 17);
    this.blit(poseStack, i, j + ROW_SIZE * 18 + 17, 0, 126, this.imageWidth, 96);
  }

}