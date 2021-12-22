package ak.hyperdimensionalbag.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Created by A.K. on 2019/06/01.
 */
public class ObjectUtils {
  public static Block getBlock(String registeredName) {
    return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registeredName));
  }
}
