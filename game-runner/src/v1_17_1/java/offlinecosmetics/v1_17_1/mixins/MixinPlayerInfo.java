package offlinecosmetics.v1_17_1.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import offlinecosmetics.core.OfflineCosmetics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInfo.class)
public abstract class MixinPlayerInfo {

  @Shadow @Final @Mutable private GameProfile profile;

  @Inject(method = "<init>", at = @At("RETURN"))
  private void onInit(CallbackInfo ci) {
    GameProfile currentProfile = this.profile;
    if (currentProfile == null) return;

    OfflineCosmetics addon = OfflineCosmetics.get();
    if (addon == null) return;
    if (!addon.configuration().enabled().get()) return;
    if (!addon.configuration().showCosmetics().get()) return;

    var user = Minecraft.getInstance().getUser();
    if (user == null || user.getGameProfile() == null) return;
    if (!user.getName().equals(currentProfile.getName())) return;

    GameProfile mapped = new GameProfile(user.getGameProfile().getId(), currentProfile.getName());
    mapped.getProperties().putAll(currentProfile.getProperties());
    this.profile = mapped;
  }
}
