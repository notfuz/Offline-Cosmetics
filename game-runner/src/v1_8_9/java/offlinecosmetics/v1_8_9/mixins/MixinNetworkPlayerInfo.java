package offlinecosmetics.v1_8_9.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import offlinecosmetics.core.OfflineCosmetics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Player list entries carry the profile the skin is resolved from. */
@Mixin(NetworkPlayerInfo.class)
public abstract class MixinNetworkPlayerInfo {

  @ModifyVariable(
      method = "<init>(Lcom/mojang/authlib/GameProfile;)V",
      at = @At("HEAD"),
      argsOnly = true
  )
  private static GameProfile offlinecosmetics$mapProfile(GameProfile profile) {
    if (profile == null) return profile;

    OfflineCosmetics addon = OfflineCosmetics.get();
    if (addon == null || !addon.configuration().enabled().get() || !addon.configuration().showCosmetics().get()) {
      return profile;
    }

    Minecraft mc = Minecraft.getMinecraft();
    if (mc.getSession() == null || mc.getSession().getProfile() == null) return profile;
    if (!mc.getSession().getUsername().equalsIgnoreCase(profile.getName())) return profile;

    GameProfile mapped = new GameProfile(mc.getSession().getProfile().getId(), profile.getName());
    mapped.getProperties().putAll(profile.getProperties());
    return mapped;
  }
}
