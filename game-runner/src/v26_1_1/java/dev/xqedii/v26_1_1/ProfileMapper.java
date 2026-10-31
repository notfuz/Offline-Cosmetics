package dev.xqedii.v26_1_1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import dev.xqedii.core.Configuration;
import dev.xqedii.core.OfflineCosmetics;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

/**
 * Maps the cracked session profile to the premium one.
 *
 * <p>Since Minecraft 1.21.9 {@link GameProfile} is a record, so the UUID can no longer be replaced
 * on the existing object. Instead the profile is swapped for a new one whenever a player or a tab
 * list entry is created for the local user.
 */
public final class ProfileMapper {

  private static final long RETRY_DELAY_MS = 60_000L;

  private static volatile GameProfile premiumProfile;
  private static volatile UUID serverLocalId;
  private static volatile boolean fetching;
  private static volatile long nextAttemptAt;

  private ProfileMapper() {
  }

  /**
   * Returns the profile that should be used instead of the given one. Profiles of other players,
   * and the profile of the local user on servers that already use the premium UUID, are returned
   * untouched.
   */
  public static GameProfile map(GameProfile original) {
    if (original == null) {
      return null;
    }

    try {
      Configuration config = configuration();
      User user = user();
      if (config == null || user == null || user.getName() == null
          || !user.getName().equalsIgnoreCase(original.name())) {
        return original;
      }

      UUID premiumId = user.getProfileId();
      if (premiumId == null || premiumId.equals(original.id())) {
        // Premium server or singleplayer, nothing to restore.
        serverLocalId = null;
        return original;
      }

      boolean cosmetics = config.showCosmetics().get();
      boolean skin = config.forceSkin().get();

      UUID id = original.id();
      if (cosmetics) {
        serverLocalId = original.id();
        id = premiumId;
      } else {
        serverLocalId = null;
      }

      var properties = original.properties();
      if (skin) {
        requestPremiumProfile();
        GameProfile premium = premiumProfile;
        if (premium != null) {
          properties = premium.properties();
        }
      }

      if (id.equals(original.id()) && properties == original.properties()) {
        return original;
      }

      return new GameProfile(id, original.name(), properties);
    } catch (Throwable throwable) {
      warn("Could not map the profile of " + original.name() + ": " + throwable);
      return original;
    }
  }

  /**
   * The player list is keyed by the UUID the server sent, while the local player now carries the
   * premium one. This maps the premium UUID back so the local player still finds its own entry.
   */
  public static UUID aliasId(UUID requested) {
    UUID server = serverLocalId;
    if (server == null || requested == null) {
      return requested;
    }

    User user = user();
    if (user != null && requested.equals(user.getProfileId())) {
      return server;
    }

    return requested;
  }

  /** Starts loading the premium skin early, so it is ready by the time a server is joined. */
  public static void prefetch() {
    if (premiumProfile != null || fetching) {
      return;
    }

    Configuration config = configuration();
    if (config != null && config.enabled().get() && config.forceSkin().get()) {
      requestPremiumProfile();
    }
  }

  private static void requestPremiumProfile() {
    if (premiumProfile != null || fetching) {
      return;
    }

    long now = System.currentTimeMillis();
    if (now < nextAttemptAt) {
      return;
    }

    Minecraft minecraft = Minecraft.getInstance();
    User user = user();
    if (minecraft == null || user == null || user.getProfileId() == null) {
      return;
    }

    fetching = true;
    nextAttemptAt = now + RETRY_DELAY_MS;
    UUID profileId = user.getProfileId();

    Thread thread = new Thread(() -> {
      try {
        MinecraftSessionService service = sessionService(minecraft);
        ProfileResult result = service.fetchProfile(profileId, true);
        if (result != null && result.profile() != null) {
          premiumProfile = result.profile();
        }
      } catch (Throwable throwable) {
        warn("Could not load the premium profile: " + throwable);
      } finally {
        fetching = false;
      }
    }, "OfflineCosmetics-ProfileLoader");
    thread.setDaemon(true);
    thread.start();
  }

  /** The accessor for the session service moved between Minecraft versions. */
  private static MinecraftSessionService sessionService(Minecraft minecraft)
      throws ReflectiveOperationException {
    try {
      return (MinecraftSessionService) Minecraft.class
          .getMethod("getMinecraftSessionService")
          .invoke(minecraft);
    } catch (NoSuchMethodException ignored) {
      Object services = Minecraft.class.getMethod("services").invoke(minecraft);
      return (MinecraftSessionService) services.getClass()
          .getMethod("sessionService")
          .invoke(services);
    }
  }

  private static Configuration configuration() {
    OfflineCosmetics addon = OfflineCosmetics.get();
    if (addon == null) {
      return null;
    }

    Configuration config = addon.configuration();
    return config != null && config.enabled().get() ? config : null;
  }

  private static User user() {
    Minecraft minecraft = Minecraft.getInstance();
    return minecraft == null ? null : minecraft.getUser();
  }

  private static void warn(String message) {
    OfflineCosmetics addon = OfflineCosmetics.get();
    if (addon != null) {
      addon.logger().warn(message);
    }
  }
}
