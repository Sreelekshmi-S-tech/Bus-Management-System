package util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Universal resolution-independent vector icons for Bus Reservation System.
 * Renders anti-aliased mathematical shapes with custom color, size, and scaling.
 * Completely eliminates OS font dependency, missing glyphs, and tofu boxes (▯).
 */
public class AppIcon implements Icon {

    public enum Type {
        BUS,
        BUS_FRONT,
        USER,
        USERS,
        TICKET,
        SEAT,
        CLOCK,
        MOON,
        SUN,
        REFRESH,
        LOGOUT,
        LOCK,
        SEARCH,
        ARROW_RIGHT,
        PLUS,
        TRASH,
        CHECK,
        PRINT
    }

    private final Type type;
    private final int size;
    private Color color;

    public AppIcon(Type type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    public static AppIcon of(Type type, int size, Color color) {
        return new AppIcon(type, size, color);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);

        double s = size / 24.0; // Normalized to 24x24 coordinate grid
        g2.scale(s, s);

        Color paintColor = (color != null) ? color : (c != null ? c.getForeground() : Color.BLACK);
        g2.setColor(paintColor);

        switch (type) {
            case BUS:
                paintBus(g2);
                break;
            case BUS_FRONT:
                paintBusFront(g2);
                break;
            case USER:
                paintUser(g2);
                break;
            case USERS:
                paintUsers(g2);
                break;
            case TICKET:
                paintTicket(g2);
                break;
            case SEAT:
                paintSeat(g2);
                break;
            case CLOCK:
                paintClock(g2);
                break;
            case MOON:
                paintMoon(g2);
                break;
            case SUN:
                paintSun(g2);
                break;
            case REFRESH:
                paintRefresh(g2);
                break;
            case LOGOUT:
                paintLogout(g2);
                break;
            case LOCK:
                paintLock(g2);
                break;
            case SEARCH:
                paintSearch(g2);
                break;
            case ARROW_RIGHT:
                paintArrowRight(g2);
                break;
            case PLUS:
                paintPlus(g2);
                break;
            case TRASH:
                paintTrash(g2);
                break;
            case CHECK:
                paintCheck(g2);
                break;
            case PRINT:
                paintPrint(g2);
                break;
        }

        g2.dispose();
    }

    private void paintBus(Graphics2D g) {
        Color orig = g.getColor();
        // Bus body silhouette
        g.fill(new RoundRectangle2D.Double(2, 4, 20, 14, 5, 5));
        
        // Windows (dark semi-transparent cutouts)
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new RoundRectangle2D.Double(4, 6.5, 4.5, 4.5, 2, 2));
        g.fill(new RoundRectangle2D.Double(9.7, 6.5, 4.5, 4.5, 2, 2));
        g.fill(new RoundRectangle2D.Double(15.5, 6.5, 4.5, 4.5, 2, 2));

        // Wheels
        g.setColor(new Color(30, 40, 55));
        g.fill(new Ellipse2D.Double(5.5, 16.5, 4, 4));
        g.fill(new Ellipse2D.Double(14.5, 16.5, 4, 4));
        
