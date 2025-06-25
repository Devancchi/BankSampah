package chart;

import java.awt.*;

/**
 * Layout that forces a specific number of components per row
 */
public class FixedRowLayout implements LayoutManager {
    private final int itemsPerRow;
    private final int hgap;
    private final int vgap;

    public FixedRowLayout(int itemsPerRow, int hgap, int vgap) {
        this.itemsPerRow = itemsPerRow;
        this.hgap = hgap;
        this.vgap = vgap;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // Not used
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // Not used
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            int componentCount = parent.getComponentCount();
            if (componentCount == 0) {
                return new Dimension(0, 0);
            }

            int rows = (int) Math.ceil((double) componentCount / itemsPerRow);

            // Get tallest and widest component
            int maxHeight = 0;
            int maxWidth = 0;

            for (int i = 0; i < componentCount; i++) {
                Component c = parent.getComponent(i);
                Dimension d = c.getPreferredSize();
                maxHeight = Math.max(maxHeight, d.height);
                maxWidth = Math.max(maxWidth, d.width);
            }

            Insets insets = parent.getInsets();
            int width = itemsPerRow * maxWidth + (itemsPerRow - 1) * hgap + insets.left + insets.right;
            int height = rows * maxHeight + (rows - 1) * vgap + insets.top + insets.bottom;

            return new Dimension(width, height);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentCount = parent.getComponentCount();

            if (componentCount == 0) {
                return;
            }

            // Calculate width of each cell
            int availableWidth = parent.getWidth() - insets.left - insets.right;
            int cellWidth = (availableWidth - (itemsPerRow - 1) * hgap) / itemsPerRow;

            // Get tallest component height
            int maxHeight = 0;
            for (int i = 0; i < componentCount; i++) {
                Component c = parent.getComponent(i);
                maxHeight = Math.max(maxHeight, c.getPreferredSize().height);
            }

            // Layout components
            for (int i = 0; i < componentCount; i++) {
                Component c = parent.getComponent(i);
                int row = i / itemsPerRow;
                int col = i % itemsPerRow;

                int x = insets.left + col * (cellWidth + hgap);
                int y = insets.top + row * (maxHeight + vgap);

                c.setBounds(x, y, cellWidth, maxHeight);
            }
        }
    }
}