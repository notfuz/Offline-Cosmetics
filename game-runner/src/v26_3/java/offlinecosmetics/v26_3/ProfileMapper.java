package offlinecosmetics.v26_3;

import com.mojang.authlib.GameProfile;
import offlinecosmetics.core.Configuration;
import offlinecosmetics.core.OfflineCosmetics;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
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
        GameProfile profile = fetchPremiumProfile(minecraft, profileId);
        if (profile != null) {
          premiumProfile = profile;
        } else {
          warn("Could not load the premium profile: no session API matched");
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

  /**
   * authlib 10 (Minecraft 26.3) renamed its yggdrasil package and moved the session classes, so
   * nothing from there is referenced by name. The session service and the profile resolver are
   * called by method name instead, and whichever answers first wins.
   */
  private static GameProfile fetchPremiumProfile(Minecraft minecraft, UUID profileId)
      throws ReflectiveOperationException {
    Object services = resolve(minecraft, "services");

    Object session = resolve(minecraft, "getMinecraftSessionService", "minecraftSessionService");
    if (session == null && services != null) {
      session = resolve(services, "sessionService", "getSessionService");
    }

    GameProfile profile = null;
    if (session != null) {
      profile = toProfile(call(session, "fetchProfile", profileId, true), 0);
      if (profile == null) {
        profile = toProfile(call(session, "fetchProfile", profileId, false), 0);
      }
    }

    if (profile == null && services != null) {
      Object resolver = resolve(services, "profileResolver", "getProfileResolver");
      if (resolver != null) {
        profile = toProfile(call(resolver, "fetchById", profileId), 0);
        if (profile == null) {
          profile = toProfile(call(resolver, "fetchById", profileId, true), 0);
        }
      }
    }

    return profile;
  }

  private static Object resolve(Object target, String... names) throws ReflectiveOperationException {
    if (target == null) {
      return null;
    }

    for (String name : names) {
      Object value = call(target, name);
      if (value != null) {
        return value;
      }

      for (Field field : target.getClass().getFields()) {
        if (field.getName().equals(name)) {
          field.setAccessible(true);
          return field.get(target);
        }
      }
    }

    return null;
  }

  /** Unwraps the result types ({@code Optional}, or a record exposing {@code profile()}). */
  private static GameProfile toProfile(Object result, int depth) throws ReflectiveOperationException {
    if (result == null || depth > 3) {
      return null;
    }

    if (result instanceof GameProfile profile) {
      return profile;
    }

    if (result instanceof Optional<?> optional) {
      return toProfile(optional.orElse(null), depth + 1);
    }

    return toProfile(call(result, "profile"), depth + 1);
  }

  /** Calls a public method by name and argument count, or returns null if there is none. */
  private static Object call(Object target, String name, Object... args)
      throws ReflectiveOperationException {
    for (Method method : target.getClass().getMethods()) {
      if (!method.getName().equals(name) || method.getParameterCount() != args.length) {
        continue;
      }

      Class<?>[] types = method.getParameterTypes();
      boolean matches = true;
      for (int index = 0; index < types.length; index++) {
        if (!wrap(types[index]).isInstance(args[index])) {
          matches = false;
          break;
        }
      }

      if (matches) {
        try {
          method.setAccessible(true);
        } catch (RuntimeException ignored) {
          // The method is public, calling it works without this.
        }
        return method.invoke(target, args);
      }
    }

    return null;
  }

  private static Class<?> wrap(Class<?> type) {
    if (!type.isPrimitive()) {
      return type;
    }

    if (type == boolean.class) {
      return Boolean.class;
    }

    if (type == int.class) {
      return Integer.class;
    }

    if (type == long.class) {
      return Long.class;
    }

    return type;
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
