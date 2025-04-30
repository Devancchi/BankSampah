package swing;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JScrollPane;

public class ScrollBar extends JScrollPane {

    public ScrollBar() {
        setUI(new ModernScrollBarUI());
        setPreferredSize(new Dimension(5, 5));
        setBackground(new Color(242, 242, 242));
        getHorizontalScrollBar().setUnitIncrement(20);
    }
}
