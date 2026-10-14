package gui;

import model.Seat;
import service.SeatService;
import util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * Custom-painted Java2D Bus Seat Map featuring:
 * - Bus chassis outline, front windshield, steering wheel, entrance door, and aisle.
 * - 2+2 seating layout with rounded seat graphics, headrests, and armrests.
 * - Dynamic color-coding: Available, Selected, Booked (Male/Female/Others), and Preference Matches.
 * - Privacy-preserving hover tooltips displaying adjacent seat passenger gender.
 * - Interactive multi-seat selection (up to 6 seats) with listener callback.
 * - Seamless integration with ThemeManager for Dark and Light mode.
 */
public class SeatMapPanel extends JPanel implements ThemeManager.ThemeChangeListener {

    public interface SeatSelectionListener {
        void onSeatSelectionChanged(List<Integer> selectedSeats);
    }

    private int totalSeats = 40;
    private Map<Integer, Seat> seatMap = new HashMap<>();
    private final Set<Integer> selectedSeats = new TreeSet<>();
    private SeatSelectionListener listener;

    // Dimensions
    private static final int SEAT_WIDTH = 48;
    private static final int SEAT_HEIGHT = 44;
    private static final int HORIZONTAL_GAP = 12;
    private static final int AISLE_WIDTH = 34;
    private static final int VERTICAL_GAP = 14;
    private static final int BUS_PADDING_X = 28;
    private static final int BUS_PADDING_TOP = 85; // space for windshield & driver
    private static final int BUS_PADDING_BOTTOM = 35;

    // Palettes
    public static final Color COLOR_AVAILABLE = new Color(46, 204, 113);
    public static final Color COLOR_SELECTED = new Color(41, 128, 185);
    public static final Color COLOR_MALE = new Color(30, 136, 229);
    public static final Color COLOR_FEMALE = new Color(233, 30, 99);
    public static final Color COLOR_OTHERS = new Color(142, 36, 170);
    public static final Color COLOR_PREF_GLOW = new Color(243, 156, 18);
    public static final Color COLOR_BUS_BODY = new Color(240, 243, 246);
    public static final Color COLOR_BUS_BORDER = new Color(189, 195, 199);
    public static final Color COLOR_AISLE = new Color(225, 230, 235);

    private int hoveredSeat = -1;

