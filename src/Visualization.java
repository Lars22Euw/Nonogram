import javax.swing.*;
import java.awt.*;

public class Visualization extends JPanel {
    public Visualization(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public Visualization() {
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw Text
        g.setColor(Color.BLACK);
        final int drawnBoxSize = 500;
        g.drawRect(10, 10, drawnBoxSize, drawnBoxSize);
        g.drawString("Hi Lars", 300, 300);
    }

}