        // Inner wheel hubs
        g.setColor(orig);
        g.fill(new Ellipse2D.Double(6.8, 17.8, 1.4, 1.4));
        g.fill(new Ellipse2D.Double(15.8, 17.8, 1.4, 1.4));
    }

    private void paintBusFront(Graphics2D g) {
        // Modern Tour Bus Front View
        g.fill(new RoundRectangle2D.Double(4, 3, 16, 17, 4, 4));

        Color orig = g.getColor();
        // Windshield
        g.setColor(new Color(255, 255, 255, 180));
        g.fill(new RoundRectangle2D.Double(6, 6, 12, 6, 2, 2));

        // Destination board
        g.fill(new RoundRectangle2D.Double(7.5, 4, 9, 1.5, 1, 1));

        // Dual Headlights
        g.setColor(new Color(255, 235, 59));
        g.fill(new Ellipse2D.Double(5.5, 15, 2.5, 2.5));
        g.fill(new Ellipse2D.Double(16, 15, 2.5, 2.5));

        // Bumper grill
        g.setColor(new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), 120));
        g.fill(new RoundRectangle2D.Double(9, 15.5, 6, 2, 1, 1));

        // Rearview mirrors
        g.setColor(orig);
        g.fill(new RoundRectangle2D.Double(2, 7, 1.8, 3.5, 1, 1));
        g.fill(new RoundRectangle2D.Double(20.2, 7, 1.8, 3.5, 1, 1));

        // Tires under chassis
        g.fill(new RoundRectangle2D.Double(5.5, 19.5, 3, 2, 1, 1));
        g.fill(new RoundRectangle2D.Double(15.5, 19.5, 3, 2, 1, 1));
    }

    private void paintUser(Graphics2D g) {
        // Head
        g.fill(new Ellipse2D.Double(7.5, 2.5, 9, 9));

        // Shoulders / Torso
        Path2D torso = new Path2D.Double();
        torso.moveTo(3, 21.5);
        torso.curveTo(3, 15.5, 7, 13.5, 12, 13.5);
        torso.curveTo(17, 13.5, 21, 15.5, 21, 21.5);
        torso.closePath();
        g.fill(torso);
    }

    private void paintUsers(Graphics2D g) {
        Color orig = g.getColor();

        // Background user (offset slightly left & up)
        g.setColor(new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), 140));
        g.fill(new Ellipse2D.Double(5, 3, 6.5, 6.5));
        Path2D bkgTorso = new Path2D.Double();
        bkgTorso.moveTo(1.5, 18);
        bkgTorso.curveTo(1.5, 13.5, 4.5, 11.5, 8.2, 11.5);
        bkgTorso.curveTo(12, 11.5, 14.5, 13.5, 14.5, 18);
        bkgTorso.closePath();
        g.fill(bkgTorso);

        // Foreground user (offset right & down)
        g.setColor(orig);
        g.fill(new Ellipse2D.Double(12, 5.5, 7.5, 7.5));
        Path2D fgTorso = new Path2D.Double();
        fgTorso.moveTo(8, 21.5);
        fgTorso.curveTo(8, 16.5, 11.5, 14.5, 15.5, 14.5);
        fgTorso.curveTo(19.5, 14.5, 23, 16.5, 23, 21.5);
        fgTorso.closePath();
        g.fill(fgTorso);
    }

    private void paintTicket(Graphics2D g) {
        // Boarding pass coupon with notch cutouts
        Path2D ticket = new Path2D.Double();
        ticket.moveTo(3, 5);
        ticket.lineTo(16, 5);
        ticket.quadTo(16, 7.5, 18, 7.5);
        ticket.quadTo(20, 7.5, 20, 5);
        ticket.lineTo(21, 5);
        ticket.quadTo(22, 5, 22, 6);
        ticket.lineTo(22, 18);
        ticket.quadTo(22, 19, 21, 19);
        ticket.lineTo(20, 19);
        ticket.quadTo(20, 16.5, 18, 16.5);
        ticket.quadTo(16, 16.5, 16, 19);
        ticket.lineTo(3, 19);
        ticket.quadTo(2, 19, 2, 18);
        ticket.lineTo(2, 6);
        ticket.quadTo(2, 5, 3, 5);
        ticket.closePath();
        g.fill(ticket);

        // Perforation dashed line
        Color orig = g.getColor();
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{2, 2}, 0));
        g.draw(new Line2D.Double(15, 6, 15, 18));

        // Passenger silhouette or barcode lines on the main body
        g.fill(new RoundRectangle2D.Double(5, 8, 7, 2, 1, 1));
        g.fill(new RoundRectangle2D.Double(5, 11.5, 5, 1.5, 0.7, 0.7));
        g.fill(new RoundRectangle2D.Double(5, 14.5, 8, 1.5, 0.7, 0.7));
    }

    private void paintSeat(Graphics2D g) {
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Ergonomic Seat profile
        Path2D seat = new Path2D.Double();
        // Headrest
        g.fill(new RoundRectangle2D.Double(6.5, 2.5, 8, 4, 2, 2));

        // Backrest to cushion path
        seat.moveTo(8.5, 7);
        seat.curveTo(8.5, 12, 8.5, 15, 8.5, 16);
        seat.curveTo(8.5, 17, 9.5, 17.5, 11, 17.5);
        seat.lineTo(17.5, 17.5);
        seat.curveTo(19, 17.5, 19.5, 17, 19.5, 15.5);
        g.draw(seat);

        // Armrest
        Path2D arm = new Path2D.Double();
        arm.moveTo(11, 13);
        arm.lineTo(16, 13);
        arm.lineTo(16, 16);
        g.draw(arm);

        // Pedestal base
        g.draw(new Line2D.Double(13, 18, 13, 21.5));
        g.draw(new Line2D.Double(9, 21.5, 17, 21.5));
    }

    private void paintClock(Graphics2D g) {
        // Clock rim
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new Ellipse2D.Double(3, 3, 18, 18));

        // Center dot
        g.fill(new Ellipse2D.Double(11, 11, 2, 2));

        // Hands (hour pointing up-right, minute pointing right)
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(12, 12, 12, 7)); // Hour hand (12 o'clock)
        g.draw(new Line2D.Double(12, 12, 16.5, 12)); // Minute hand (3 o'clock)
    }

    private void paintMoon(Graphics2D g) {
        // Crescent Moon for Dark Mode
        Area moon = new Area(new Ellipse2D.Double(3.5, 3.5, 17, 17));
        Area cutout = new Area(new Ellipse2D.Double(7.5, 2.5, 16, 16));
        moon.subtract(cutout);
        g.fill(moon);

        // Subtle stars around moon
        g.fill(new Ellipse2D.Double(17, 5, 1.8, 1.8));
        g.fill(new Ellipse2D.Double(19, 11, 1.4, 1.4));
    }

    private void paintSun(Graphics2D g) {
        // Sun center
        g.fill(new Ellipse2D.Double(7, 7, 10, 10));

        // Sun rays
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(12, 2, 12, 4.5));
        g.draw(new Line2D.Double(12, 19.5, 12, 22));
        g.draw(new Line2D.Double(2, 12, 4.5, 12));
        g.draw(new Line2D.Double(19.5, 12, 22, 12));
        g.draw(new Line2D.Double(4.9, 4.9, 6.7, 6.7));
        g.draw(new Line2D.Double(17.3, 17.3, 19.1, 19.1));
        g.draw(new Line2D.Double(4.9, 19.1, 6.7, 17.3));
        g.draw(new Line2D.Double(17.3, 6.7, 19.1, 4.9));
    }

    private void paintRefresh(Graphics2D g) {
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Circular arc
        Arc2D arc = new Arc2D.Double(3.5, 3.5, 17, 17, 45, 270, Arc2D.OPEN);
        g.draw(arc);

        // Arrow head at arc end
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(15.5, 3.5);
        arrow.lineTo(19.5, 7.5);
        arrow.lineTo(15.5, 10.5);
        g.draw(arrow);
    }

    private void paintLogout(Graphics2D g) {
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Door frame
        Path2D door = new Path2D.Double();
        door.moveTo(13, 4);
        door.lineTo(4, 4);
        door.lineTo(4, 20);
        door.lineTo(13, 20);
        g.draw(door);

        // Exit arrow pointing right
        g.draw(new Line2D.Double(9, 12, 20.5, 12));
        Path2D head = new Path2D.Double();
        head.moveTo(16.5, 8);
        head.lineTo(20.5, 12);
        head.lineTo(16.5, 16);
        g.draw(head);
    }

    private void paintLock(Graphics2D g) {
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Shackle arch
        Path2D shackle = new Path2D.Double();
        shackle.moveTo(7, 10);
        shackle.lineTo(7, 6.5);
        shackle.curveTo(7, 3.5, 17, 3.5, 17, 6.5);
        shackle.lineTo(17, 10);
        g.draw(shackle);

        // Lock body
        g.fill(new RoundRectangle2D.Double(5, 10, 14, 11, 3, 3));

        // Keyhole
        Color orig = g.getColor();
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(11, 12.5, 2, 2));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Line2D.Double(12, 14, 12, 17));
    }

    private void paintSearch(Graphics2D g) {
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Double(3.5, 3.5, 12, 12));
        g.draw(new Line2D.Double(13, 13, 20.5, 20.5));
    }

    private void paintArrowRight(Graphics2D g) {
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(4, 12, 18.5, 12));
        Path2D head = new Path2D.Double();
        head.moveTo(13, 6.5);
        head.lineTo(18.5, 12);
        head.lineTo(13, 17.5);
        g.draw(head);
    }

    private void paintPlus(Graphics2D g) {
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(12, 4.5, 12, 19.5));
        g.draw(new Line2D.Double(4.5, 12, 19.5, 12));
    }

    private void paintTrash(Graphics2D g) {
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Lid
        g.draw(new Line2D.Double(4, 7, 20, 7));
        g.draw(new RoundRectangle2D.Double(9, 4, 6, 3, 1, 1));
        // Bin
        Path2D bin = new Path2D.Double();
        bin.moveTo(6, 7);
        bin.lineTo(7, 20);
        bin.lineTo(17, 20);
        bin.lineTo(18, 7);
        g.draw(bin);
        // Ribs
        g.draw(new Line2D.Double(10, 10, 10, 17));
        g.draw(new Line2D.Double(14, 10, 14, 17));
    }

    private void paintCheck(Graphics2D g) {
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D check = new Path2D.Double();
        check.moveTo(4, 12.5);
        check.lineTo(9.5, 17.5);
        check.lineTo(20, 6.5);
        g.draw(check);
    }

    private void paintPrint(Graphics2D g) {
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Top paper
        g.draw(new RoundRectangle2D.Double(7, 3, 10, 6, 1, 1));
        // Printer body
        g.fill(new RoundRectangle2D.Double(4, 8, 16, 9, 3, 3));
        // Output tray
        Color orig = g.getColor();
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(7, 13, 10, 6, 1, 1));
        g.setColor(orig);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Line2D.Double(9, 15, 15, 15));
        g.draw(new Line2D.Double(9, 17, 13, 17));
    }
}