    public SeatMapPanel() {
        setBackground(ThemeManager.getInstance().getCardBg());
        ThemeManager.getInstance().addListener(this);

        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setInitialDelay(150);
        ToolTipManager.sharedInstance().setDismissDelay(6000);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int seatNum = getSeatAt(e.getX(), e.getY());
                if (seatNum != hoveredSeat) {
                    hoveredSeat = seatNum;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredSeat != -1) {
                    hoveredSeat = -1;
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int seatNum = getSeatAt(e.getX(), e.getY());
                if (seatNum != -1) {
                    toggleSeat(seatNum);
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        updatePreferredSize();
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        setBackground(ThemeManager.getInstance().getCardBg());
        repaint();
    }

    public void setSeatSelectionListener(SeatSelectionListener l) {
        this.listener = l;
    }

    public void setSeats(int totalSeats, Map<Integer, Seat> map) {
        this.totalSeats = Math.max(1, totalSeats);
        this.seatMap = (map != null) ? map : new HashMap<>();
        selectedSeats.removeIf(num -> num > totalSeats || (seatMap.containsKey(num) && seatMap.get(num).isBooked()));
        updatePreferredSize();
        repaint();
    }

    public void clearSelection() {
        selectedSeats.clear();
        repaint();
        if (listener != null) {
            listener.onSeatSelectionChanged(new ArrayList<>());
        }
    }

    public List<Integer> getSelectedSeats() {
        return new ArrayList<>(selectedSeats);
    }

    private void toggleSeat(int seatNum) {
        Seat s = seatMap.get(seatNum);
        if (s == null || s.isBooked()) {
            return; // Cannot select booked seats
        }

        if (selectedSeats.contains(seatNum)) {
            selectedSeats.remove(seatNum);
        } else {
            if (selectedSeats.size() >= 6) {
                JOptionPane.showMessageDialog(this,
                        "You can book a maximum of 6 seats in a single reservation.",
                        "Seat Limit Exceeded", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedSeats.add(seatNum);
        }
        repaint();
        if (listener != null) {
            listener.onSeatSelectionChanged(new ArrayList<>(selectedSeats));
        }
    }

    private void updatePreferredSize() {
        int rows = (int) Math.ceil((double) totalSeats / 4.0);
        int totalWidth = BUS_PADDING_X * 2 + (SEAT_WIDTH * 4) + (HORIZONTAL_GAP * 2) + AISLE_WIDTH;
        int totalHeight = BUS_PADDING_TOP + (rows * (SEAT_HEIGHT + VERTICAL_GAP)) + BUS_PADDING_BOTTOM;
        setPreferredSize(new Dimension(totalWidth + 20, totalHeight + 10));
        revalidate();
    }

    private Rectangle getSeatBounds(int seatNum) {
        int index = seatNum - 1;
        int row = index / 4;
        int col = index % 4; // 0, 1 (left side) | 2, 3 (right side)

        int x = BUS_PADDING_X + 10;
        if (col == 0) {
            x += 0;
        } else if (col == 1) {
            x += SEAT_WIDTH + HORIZONTAL_GAP;
        } else if (col == 2) {
            x += (SEAT_WIDTH * 2) + HORIZONTAL_GAP + AISLE_WIDTH;
        } else {
            x += (SEAT_WIDTH * 3) + (HORIZONTAL_GAP * 2) + AISLE_WIDTH;
        }

        int y = BUS_PADDING_TOP + (row * (SEAT_HEIGHT + VERTICAL_GAP));
        return new Rectangle(x, y, SEAT_WIDTH, SEAT_HEIGHT);
    }

    private int getSeatAt(int mouseX, int mouseY) {
        for (int i = 1; i <= totalSeats; i++) {
            Rectangle r = getSeatBounds(i);
            if (r.contains(mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int seatNum = getSeatAt(e.getX(), e.getY());
        if (seatNum != -1) {
            Seat s = seatMap.get(seatNum);
            if (s != null) {
                return SeatService.getSeatTooltip(s);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        ThemeManager tm = ThemeManager.getInstance();
        boolean dark = tm.isDarkMode();
        Color busBodyColor = dark ? new Color(26, 36, 52) : COLOR_BUS_BODY;
        Color busBorderColor = dark ? new Color(71, 85, 105) : COLOR_BUS_BORDER;
        Color aisleColor = dark ? new Color(15, 23, 42) : COLOR_AISLE;

        int rows = (int) Math.ceil((double) totalSeats / 4.0);
        int busW = (SEAT_WIDTH * 4) + (HORIZONTAL_GAP * 2) + AISLE_WIDTH + 40;
        int busH = BUS_PADDING_TOP + (rows * (SEAT_HEIGHT + VERTICAL_GAP)) + BUS_PADDING_BOTTOM - 10;
        int busX = BUS_PADDING_X - 10;
        int busY = 15;

        // 1. Bus Exterior Body
        RoundRectangle2D busBody = new RoundRectangle2D.Double(busX, busY, busW, busH, 40, 40);
        g2.setColor(busBodyColor);
        g2.fill(busBody);
        g2.setColor(busBorderColor);
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(busBody);

        // 2. Front Windshield & Dashboard
        RoundRectangle2D windshield = new RoundRectangle2D.Double(busX + 25, busY + 12, busW - 50, 22, 12, 12);
        g2.setColor(dark ? new Color(30, 58, 88) : new Color(200, 230, 245));
        g2.fill(windshield);
        g2.setColor(dark ? new Color(56, 189, 248) : new Color(120, 180, 215));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(windshield);

        // Windshield reflection
        g2.setColor(new Color(255, 255, 255, dark ? 40 : 120));
        g2.fillRoundRect(busX + 35, busY + 15, (busW - 70) / 2, 8, 4, 4);

        // Driver Cabin & Steering Wheel (Left side)
        int driverX = busX + 32;
        int driverY = busY + 44;
        g2.setColor(dark ? new Color(51, 65, 85) : new Color(80, 90, 100));
        g2.fillOval(driverX, driverY, 26, 26);
        g2.setColor(dark ? new Color(148, 163, 184) : Color.WHITE);
        g2.fillOval(driverX + 7, driverY + 7, 12, 12);
        g2.setColor(dark ? new Color(148, 163, 184) : new Color(60, 70, 80));
        g2.drawString("Driver", driverX - 2, driverY + 36);

        // Entrance Door Indicator (Right side)
        int doorX = busX + busW - 60;
        int doorY = busY + 42;
        g2.setColor(new Color(41, 128, 185));
        g2.fillRoundRect(doorX, doorY, 32, 16, 4, 4);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.drawString("DOOR", doorX + 3, doorY + 12);

        // 3. Central Walking Aisle
        int aisleX = BUS_PADDING_X + 10 + (SEAT_WIDTH * 2) + HORIZONTAL_GAP;
        int aisleY = BUS_PADDING_TOP - 8;
        int aisleH = rows * (SEAT_HEIGHT + VERTICAL_GAP) + 12;
        g2.setColor(aisleColor);
        g2.fillRoundRect(aisleX + 2, aisleY, AISLE_WIDTH - 4, aisleH, 6, 6);

        // Aisle path arrows
        g2.setColor(dark ? new Color(71, 85, 105) : new Color(189, 195, 199));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int r = 0; r < rows; r += 2) {
            int arrowY = BUS_PADDING_TOP + (r * (SEAT_HEIGHT + VERTICAL_GAP)) + 24;
            g2.drawString("▼", aisleX + 11, arrowY);
        }

        // 4. Render All Seats
        for (int i = 1; i <= totalSeats; i++) {
            paintSeat(g2, i);
        }

        g2.dispose();
    }

    private void paintSeat(Graphics2D g2, int seatNum) {
        Rectangle r = getSeatBounds(seatNum);
        Seat s = seatMap.get(seatNum);
        boolean isSelected = selectedSeats.contains(seatNum);
        boolean isHovered = (seatNum == hoveredSeat);

        Color seatColor = COLOR_AVAILABLE;
        if (isSelected) {
            seatColor = COLOR_SELECTED;
        } else if (s != null && s.isBooked()) {
            String g = s.getPassengerGender();
            if ("FEMALE".equalsIgnoreCase(g)) {
                seatColor = COLOR_FEMALE;
            } else if ("OTHERS".equalsIgnoreCase(g)) {
                seatColor = COLOR_OTHERS;
            } else {
                seatColor = COLOR_MALE; // Default Male
            }
        }

        // Glowing border for preferred seat match
        if (s != null && s.isAvailable() && s.isPreferenceMatch() && !isSelected) {
            g2.setColor(COLOR_PREF_GLOW);
            g2.setStroke(new BasicStroke(3.0f));
            g2.drawRoundRect(r.x - 3, r.y - 3, r.width + 6, r.height + 6, 12, 12);
        }

        // Seat Main Cushion
        RoundRectangle2D cushion = new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10);
        g2.setColor(seatColor);
        g2.fill(cushion);

        // Seat Border
        if (isHovered) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f));
        } else {
            g2.setColor(seatColor.darker());
            g2.setStroke(new BasicStroke(1.2f));
        }
        g2.draw(cushion);

        // Top Headrest
        g2.setColor(seatColor.brighter());
        g2.fillRoundRect(r.x + 6, r.y + 4, r.width - 12, 10, 4, 4);

        // Armrests
        g2.setColor(seatColor.darker());
        g2.fillRoundRect(r.x + 2, r.y + 16, 4, 20, 2, 2);
        g2.fillRoundRect(r.x + r.width - 6, r.y + 16, 4, 20, 2, 2);

        // Seat Number Text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String text = String.valueOf(seatNum);
        FontMetrics fm = g2.getFontMetrics();
        int textX = r.x + (r.width - fm.stringWidth(text)) / 2;
        int textY = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2 + 3;
        g2.drawString(text, textX, textY);

        // Concession tag icon/indicator
        if (s != null && s.isConcession()) {
            g2.setColor(new Color(241, 196, 15));
            g2.fillOval(r.x + r.width - 12, r.y + 3, 7, 7);
        }
    }

    /**
     * Builds a comprehensive UI Legend panel explaining the color codes.
     */
    public static JPanel createLegendPanel() {
        ThemeManager tm = ThemeManager.getInstance();
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        legend.setBackground(tm.getTableAlternateRow());
        legend.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, tm.getCardBorder()),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        legend.add(createLegendItem(COLOR_AVAILABLE, "Available"));
        legend.add(createLegendItem(COLOR_SELECTED, "Selected"));
        legend.add(createLegendItem(COLOR_MALE, "Booked (Male)"));
        legend.add(createLegendItem(COLOR_FEMALE, "Booked (Female)"));
        legend.add(createLegendItem(COLOR_OTHERS, "Booked (Others)"));
        legend.add(createLegendItem(COLOR_PREF_GLOW, "★ Pref Match"));

        return legend;
    }

    private static JPanel createLegendItem(Color color, String label) {
        ThemeManager tm = ThemeManager.getInstance();
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);

        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 14, 14, 4, 4);
                g2.setColor(color.darker());
                g2.drawRoundRect(0, 0, 13, 13, 4, 4);
            }
        };
        box.setPreferredSize(new Dimension(14, 14));
        box.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(tm.getTextPrimary());

        item.add(box);
        item.add(lbl);
        return item;
    }
}
