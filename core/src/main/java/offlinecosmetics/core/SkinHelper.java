package offlinecosmetics.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkinHelper {
  // Written from an async thread and read on the client thread.
  private static final Map<String, Object> cachedProfiles = new ConcurrentHashMap<>();
  private static final Map<String, Boolean> loadingFlags = new ConcurrentHashMap<>();

  public static boolean isProfileLoading(String name) {
    return loadingFlags.getOrDefault(name, false);
  }

  public static void setProfileLoading(String name, boolean loading) {
    loadingFlags.put(name, loading);
  }

  public static Object getCachedProfile(String name) {
    return cachedProfiles.get(name);
  }

  public static void cacheProfile(String name, Object profile) {
    if (profile == null) {
      cachedProfiles.remove(name);
    } else {
      cachedProfiles.put(name, profile);
    }
  }
}
