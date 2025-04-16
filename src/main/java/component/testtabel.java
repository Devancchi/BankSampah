/**
 *
 * @author devan
 */
package component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author devan
 */
public class testtabel extends JTable {

    private int selectedRow = -1;

    public testtabel() {
        setShowHorizontalLines(true);
        setGridColor(new Color(230, 230, 230));
        setRowHeight(40);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Mencegah user mengubah posisi kolom
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object o, boolean isSelected, boolean hasFocus, int row, int column) {
                TablezHeader header = new TablezHeader(o.toString());
                if (column == 10) {
                    header.setHorizontalAlignment(JLabel.CENTER);
                }
                return header;
            }
        });

        // Custom renderer untuk tabel
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                com.setBackground(Color.WHITE);
                setBorder(noFocusBorder);
                setOpaque(true); // Pastikan rendering lebih stabil

                if (isSelected) {
                    com.setForeground(Color.WHITE);
                    com.setBackground(new Color(15, 89, 140));
                } else {
                    com.setForeground(new Color(80, 80, 80));
                    com.setBackground(Color.WHITE);
                }
                return com;
            }
        });

        // Event Listener untuk Click Selection
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = getSelectedRow();
                if (row == selectedRow) {
                    SwingUtilities.invokeLater(() -> {
                        clearSelection();
                        selectedRow = -1;
                    });
                } else {
                    selectedRow = row;
                }
            }
        });
    }

    public void addRow(Object[] row) {
        DefaultTableModel model = (DefaultTableModel) getModel();
        model.addRow(row);
    }

    private class TablezHeader extends JLabel {

        public TablezHeader(String text) {
            super(text);
            setOpaque(true);
            setBackground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setForeground(new Color(50, 50, 50)); // Lebih kontras
            setBorder(new MatteBorder(0, 0, 1, 0, new Color(200, 200, 200))); // Garis bawah lebih presisi
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
        }
    }
}
