import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * MarsMap3D - A standalone, single-file Java Swing application 
 * featuring interactive 3D Mars rendering, coordinate conversion, 
 * landmark management (add/delete), and integrated NASA research resources.
 */
public class MarsMap extends JFrame {

    // Canvas dimensions
    private static final int WIDTH = 950;
    private static final int HEIGHT = 750;

    // 3D Engine state variables
    private double angleX = 0.5;
    private double angleY = 0.0;
    private int lastMouseX, lastMouseY;
    private double globeRadius = 200.0;
    private Timer renderTimer;

    // Landmark representation list & UI Dropdown Model
    private final List<MarsLandmark> landmarks = new ArrayList<>();
    private final DefaultComboBoxModel<MarsLandmark> landmarkComboModel = new DefaultComboBoxModel<>();

    public MarsMap() {
        setTitle("Interactive 3D Mars Map & Landmark Manager");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Pre-load key NASA mission sites from your research document
        addLandmark(new MarsLandmark("Olympus Mons", 18.65, 226.2, Color.RED));
        addLandmark(new MarsLandmark("Jezero Crater (Perseverance)", 18.38, 77.58, Color.CYAN));
        addLandmark(new MarsLandmark("Gale Crater (Curiosity)", -5.4, 137.4, Color.ORANGE));

        // Create interactive rendering panel FIRST
        MarsCanvas canvas = new MarsCanvas();
        add(canvas, BorderLayout.CENTER);

        // --- TOP PANEL: Add Landmark with Name, Latitude, and Longitude ---
        JPanel addPanel = new JPanel();
        addPanel.setBackground(new Color(25, 25, 35));
        addPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 8));

        JTextField nameField = new JTextField(12);
        nameField.setText("New Site");
        JTextField latField = new JTextField(5);
        latField.setText("0.0");
        JTextField lonField = new JTextField(5);
        lonField.setText("0.0");

        JButton addButton = new JButton("Add Landmark");

        addPanel.add(new JLabel("<html><font color='white'>Name:</font></html>"));
        addPanel.add(nameField);
        addPanel.add(new JLabel("<html><font color='white'>Lat (-90 to 90):</font></html>"));
        addPanel.add(latField);
        addPanel.add(new JLabel("<html><font color='white'>Lon (0 to 360):</font></html>"));
        addPanel.add(lonField);
        addPanel.add(addButton);

        add(addPanel, BorderLayout.NORTH);

        // --- BOTTOM PANEL: Management, Deletion & NASA Resources ---
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(20, 20, 25));
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JComboBox<MarsLandmark> deleteComboBox = new JComboBox<>(landmarkComboModel);
        deleteComboBox.setPreferredSize(new Dimension(210, 25));
        JButton deleteButton = new JButton("Delete Selected");
        JButton resourcesButton = new JButton("NASA Data & Resources");
        JButton resetButton = new JButton("Reset View");

        controlPanel.add(new JLabel("<html><font color='lightgray'>Manage:</font></html>"));
        controlPanel.add(deleteComboBox);
        controlPanel.add(deleteButton);
        controlPanel.add(resourcesButton);
        controlPanel.add(resetButton);

        add(controlPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---

        // 1. Add Landmark Logic (Name + Coordinates)
        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double lat = Double.parseDouble(latField.getText().trim());
                double lon = Double.parseDouble(lonField.getText().trim());

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid landmark name.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (lat < -90 || lat > 90) {
                    JOptionPane.showMessageDialog(this, "Latitude must be between -90 and 90 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Color randomColor = new Color((int)(Math.random()*200) + 55, (int)(Math.random()*200) + 55, (int)(Math.random()*200) + 55);
                MarsLandmark newLandmark = new MarsLandmark(name, lat, lon, randomColor);
                
                addLandmark(newLandmark);
                canvas.repaint();

                nameField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Latitude and Longitude must be valid numbers.", "Format Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 2. Delete Landmark Logic
        deleteButton.addActionListener(e -> {
            MarsLandmark selected = (MarsLandmark) deleteComboBox.getSelectedItem();
            if (selected != null) {
                landmarks.remove(selected);
                landmarkComboModel.removeElement(selected);
                canvas.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "No landmark selected to delete.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // 3. Open NASA Research & Resources Dialog
        resourcesButton.addActionListener(e -> showResourcesDialog());

        // 4. Reset View Logic
        resetButton.addActionListener(e -> {
            angleX = 0.5;
            angleY = 0.0;
            globeRadius = 200.0;
            canvas.repaint();
        });

        // Animation loop for smooth updates
        renderTimer = new Timer(30, e -> canvas.repaint());
        renderTimer.start();
    }

    private void addLandmark(MarsLandmark lm) {
        landmarks.add(lm);
        landmarkComboModel.addElement(lm);
    }

    /**
     * Displays a popup window containing the NASA Space Apps research datasets and maps[cite: 2].
     */
    private void showResourcesDialog() {
        JDialog dialog = new JDialog(this, "NASA Space Apps - Mars Research & Datasets", true);
        dialog.setSize(650, 450);
        dialog.setLocationRelativeTo(this);

        JEditorPane infoPane = new JEditorPane();
        infoPane.setContentType("text/html");
        infoPane.setEditable(false);
        infoPane.setText(
            "<html><body style='font-family:sans-serif; padding:10px; background-color:#f4f4f4;'>" +
            "<h2>Mars Mapping & Research Sources</h2>" +
            "<p>Integrated from your research document for the NASA Space Apps Challenge:</p>" +
            "<ul>" +
            "<li><b>USGS-NASA Planetary Geologic Mapping Program:</b> Provides morphologic, topographic, and stratigraphic datasets[cite: 2].</li>" +
            "<li><b>JPL Mars Trek:</b> Interactive mapping and visualization portal (<a href='https://trek.nasa.gov/mars/'>Link</a>)[cite: 2].</li>" +
            "<li><b>AI4Mars Dataset:</b> Terrain-aware autonomous driving datasets for Mars rovers[cite: 2].</li>" +
            "<li><b>MGS TES Thermal Inertia:</b> Thermal Emission Spectrometer data measuring rock abundance, particle size, and subsurface heat storage[cite: 2].</li>" +
            "<li><b>Weather & Seasonal Cycles:</b> Curiosity rover meteorology reports and thermal tide observations[cite: 2].</li>" +
            "</ul>" +
            "<p><i>Tip: You can use the top input fields to add your own custom coordinates based on these datasets!</i></p>" +
            "</body></html>"
        );

        dialog.add(new JScrollPane(infoPane), BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(ev -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeButton);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Data structure for geographical locations on Mars.
     */
    public static class MarsLandmark {
        String name;
        double latitude;  // Degrees (-90 to 90)
        double longitude; // Degrees (0 to 360)
        Color markerColor;

        public MarsLandmark(String name, double lat, double lon, Color color) {
            this.name = name;
            this.latitude = lat;
            this.longitude = lon;
            this.markerColor = color;
        }

        @Override
        public String toString() {
            return name + " (" + latitude + "°, " + longitude + "°)";
        }

        /**
         * Converts lat/lon to 3D Cartesian coordinates (Vector3 equivalent)
         */
        public Vector3D toCartesian(double radius) {
            double latRad = Math.toRadians(latitude);
            double lonRad = Math.toRadians(longitude);

            double x = radius * Math.cos(latRad) * Math.cos(lonRad);
            double y = -radius * Math.sin(latRad); // Inverted Y for screen coordinates
            double z = radius * Math.cos(latRad) * Math.sin(lonRad);

            return new Vector3D(x, y, z);
        }
    }

    /**
     * Simple 3D Vector representation.
     */
    public static class Vector3D {
        public double x, y, z;

        public Vector3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Rotates the point around X and Y axes for camera projection.
         */
        public Vector3D rotate(double ax, double ay) {
            double x1 = x * Math.cos(ay) + z * Math.sin(ay);
            double y1 = y;
            double z1 = -x * Math.sin(ay) + z * Math.cos(ay);

            double x2 = x1;
            double y2 = y1 * Math.cos(ax) - z1 * Math.sin(ax);
            double z2 = y1 * Math.sin(ax) + z1 * Math.cos(ax);

            return new Vector3D(x2, y2, z2);
        }
    }

    /**
     * Custom Canvas panel to handle 3D projection rendering.
     */
    private class MarsCanvas extends JPanel {

        public MarsCanvas() {
            setBackground(new Color(10, 10, 15));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;

                    angleY += dx * 0.01;
                    angleX += dy * 0.01;

                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                }
            });

            addMouseWheelListener(e -> {
                globeRadius -= e.getWheelRotation() * 10;
                if (globeRadius < 80) globeRadius = 80;
                if (globeRadius > 450) globeRadius = 450;
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            // Draw procedural latitude/longitude bands
            g2d.setColor(new Color(180, 75, 45, 40));
            for (int lat = -60; lat <= 60; lat += 30) {
                drawLatitudeRing(g2d, centerX, centerY, lat);
            }
            for (int lon = 0; lon < 360; lon += 45) {
                drawLongitudeLine(g2d, centerX, centerY, lon);
            }

            // Draw Mars Body (Shaded Sphere)
            GradientPaint marsGradient = new GradientPaint(
                    centerX - (float)globeRadius, centerY - (float)globeRadius, new Color(220, 95, 50),
                    centerX + (float)globeRadius, centerY + (float)globeRadius, new Color(110, 35, 15)
            );
            g2d.setPaint(marsGradient);
            g2d.fillOval(centerX - (int)globeRadius, centerY - (int)globeRadius, (int)(globeRadius * 2), (int)(globeRadius * 2));

            g2d.setColor(new Color(255, 120, 70));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(centerX - (int)globeRadius, centerY - (int)globeRadius, (int)(globeRadius * 2), (int)(globeRadius * 2));

            // Render Landmarks and Markers
            for (MarsLandmark landmark : landmarks) {
                Vector3D localPos = landmark.toCartesian(globeRadius);
                Vector3D rotatedPos = localPos.rotate(angleX, angleY);

                if (rotatedPos.z > 0) {
                    int screenX = centerX + (int) rotatedPos.x;
                    int screenY = centerY + (int) rotatedPos.y;

                    g2d.setColor(landmark.markerColor);
                    g2d.fillOval(screenX - 5, screenY - 5, 10, 10);
                    g2d.setColor(Color.WHITE);
                    g2d.drawOval(screenX - 5, screenY - 5, 10, 10);

                    g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
                    g2d.drawString(landmark.name, screenX + 8, screenY + 4);
                }
            }

            // Atmospheric Rim Glow
            g2d.setColor(new Color(255, 160, 100, 30));
            g2d.setStroke(new BasicStroke(6));
            g2d.drawOval(centerX - (int)globeRadius - 3, centerY - (int)globeRadius - 3, (int)(globeRadius * 2) + 6, (int)(globeRadius * 2) + 6);
        }

        private void drawLatitudeRing(Graphics2D g2d, int cx, int cy, double lat) {
            double latRad = Math.toRadians(lat);
            double rRing = globeRadius * Math.cos(latRad);
            double yOffset = -globeRadius * Math.sin(latRad);

            Vector3D sample = new Vector3D(0, yOffset, rRing).rotate(angleX, angleY);
            int ringRadius = (int) Math.abs(rRing * Math.cos(angleY));
            if (ringRadius > 0) {
                g2d.drawOval(cx - ringRadius, cy + (int)sample.y - 5, ringRadius * 2, 10);
            }
        }

        private void drawLongitudeLine(Graphics2D g2d, int cx, int cy, double lon) {
            Path2D path = new Path2D.Double();
            boolean first = true;
            for (double lat = -90; lat <= 90; lat += 10) {
                MarsLandmark lm = new MarsLandmark("", lat, lon, Color.WHITE);
                Vector3D p = lm.toCartesian(globeRadius).rotate(angleX, angleY);
                if (p.z > -globeRadius) {
                    int sx = cx + (int)p.x;
                    int sy = cy + (int)p.y;
                    if (first) {
                        path.moveTo(sx, sy);
                        first = false;
                    } else {
                        path.lineTo(sx, sy);
                    }
                }
            }
            g2d.draw(path);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MarsMap().setVisible(true));
    }
}