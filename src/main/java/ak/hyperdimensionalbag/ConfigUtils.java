package ak.hyperdimensionalbag;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import static ak.hyperdimensionalbag.HyperDimensionalBag.LOGGER;
import static ak.hyperdimensionalbag.HyperDimensionalBag.MOD_ID;

/**
 * Created by A.K. on 2019/05/30.
 */
public class ConfigUtils {

  public static final Common COMMON;
  static final ForgeConfigSpec configSpec;

  static {
    var builder = new ForgeConfigSpec.Builder();
    COMMON = new Common(builder);
    configSpec = builder.build();
  }

  @SubscribeEvent
  public static void configLoading(final ModConfigEvent.Loading event) {
    LOGGER.debug("Loaded HyperDimensionalBag config file {}",
        event.getConfig().getFileName());
    COMMON.hardRecipe = COMMON.hardRecipeValue.get();
    COMMON.maxRange = COMMON.maxRangeValue.get();
    COMMON.exchangeInvisibleBlock = COMMON.exchangeInvisibleBlockValue.get();
  }

  public static class Common {

    public boolean hardRecipe = false;
    public int maxRange = 10;
    public boolean exchangeInvisibleBlock = false;
    private final BooleanValue hardRecipeValue;
    private final IntValue maxRangeValue;
    private final BooleanValue exchangeInvisibleBlockValue;

    Common(Builder builder) {
      builder.comment("Common settings")
          .push(MOD_ID);
      hardRecipeValue = builder.comment("Change recipe hard").define("HardRecipe", hardRecipe);
      maxRangeValue = builder.comment("Set max range of BlockExchanger")
          .defineInRange("maxBlockExchangeRange", maxRange, 0, 255);
      exchangeInvisibleBlockValue = builder.comment("true : exchange invisible block")
          .define("exchangeInvisibleBlock", exchangeInvisibleBlock);
      builder.pop();
    }
  }
}
