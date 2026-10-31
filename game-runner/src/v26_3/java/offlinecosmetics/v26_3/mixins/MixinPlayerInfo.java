package offlinecosmetics.v26_3.mixins;

import com.mojang.authlib.GameProfile;
import offlinecosmetics.v26_3.ProfileMapper;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Player list entries carry the profile the skin is resolved from. */
@Mixin(PlayerInfo.class)
public abstract class MixinPlayerInfo {

  @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, require = 0)
  private static GameProfile offlinecosmetics$mapProfile(GameProfile profile) {
    return ProfileMapper.map(profile);
  }
}
