package ak.hyperdimensionalbag.client;

import ak.hyperdimensionalbag.inventory.BagContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class BagScreen extends ContainerScreen<BagContainer> {

  private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
  private static final int ROW_SIZE = 6;

  public BagScreen(BagContainer bagContainer, PlayerInventory playerInventory, ITextComponent iTextComponent) {
    super(bagContainer, playerInventory, iTextComponent);
    this.imageHeight = 114 + ROW_SIZE * 18;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(matrixStack);
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    this.renderTooltip(matrixStack, mouseX, mouseY);
  }

  @Override
  protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.getMinecraft().getTextureManager().bind(GUI_TEXTURE);
    int i = (width - imageWidth) / 2;
    int j = (height - imageHeight) / 2;
//    this.blit(matrixStack, i, j, 0, 0, xSize, ySize);
    this.blit(matrixStack, i, j, 0, 0, this.imageWidth, ROW_SIZE * 18 + 17);
    this.blit(matrixStack, i, j + ROW_SIZE * 18 + 17, 0, 126, this.imageWidth, 96);
  }

}