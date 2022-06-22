package ak.hyperdimensionalbag;

import ak.hyperdimensionalbag.inventory.BagInventory;
import ak.hyperdimensionalbag.item.HDBagItem;
import ak.hyperdimensionalbag.util.StorageBoxUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlayerPickHook {

  @SubscribeEvent
  public static void pickUpEvent(final EntityItemPickupEvent event) {
    if (HyperDimensionalBag.loadSB) {
      var player = event.getPlayer();
      var item = event.getItem();
      var inv = player.getInventory().items;
      if (pickUpItemInBag(player, inv, item.getItem())) {
        event.setCanceled(true);
        player.getCommandSenderWorld()
                .playSound(player, new BlockPos(player.getX(), player.getY(), player.getZ()),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                        0.2F,
                        ((player.getCommandSenderWorld().random.nextFloat() - player.getCommandSenderWorld().random
                                .nextFloat()) * 0.7F + 1.0F) * 2.0F);
        var stackSize = item.getItem().getCount();
        player.take(item, stackSize);
      }
    }
  }

  private static boolean pickUpItemInBag(Player player, NonNullList<ItemStack> inv, ItemStack item) {
    for (var itemStack : inv) {
      if (itemStack.getItem() instanceof HDBagItem) {
        var data = new BagInventory(itemStack, player);
        for (var j = 0; j < data.getContainerSize(); j++) {
          if (!data.getItem(j).isEmpty() && StorageBoxUtils.isItemStorageBox(data.getItem(j))
                  && StorageBoxUtils.isAutoCollect(data.getItem(j))) {
            var storageStack = StorageBoxUtils.peekItemStackAll(data.getItem(j));
            if (storageStack != null && areOreNameEquals(item, storageStack)) {
              var copyStack = storageStack.copy();
              copyStack.setCount(item.getCount());
              StorageBoxUtils.addItemStack(data.getItem(j), copyStack);
              item.setCount(0);
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private static boolean areOreNameEquals(ItemStack check, ItemStack target) {
    var targetTagNames = getTagNames(target);
    var checkTagNames = getTagNames(check);
    if (targetTagNames.size() > 0 && checkTagNames.size() > 0) {
      for (var oreName : targetTagNames) {
        if (checkTagNames.contains(oreName)) {
          return true;
        }
      }
      return false;
    } else {
      return check.sameItem(target);
    }
  }

  private static Collection<ResourceLocation> getTagNames(ItemStack itemStack) {
    var tagNames = new ArrayList<ResourceLocation>();
    var tagManager = ForgeRegistries.ITEMS.tags();
    if (Objects.nonNull(tagManager)) {
      tagManager.getReverseTag(itemStack.getItem()).ifPresent(tag -> tagNames.addAll(tag.getTagKeys().map(TagKey::location).toList()));
    }
    return tagNames;
  }
}