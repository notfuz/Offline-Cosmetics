package offlinecosmetics.v1_20_6.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import offlinecosmetics.core.OfflineCosmetics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Player list entries carry the profile the skin is resolved from. */
@Mixin(PlayerInfo.class)
public abstract class MixinPlayerInfo {

  @ModifyVariable(
      method = "<init>(Lcom/mojang/authlib/GameProfile;Z)V",
      at = @At("HEAD"),
      argsOnly = true
  )
  private static GameProfile offlinecosmetics$mapProfile(GameProfile profile) {
    if (profile == null) return profile;

    OfflineCosmetics addon = OfflineCosmetics.get();
    if (addon == null) return profile;
    if (!addon.configuration().enabled().get()) return profile;
    if (!addon.configuration().showCosmetics().get()) return profile;

    var user = Minecraft.getInstance().getUser();
    if (user == null || user.getProfileId() == null) return profile;
    if (!user.getName().equals(profile.getName())) return profile;

    GameProfile mapped = new GameProfile(user.getProfileId(), profile.getName());
    mapped.getProperties().putAll(profile.getProperties());
    return mapped;
  }
}
