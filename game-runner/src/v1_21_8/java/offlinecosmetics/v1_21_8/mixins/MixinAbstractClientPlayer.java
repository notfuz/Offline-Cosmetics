package offlinecosmetics.v1_21_8.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import offlinecosmetics.core.OfflineCosmetics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.CompletableFuture;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends Player {

  public MixinAbstractClientPlayer(Level level, GameProfile gameProfile) {
    super(level, gameProfile);
  }

  @Unique
  private PlayerSkin myCachedSkin;
  @Unique
  private GameProfile fetchedProfile;
  @Unique
  private boolean isProfileLoading = false;
  @Unique
  private long nextFetchAttemptAt = 0L;

  @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
  private void onGetSkin(CallbackInfoReturnable<PlayerSkin> cir) {
    OfflineCosmetics addon = OfflineCosmetics.get();
    if (addon == null) return;

    try {
      if (!addon.configuration().enabled().get() || !addon.configuration().forceSkin().get()) {
        return;
      }
    } catch (Exception e) { return; }

    if (myCachedSkin != null) {
      cir.setReturnValue(myCachedSkin);
      return;
    }

    Minecraft mc = Minecraft.getInstance();
    var sessionUser = mc.getUser();

    if (sessionUser != null && sessionUser.getName().equalsIgnoreCase(this.getGameProfile().getName())) {
      if (fetchedProfile != null) {
        PlayerSkin skin = mc.getSkinManager().getInsecureSkin(fetchedProfile);

        if (skin != null && skin.texture() != null && !skin.texture().getPath().contains("default")) {
          this.myCachedSkin = skin;
          cir.setReturnValue(skin);
        }
        return;
      }

      if (!isProfileLoading && System.currentTimeMillis() >= nextFetchAttemptAt) {
        isProfileLoading = true;
        final var profileId = sessionUser.getProfileId();

        CompletableFuture.runAsync(() -> {
          try {
            var result = mc.getMinecraftSessionService().fetchProfile(profileId, false);
            if (result != null) {
              mc.execute(() -> {
                this.fetchedProfile = result.profile();
                mc.getSkinManager().getInsecureSkin(this.fetchedProfile);
              });
            } else {
              mc.execute(this::offlinecosmetics$scheduleRetry);
            }
          } catch (Exception e) {
            addon.logger().warn("Could not load the premium profile: " + e);
            mc.execute(this::offlinecosmetics$scheduleRetry);
          }
        });
      }
    }
  }

  /** A failed fetch is retried after a delay instead of being stored as a profile without textures. */
  @Unique
  private void offlinecosmetics$scheduleRetry() {
    this.isProfileLoading = false;
    this.nextFetchAttemptAt = System.currentTimeMillis() + 60_000L;
  }
}
