package offlinecosmetics.v26_2.mixins;

import com.mojang.authlib.GameProfile;
import offlinecosmetics.v26_2.ProfileMapper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Every player entity is created with a game profile. LabyMod looks cosmetics up by the UUID of
 * that profile, so the local user gets the premium UUID here.
 */
@Mixin(Player.class)
public abstract class MixinPlayer {

  @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, require = 0)
  private static GameProfile offlinecosmetics$mapProfile(GameProfile profile) {
    return ProfileMapper.map(profile);
  }
}
