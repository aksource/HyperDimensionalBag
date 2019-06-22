package ak.hyperdimensionalbag;

import static ak.hyperdimensionalbag.HyperDimensionalBag.MOD_ID;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig.Loading;
import org.apache.logging.log4j.LogManager;

/**
 * Created by A.K. on 2019/05/30.
 */
public class ConfigUtils {

  public static final Common COMMON;
  static final ForgeConfigSpec configSpec;

  static {
    Builder builder = new ForgeConfigSpec.Builder();
    COMMON = new Common(builder);
    configSpec = builder.build();
  }

  @SuppressWarnings("unused")
  @SubscribeEvent
  public static void configLoading(final Loading event) {
    LogManager
        .getLogger().debug(MOD_ID, "Loaded HyperDimensionalBag config file {}",
        event.getConfig().getFileName());
    COMMON.hardRecipe = COMMON.hardRecipeValue.get();
    COMMON.maxRange = COMMON.maxRangeValue.get();
    COMMON.exchangeInvisibleBlock = COMMON.exchangeInvisibleBlockValue.get();
  }

  public static class Common {

    public boolean hardRecipe = false;
    public int maxRange = 10;
    public boolean exchangeInvisibleBlock = false;
    private BooleanValue hardRecipeValue;
    private IntValue maxRangeValue;
    private BooleanValue exchangeInvisibleBlockValue;

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
