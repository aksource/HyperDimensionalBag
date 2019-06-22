package ak.hyperdimensionalbag.util;

import ak.hyperdimensionalbag.item.ItemBlockExchanger;
import ak.hyperdimensionalbag.item.ItemHDBag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
/**
 * 登録処理用クラス Created by A.K. on 16/05/29.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistrationUtils {

  public static List<Item> itemHDBagList = new ArrayList<>();

  @SubscribeEvent
  @SuppressWarnings("unused")
  public static void registerItems(final RegistryEvent.Register<Item> event) {
    IForgeRegistry<Item> registry = event.getRegistry();
    Arrays.stream(EnumDyeColor.values())
        .forEach(enumDyeColor -> {
            Item item = new ItemHDBag(enumDyeColor)
            .setRegistryName("hyperdimensionalbag_" + enumDyeColor.getName().toLowerCase());
            registry.register(item);
            itemHDBagList.add(item);
        });
    registry.register(new ItemBlockExchanger()
        .setRegistryName("itemblockexchanger"));
  }
}
