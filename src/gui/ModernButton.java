package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern anti-aliased rounded button with dynamic hover and press animations.
 * Completely immune to Windows LookAndFeel background overrides and white-on-white text issues.
 */
public class ModernButton extends JButton {

    private Color normalBg;
    private Color hoverBg;
    private Color pressedBg;
    private Color normalFg;
    private int cornerRadius = 8;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public ModernButton(String text) {
        this(text, null, new Color(41, 128, 185), Color.WHITE);
    }

    public ModernButton(String text, Icon icon) {
        this(text, icon, new Color(41, 128, 185), Color.WHITE);
    }

    public ModernButton(String text, Color bgColor, Color fgColor) {
        this(text, null, bgColor, fgColor);
    }

    public ModernButton(String text, Icon icon, Color bgColor, Color fgColor) {
        super(text, icon);
        this.normalBg = (bgColor != null) ? bgColor : new Color(41, 128, 185);
        this.hoverBg = computeHoverColor(this.normalBg);
        this.pressedBg = computePressedColor(this.normalBg);
        this.normalFg = (fgColor != null) ? fgColor : Color.WHITE;

        init();
    }

    private void init() {
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(normalFg);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setColors(Color bg, Color fg) {
        this.normalBg = bg;
        this.hoverBg = computeHoverColor(bg);
        this.pressedBg = computePressedColor(bg);
        this.normalFg = fg;
        setForeground(fg);
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    private Color computeHoverColor(Color c) {
        if (c == null) return new Color(52, 152, 219);
        // Slightly lighten on hover
        int r = Math.min(255, (int) (c.getRed() * 1.15) + 12);
        int g = Math.min(255, (int) (c.getGreen() * 1.15) + 12);
        int b = Math.min(255, (int) (c.getBlue() * 1.15) + 12);
        return new Color(r, g, b, c.getAlpha());
    }

    private Color computePressedColor(Color c) {
        if (c == null) return new Color(31, 97, 141);
        // Slightly darken on press
        int r = Math.max(0, (int) (c.getRed() * 0.85));
        int g = Math.max(0, (int) (c.getGreen() * 0.85));
        int b = Math.max(0, (int) (c.getBlue() * 0.85));
        return new Color(r, g, b, c.getAlpha());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bg;
        if (!isEnabled()) {
            bg = new Color(180, 190, 202);
            setForeground(new Color(245, 248, 252));
        } else if (isPressed) {
            bg = pressedBg;
            setForeground(normalFg);
        } else if (isHovered) {
            bg = hoverBg;
            setForeground(normalFg);
        } else {
            bg = normalBg;
            setForeground(normalFg);
        }

        // Draw smooth rounded pill/card background
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, cornerRadius, cornerRadius));

        // Subtle top reflection highlight
        if (isEnabled() && !isPressed) {
            g2.setColor(new Color(255, 255, 255, 35));
            g2.fill(new RoundRectangle2D.Double(1, 1, w - 2, (h / 2.0), cornerRadius - 1, cornerRadius - 1));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
