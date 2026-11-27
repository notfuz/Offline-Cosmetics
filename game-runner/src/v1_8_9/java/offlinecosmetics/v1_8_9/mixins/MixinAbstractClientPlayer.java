package offlinecosmetics.v1_8_9.mixins;

import com.mojang.authlib.GameProfile;
import offlinecosmetics.core.OfflineCosmetics;
import offlinecosmetics.core.SkinHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.CompletableFuture;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer {

  /** The resolved premium skin, so the skin manager is not queried again every frame. */
  @Unique
  private ResourceLocation offlinecosmetics$cachedSkin;

  @Inject(method = "getLocationSkin", at = @At("HEAD"), cancellable = true)
  private void onGetLocationSkin(CallbackInfoReturnable<ResourceLocation> cir) {
    if (!OfflineCosmetics.get().configuration().enabled().get()) return;
    if (!OfflineCosmetics.get().configuration().forceSkin().get()) {
      offlinecosmetics$cachedSkin = null;
      return;
    }

    if (offlinecosmetics$cachedSkin != null) {
      cir.setReturnValue(offlinecosmetics$cachedSkin);
      return;
    }

    Minecraft mc = Minecraft.getMinecraft();
    AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;

    String name = player.getName();

    if (mc.getSession().getUsername().equals(name)) {
      GameProfile profile = (GameProfile) SkinHelper.getCachedProfile(name);

      if (profile != null) {
        var map = mc.getSkinManager().loadSkinFromCache(profile);
        if (map.containsKey(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN)) {
          ResourceLocation skin = mc.getSkinManager().loadSkin(
                  map.get(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN),
                  com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN);
          offlinecosmetics$cachedSkin = skin;
          cir.setReturnValue(skin);
        }
        return;
      }

      if (!SkinHelper.isProfileLoading(name)) {
        SkinHelper.setProfileLoading(name, true);
        CompletableFuture.runAsync(() -> {
          try {
            GameProfile gp = mc.getSessionService().fillProfileProperties(mc.getSession().getProfile(), false);
            SkinHelper.cacheProfile(name, gp);
          } catch (Exception e) {
            SkinHelper.setProfileLoading(name, false);
          }
        });
      }
    }
  }
}