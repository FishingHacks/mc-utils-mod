package net.fishinghacks.utils;

public class CommonUtil {
    public static boolean isInvalidFilename(String name) {
        if(name == null || name.isEmpty() || ".".equals(name) || "..".equals(name)) return false;
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (c == '_' || c == '-' || c == '.' || c == ' ') continue;
            if (c >= 'a' && c <= 'z') continue;
            if (c >= 'A' && c <= 'Z') continue;
            if (c >= '0' && c <= '9') continue;
            return true;
        }
        return false;
    }
}
