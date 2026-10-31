package offlinecosmetics.v26_1_2.mixins;

import offlinecosmetics.v26_1_2.ProfileMapper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Loads the premium skin in the background before the first server is joined. */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

  @Inject(method = "tick", at = @At("HEAD"), require = 0)
  private void offlinecosmetics$prefetchProfile(CallbackInfo ci) {
    ProfileMapper.prefetch();
  }
}
