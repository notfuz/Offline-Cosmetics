package offlinecosmetics.v1_21_4.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import offlinecosmetics.core.OfflineCosmetics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

  @Shadow @Final @Mutable private GameProfile localGameProfile;

  @Inject(method = "handleLogin", at = @At("HEAD"))
  private void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
    OfflineCosmetics addon = OfflineCosmetics.get();
    if (addon == null) return;
    if (!addon.configuration().enabled().get()) return;
    if (!addon.configuration().showCosmetics().get()) return;

    GameProfile profile = this.localGameProfile;
    var user = Minecraft.getInstance().getUser();
    if (profile == null || user == null) return;
    if (!user.getName().equals(profile.getName())) return;

    var premiumId = user.getProfileId();
    if (premiumId == null) return;

    GameProfile mapped = new GameProfile(premiumId, profile.getName());
    mapped.getProperties().putAll(profile.getProperties());
    this.localGameProfile = mapped;
  }
}
