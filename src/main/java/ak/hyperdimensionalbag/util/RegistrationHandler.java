package ak.hyperdimensionalbag.util;

import ak.hyperdimensionalbag.inventory.BagContainer;
import ak.hyperdimensionalbag.item.BlockExchangerItem;
import ak.hyperdimensionalbag.item.HDBagItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import static ak.hyperdimensionalbag.HyperDimensionalBag.MOD_ID;

/**
 * 登録処理用クラス Created by A.K. on 16/05/29.
 */
@ParametersAreNonnullByDefault
public class RegistrationHandler {

  private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
  private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);
  public static final List<RegistryObject<Item>> ITEM_HD_BAG_LIST = new ArrayList<>();
  public static final String H_D_BAG_REGISTER_PREFIX = "hyperdimensionalbag";

  public static void register(IEventBus eventBus) {
    Arrays.stream(DyeColor.values())
            .forEach(dyeColor -> ITEM_HD_BAG_LIST.add(ITEMS.register(new StringJoiner("_").add(H_D_BAG_REGISTER_PREFIX).add(dyeColor.getName()).toString(), () -> new HDBagItem(dyeColor))));
    ITEMS.register("itemblockexchanger", BlockExchangerItem::new);
    ITEMS.register(eventBus);
    CONTAINERS.register(H_D_BAG_REGISTER_PREFIX, () -> BagContainer.BAG_CONTAINER_TYPE);
    CONTAINERS.register(eventBus);
  }
}
