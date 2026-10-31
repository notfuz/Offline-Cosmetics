package offlinecosmetics.v26_2.mixins;

import offlinecosmetics.v26_2.ProfileMapper;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * The player list is keyed by the UUID the cracked server assigned. The local player carries the
 * premium UUID, so its lookup is pointed back to the server's entry to find its own skin.
 */
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

  @ModifyVariable(
      method = "getPlayerInfo(Ljava/util/UUID;)Lnet/minecraft/client/multiplayer/PlayerInfo;",
      at = @At("HEAD"),
      argsOnly = true,
      require = 0
  )
  private static UUID offlinecosmetics$aliasId(UUID uuid) {
    return ProfileMapper.aliasId(uuid);
  }
}
