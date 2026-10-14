package util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Central Theme Manager supporting seamless dynamic switching between
 * Modern Light Mode and Rich Dark Mode with persistent storage.
 */
public class ThemeManager {

    private static final String PREF_DARK_MODE = "bus_system_dark_mode";
    private static final ThemeManager INSTANCE = new ThemeManager();

    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private boolean darkMode;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();

    public interface ThemeChangeListener {
        void onThemeChanged(boolean isDark);
    }

    private ThemeManager() {
        // Load saved preference, defaulting to light mode
        this.darkMode = prefs.getBoolean(PREF_DARK_MODE, false);
    }

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean dark) {
        if (this.darkMode != dark) {
            this.darkMode = dark;
            prefs.putBoolean(PREF_DARK_MODE, dark);
            notifyListeners();
        }
    }

    public void toggleTheme() {
        setDarkMode(!this.darkMode);
    }

    public void addListener(ThemeChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ThemeChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onThemeChanged(darkMode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================================
    // Dynamic Theme Color Tokens
    // ==========================================

    public Color getBgColor() {
        return darkMode ? new Color(15, 23, 42) : new Color(245, 248, 252); // #0F172A vs #F5F8FC
    }

    public Color getCardBg() {
        return darkMode ? new Color(30, 41, 59) : Color.WHITE; // #1E293B vs #FFFFFF
    }

    public Color getCardBorder() {
        return darkMode ? new Color(51, 65, 85) : new Color(225, 230, 238); // #334155 vs #E1E6EE
    }

    public Color getHeaderBg() {
        return darkMode ? new Color(10, 15, 29) : new Color(24, 43, 73); // #0A0F1D vs #182B49
    }

    public Color getHeaderSubtext() {
        return darkMode ? new Color(148, 163, 184) : new Color(180, 205, 235);
    }

    public Color getTextPrimary() {
        return darkMode ? new Color(248, 250, 252) : new Color(30, 40, 55); // #F8FAFC vs #1E2837
    }

    public Color getTextSecondary() {
        return darkMode ? new Color(148, 163, 184) : new Color(110, 125, 145); // #94A3B8 vs #6E7D91
    }

    public Color getTextMuted() {
        return darkMode ? new Color(100, 116, 139) : new Color(148, 163, 184);
    }

    public Color getInputBg() {
        return darkMode ? new Color(15, 23, 42) : Color.WHITE;
    }

    public Color getInputBorder() {
        return darkMode ? new Color(71, 85, 105) : new Color(200, 210, 225);
    }

    public Color getInputText() {
        return darkMode ? new Color(248, 250, 252) : new Color(30, 40, 55);
    }

    public Color getTableBg() {
        return darkMode ? new Color(30, 41, 59) : Color.WHITE;
    }

    public Color getTableAlternateRow() {
        return darkMode ? new Color(22, 32, 50) : new Color(248, 250, 252);
    }

    public Color getTableGrid() {
        return darkMode ? new Color(51, 65, 85) : new Color(225, 230, 238);
    }

    public Color getTableHeaderBg() {
        return darkMode ? new Color(15, 23, 42) : new Color(238, 243, 250);
    }

    public Color getTableHeaderText() {
        return darkMode ? new Color(226, 232, 240) : new Color(50, 65, 85);
    }

    // Accents (optimized for high contrast in both themes)
    public Color getPrimaryBlue() {
        return darkMode ? new Color(56, 189, 248) : new Color(41, 128, 185); // Light Blue / Ocean Blue
    }

    public Color getSuccessGreen() {
        return darkMode ? new Color(52, 211, 153) : new Color(39, 174, 96); // Mint Green / Emerald
    }

    public Color getPurpleAccent() {
        return darkMode ? new Color(167, 139, 250) : new Color(142, 68, 173); // Lavender / Violet
    }

    public Color getOrangeAccent() {
        return darkMode ? new Color(251, 146, 60) : new Color(211, 84, 0); // Tangerine / Sunset
    }

    public Color getDangerRed() {
        return darkMode ? new Color(248, 113, 113) : new Color(231, 76, 60); // Coral / Crimson
    }
}
