import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class Main extends JPanel {

    private final Nonogram nono;
    private MouseHandler mouseHandler = new MouseHandler();
    private Point p1 = new Point(100, 100);
    private Point p2 = new Point(540, 380);
    private boolean drawing;

    public Main() {
        this.setPreferredSize(new Dimension(900, 900));
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.nono = new Nonogram(10);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.blue);
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(5,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        drawNonogram(g);
    }

    private void drawNonogram(Graphics g) {
        g.setColor(Color.black);
        Font myFont = new Font("Courier New", Font.BOLD, 20);
        g.setFont (myFont);
        var size = nono.columns.length;
        int low = 200;
        int high = 850;
        int step = (high - low) / size;
        drawMatrix(g, size, low, step, Color.lightGray, nono.solution);
        drawMatrix(g, size, low, step, Color.blue, nono.guess);
        g.setColor(Color.black);
        for (var i = 0; i <= size; i++) {
            int offset = low + i * step;
            g.drawLine(low, offset, high, offset);
            g.drawLine(offset, low, offset, high);
        }
        for (var i = 0; i < size; i++) {
            int columnSize = nono.columns[i].size();
            for (var j = 0; j < columnSize; j++) {
                var x = low + (int) ((i + 0.5) * step);
                var y = low - (int) ((columnSize - j - 0.5) * step * 0.5);
                g.drawString(nono.columns[i].get(j).toString(), x, y);
            }
        }
        for (var i = 0; i < size; i++) {
            int rowSize = nono.rows[i].size();
            for (var j = 0; j < rowSize; j++) {
                var y = low + (int) ((i + 0.5) * step);
                var x = low - (int) ((rowSize - j - 0.5) * step * 0.5);
                g.drawString(nono.rows[i].get(j).toString(), x, y);
            }
        }
        var tj = low + (int) ((nono.pos + 0.5) * step) - (nono.inColumnPass ? 0 : 8);
        if (nono.inColumnPass) {
            g.drawLine(tj, 10, tj,100);
        } else {
            g.drawLine(10, tj, 100, tj);
        }
    }

    private void drawMatrix(Graphics g, int size, int low, int step, Color c, STATE[][] matrix) {
        g.setColor(c);
        for (var i = 0; i < size; i++) {
            for (var j = 0; j < size; j++) {
                var y = low + i * step;
                var x = low + j * step;
                switch (matrix[i][j]) {
                    case EMPTY:
                        g.drawLine(x, y, x + step, y + step);
                        g.drawLine(x + step, y, x, y + step);
                        break;
                    case FILLED:
                        g.fillRect(x, y, step, step);
                        break;
                    case UNKNOWN:
                        break;
                }
            }
        }
        g.setColor(Color.black);
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            drawing = true;
            p1 = e.getPoint();
            p2 = p1;
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            drawing = false;
            p2 = e.getPoint();
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (drawing) {
                p2 = e.getPoint();
                repaint();
            }
        }
    }

    private class ControlPanel extends JPanel {

    private static final int DELTA = 10;

    public ControlPanel(Nonogram nonogram) {
        this.add(new MoveButton("\u2192", KeyEvent.VK_RIGHT, nonogram));
    }

        private class MoveButton extends JButton {
            KeyStroke k;
            Nonogram nono;

            public MoveButton(String name, int code, Nonogram nonogram) {
                super(name);
                this.k = KeyStroke.getKeyStroke(code, 0);
                this.nono = nonogram;
                this.setAction(new AbstractAction(this.getText()) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        nono.step();
                        Main.this.repaint();
                    }
                });
                ControlPanel.this.getInputMap(WHEN_IN_FOCUSED_WINDOW)
                        .put(k, k.toString());
                ControlPanel.this.getActionMap()
                        .put(k.toString(), new AbstractAction() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                MoveButton.this.doClick();
                            }
                        });
            }
        }
    }

    private void display() {
        JFrame f = new JFrame("LinePanel");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(this);
        f.add(new ControlPanel(this.nono), BorderLayout.SOUTH);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Main().display();
            }
        });
    }
}
