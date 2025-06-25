package main;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import component.LoggerUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JLabel;
import view.TabDashboard;
import view.TabDataBarang;
import view.TabManajemenSampah;
import view.TabTransaksi;
import java.awt.EventQueue;
import java.awt.Font;
import javax.swing.UIManager;
import loginregister.loginregister;
import component.UserSession;
import component.text_bergerak_atas;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import loginregister.Login;
import view.TabLaporanStatistik;
import view.TabManajemenNasabah;
import view.TabManajemenUser;

public class Dashboard extends javax.swing.JFrame {

    private JPanel lastClickedPanel = null;

    private UserSession user;

    public Dashboard(UserSession user) {
        this.user = user;
        initComponents();
        
        panelSlide1.init(new text_bergerak_atas("DASHBOARD", new Color(255,255,255)), new text_bergerak_atas("MANAJEMEN NASABAH", new Color(255,255,255)),new text_bergerak_atas("MANAJEMEN SAMPAH", new Color(255,255,255)),new text_bergerak_atas("LAPORAN & STATISTIK", new Color(255,255,255)),new text_bergerak_atas("TRANSAKSI", new Color(255,255,255)),new text_bergerak_atas("DATA BARANG", new Color(255,255,255)));
        panelSlide1.setAnimate(10);

        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSidebarLabelEffect(dashboard);
        setSidebarLabelEffect(nasabah);
        setSidebarLabelEffect(manajemen_sampah);
        setSidebarLabelEffect(laporan_statistik);
        setSidebarLabelEffect(transaksi);
        setSidebarLabelEffect(data_barang);
        setSidebarLabelEffect(data_user);
        setSidebarLabelEffect(logout);
        panelMain.setLayout(new BorderLayout());
        lb_user.setText(user.getNama());
        lb_level.setText(user.getLevel());
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabDashboard());
        panelMain.repaint();
        panelMain.revalidate();
        
        panelMain.setLayout(new BorderLayout());
        lb_user.setText(user.getNama());
        lb_level.setText(user.getLevel());
        
        gantiHalaman(new TabDashboard(), "Dashboard");
           
    }
 
    
    private void gantiHalaman(JPanel panel, String namaHalaman) {
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(panel);
        panelMain.repaint();
        panelMain.revalidate();
    }
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pn_sidebar = new javax.swing.JPanel();
        shadowDashboard = new component.ShadowPanel();
        dashboard = new javax.swing.JLabel();
        shadowNasabah = new component.ShadowPanel();
        nasabah = new javax.swing.JLabel();
        shadowSampah = new component.ShadowPanel();
        manajemen_sampah = new javax.swing.JLabel();
        shadowLaporan = new component.ShadowPanel();
        laporan_statistik = new javax.swing.JLabel();
        shadowTransaksi = new component.ShadowPanel();
        transaksi = new javax.swing.JLabel();
        shadowDataBarang = new component.ShadowPanel();
        data_barang = new javax.swing.JLabel();
        shadowLogout = new component.ShadowPanel();
        logout = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        shadowUser = new component.ShadowPanel();
        data_user = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        pn_header = new javax.swing.JPanel();
        lb_level = new javax.swing.JLabel();
        lb_user = new javax.swing.JLabel();
        panelSlide1 = new component.PanelSlide();
        panelGradient1 = new grafik.panel.PanelGradient();
        panelMain = new component.PanelSlide();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pn_sidebar.setBackground(new java.awt.Color(24, 58, 51));
        pn_sidebar.setPreferredSize(new java.awt.Dimension(240, 1024));

        shadowDashboard.setBackground(new java.awt.Color(24, 58, 51));

        dashboard.setBackground(new java.awt.Color(255, 255, 255));
        dashboard.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        dashboard.setForeground(new java.awt.Color(255, 255, 255));
        dashboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_dashboard.png"))); // NOI18N
        dashboard.setText("Dashboard");
        dashboard.setPreferredSize(new java.awt.Dimension(240, 48));
        dashboard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dashboardMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout shadowDashboardLayout = new javax.swing.GroupLayout(shadowDashboard);
        shadowDashboard.setLayout(shadowDashboardLayout);
        shadowDashboardLayout.setHorizontalGroup(
            shadowDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowDashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        shadowDashboardLayout.setVerticalGroup(
            shadowDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowDashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        shadowNasabah.setBackground(new java.awt.Color(24, 58, 51));

        nasabah.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        nasabah.setForeground(new java.awt.Color(255, 255, 255));
        nasabah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_manajemen_nasabah.png"))); // NOI18N
        nasabah.setText("Manajemen Nasabah");
        nasabah.setPreferredSize(new java.awt.Dimension(240, 48));
        nasabah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                nasabahMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                nasabahMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout shadowNasabahLayout = new javax.swing.GroupLayout(shadowNasabah);
        shadowNasabah.setLayout(shadowNasabahLayout);
        shadowNasabahLayout.setHorizontalGroup(
            shadowNasabahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowNasabahLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nasabah, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        shadowNasabahLayout.setVerticalGroup(
            shadowNasabahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowNasabahLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(nasabah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        shadowSampah.setBackground(new java.awt.Color(24, 58, 51));

        manajemen_sampah.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        manajemen_sampah.setForeground(new java.awt.Color(255, 255, 255));
        manajemen_sampah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_manajemen_sampah.png"))); // NOI18N
        manajemen_sampah.setText("Manajemen Sampah");
        manajemen_sampah.setPreferredSize(new java.awt.Dimension(240, 48));
        manajemen_sampah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                manajemen_sampahMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout shadowSampahLayout = new javax.swing.GroupLayout(shadowSampah);
        shadowSampah.setLayout(shadowSampahLayout);
        shadowSampahLayout.setHorizontalGroup(
            shadowSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(manajemen_sampah, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        shadowSampahLayout.setVerticalGroup(
            shadowSampahLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowSampahLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(manajemen_sampah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        shadowLaporan.setBackground(new java.awt.Color(24, 58, 51));

        laporan_statistik.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        laporan_statistik.setForeground(new java.awt.Color(255, 255, 255));
        laporan_statistik.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_laporan_statistik.png"))); // NOI18N
        laporan_statistik.setText("Laporan & Statistik");
        laporan_statistik.setPreferredSize(new java.awt.Dimension(240, 48));
        laporan_statistik.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                laporan_statistikMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout shadowLaporanLayout = new javax.swing.GroupLayout(shadowLaporan);
        shadowLaporan.setLayout(shadowLaporanLayout);
        shadowLaporanLayout.setHorizontalGroup(
            shadowLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(laporan_statistik, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        shadowLaporanLayout.setVerticalGroup(
            shadowLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowLaporanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(laporan_statistik, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        shadowTransaksi.setBackground(new java.awt.Color(24, 58, 51));

        transaksi.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        transaksi.setForeground(new java.awt.Color(255, 255, 255));
        transaksi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_transaksi.png"))); // NOI18N
        transaksi.setText("Transaksi");
        transaksi.setPreferredSize(new java.awt.Dimension(240, 48));
        transaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                transaksiMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout shadowTransaksiLayout = new javax.swing.GroupLayout(shadowTransaksi);
        shadowTransaksi.setLayout(shadowTransaksiLayout);
        shadowTransaksiLayout.setHorizontalGroup(
            shadowTransaksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowTransaksiLayout.createSequentialGroup()
                .addComponent(transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        shadowTransaksiLayout.setVerticalGroup(
            shadowTransaksiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowTransaksiLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(transaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        shadowDataBarang.setBackground(new java.awt.Color(24, 58, 51));

        data_barang.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        data_barang.setForeground(new java.awt.Color(255, 255, 255));
        data_barang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_data_barang.png"))); // NOI18N
        data_barang.setText("Data Barang");
        data_barang.setPreferredSize(new java.awt.Dimension(240, 48));
        data_barang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                data_barangMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout shadowDataBarangLayout = new javax.swing.GroupLayout(shadowDataBarang);
        shadowDataBarang.setLayout(shadowDataBarangLayout);
        shadowDataBarangLayout.setHorizontalGroup(
            shadowDataBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowDataBarangLayout.createSequentialGroup()
                .addComponent(data_barang, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        shadowDataBarangLayout.setVerticalGroup(
            shadowDataBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowDataBarangLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(data_barang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        shadowLogout.setBackground(new java.awt.Color(24, 58, 51));

        logout.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        logout.setForeground(new java.awt.Color(255, 255, 255));
        logout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_logout.png"))); // NOI18N
        logout.setText("Logout");
        logout.setPreferredSize(new java.awt.Dimension(240, 48));
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout shadowLogoutLayout = new javax.swing.GroupLayout(shadowLogout);
        shadowLogout.setLayout(shadowLogoutLayout);
        shadowLogoutLayout.setHorizontalGroup(
            shadowLogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowLogoutLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );
        shadowLogoutLayout.setVerticalGroup(
            shadowLogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowLogoutLayout.createSequentialGroup()
                .addGap(0, 9, Short.MAX_VALUE)
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/logo_tukerin.png"))); // NOI18N
        jLabel1.setAlignmentX(0.5F);

        shadowUser.setBackground(new java.awt.Color(24, 58, 51));

        data_user.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        data_user.setForeground(new java.awt.Color(255, 255, 255));
        data_user.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon_user_setting.png"))); // NOI18N
        data_user.setText("Manajemen User");
        data_user.setPreferredSize(new java.awt.Dimension(240, 48));
        data_user.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                data_userMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout shadowUserLayout = new javax.swing.GroupLayout(shadowUser);
        shadowUser.setLayout(shadowUserLayout);
        shadowUserLayout.setHorizontalGroup(
            shadowUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shadowUserLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(data_user, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        shadowUserLayout.setVerticalGroup(
            shadowUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, shadowUserLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(data_user, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel5.setBackground(new java.awt.Color(221, 221, 221));
        jLabel5.setFont(jLabel5.getFont().deriveFont(jLabel5.getFont().getSize()+2f));
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Sistem");

        jLabel6.setBackground(new java.awt.Color(221, 221, 221));
        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getSize()+2f));
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Transaksi");

        jLabel7.setBackground(new java.awt.Color(221, 221, 221));
        jLabel7.setFont(jLabel7.getFont().deriveFont(jLabel7.getFont().getSize()+2f));
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Manajemen");

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(jLabel8.getFont().deriveFont(jLabel8.getFont().getSize()+2f));
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Dashboard");

        jSeparator1.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator1.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pn_sidebarLayout = new javax.swing.GroupLayout(pn_sidebar);
        pn_sidebar.setLayout(pn_sidebarLayout);
        pn_sidebarLayout.setHorizontalGroup(
            pn_sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pn_sidebarLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pn_sidebarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pn_sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowUser, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowDashboard, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowLogout, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowNasabah, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowLaporan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowSampah, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowDataBarang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shadowTransaksi, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6))
        );
        pn_sidebarLayout.setVerticalGroup(
            pn_sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pn_sidebarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowNasabah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowSampah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowLaporan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowDataBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shadowUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(shadowLogout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        pn_header.setBackground(new java.awt.Color(255, 255, 255));

        lb_level.setBackground(new java.awt.Color(0, 0, 0));
        lb_level.setFont(lb_level.getFont());
        lb_level.setForeground(new java.awt.Color(153, 153, 153));
        lb_level.setText("Admin");

        lb_user.setBackground(new java.awt.Color(255, 255, 255));
        lb_user.setFont(lb_user.getFont().deriveFont(lb_user.getFont().getStyle() | java.awt.Font.BOLD));
        lb_user.setText("User");

        panelSlide1.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelSlide1Layout = new javax.swing.GroupLayout(panelSlide1);
        panelSlide1.setLayout(panelSlide1Layout);
        panelSlide1Layout.setHorizontalGroup(
            panelSlide1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 347, Short.MAX_VALUE)
        );
        panelSlide1Layout.setVerticalGroup(
            panelSlide1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        panelGradient1.setBackground(new java.awt.Color(0, 204, 204));
        panelGradient1.setColorGradient(new java.awt.Color(0, 255, 204));

        javax.swing.GroupLayout pn_headerLayout = new javax.swing.GroupLayout(pn_header);
        pn_header.setLayout(pn_headerLayout);
        pn_headerLayout.setHorizontalGroup(
            pn_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pn_headerLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(panelGradient1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(panelSlide1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 704, Short.MAX_VALUE)
                .addGroup(pn_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pn_headerLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lb_user))
                    .addComponent(lb_level))
                .addGap(74, 74, 74))
        );
        pn_headerLayout.setVerticalGroup(
            pn_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pn_headerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pn_headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelSlide1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pn_headerLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(lb_user)
                        .addGap(5, 5, 5)
                        .addComponent(lb_level, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 21, Short.MAX_VALUE))
                    .addComponent(panelGradient1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        panelMain.setBackground(new java.awt.Color(231, 231, 231));

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1194, Short.MAX_VALUE)
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 940, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(pn_sidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pn_header, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pn_header, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(pn_sidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void logoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutMouseClicked
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin logout?",
            "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
//            LoggerUtil.insert(user.getId(), "Logout dari sistem");
            this.dispose();
            new loginregister().setVisible(true);
        } else {
            // Reset warnanya kalau batal logout
            changeTabColor(lastClickedPanel);
        }
    }//GEN-LAST:event_logoutMouseClicked

    private void data_barangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_data_barangMouseClicked
        changeTabColor(shadowDataBarang);
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabDataBarang(user));
        panelMain.repaint();
        panelMain.revalidate();
        panelSlide1.show(5);
    }//GEN-LAST:event_data_barangMouseClicked

    private void transaksiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_transaksiMouseClicked
        changeTabColor(shadowTransaksi);
        panelMain.setOpaque(false);
        panelMain.removeAll();
        TabTransaksi tabTransaksi = new TabTransaksi(user);
        panelMain.add(tabTransaksi);
        panelMain.repaint();
        panelMain.revalidate();
        panelSlide1.show(4);
        
        // Request focus after panel is shown
        javax.swing.SwingUtilities.invokeLater(() -> {
            tabTransaksi.requestFocus();
            tabTransaksi.getScanbarang().requestFocusInWindow();
        });
    }//GEN-LAST:event_transaksiMouseClicked

    private void laporan_statistikMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_laporan_statistikMouseClicked
        changeTabColor(shadowLaporan);
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabLaporanStatistik());
        panelMain.repaint();
        panelMain.revalidate();
        panelSlide1.show(3);
    }//GEN-LAST:event_laporan_statistikMouseClicked

    private void manajemen_sampahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_manajemen_sampahMouseClicked
        changeTabColor(shadowSampah);
        panelMain.setOpaque(false);
        panelMain.removeAll();
        TabManajemenSampah tabManajemenSampah = new TabManajemenSampah(user);
        panelMain.add(tabManajemenSampah);
        panelMain.repaint();
        panelMain.revalidate();
        panelSlide1.show(2);
        
        // Request focus after panel is shown
        javax.swing.SwingUtilities.invokeLater(() -> {
            tabManajemenSampah.requestFocus();
            tabManajemenSampah.getTxt_Kode().requestFocusInWindow();
        });
    }//GEN-LAST:event_manajemen_sampahMouseClicked

    private void nasabahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nasabahMouseClicked
        changeTabColor(shadowNasabah);
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabManajemenNasabah(user));
        panelMain.repaint();
        panelMain.revalidate();
        panelSlide1.show(1);
    }//GEN-LAST:event_nasabahMouseClicked

    private void dashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardMouseClicked
        changeTabColor(shadowDashboard);
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabDashboard());
        panelMain.repaint();
        panelMain.revalidate();
        panelSlide1.show(0);
    }//GEN-LAST:event_dashboardMouseClicked

    private void nasabahMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nasabahMouseEntered
       
    }//GEN-LAST:event_nasabahMouseEntered

    private void data_userMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_data_userMouseClicked
        changeTabColor(shadowUser);
        panelMain.setOpaque(false);
        panelMain.removeAll();
        panelMain.add(new TabManajemenUser(user));
        panelMain.repaint();
        panelMain.revalidate();
        panelSlide1.show(6);
    }//GEN-LAST:event_data_userMouseClicked

    private void setSidebarLabelEffect(JLabel label) {
        Color defaultColor = new Color(24, 58, 51);
        Color hoverColor = new Color(47, 106, 93);
        Color clickColor = new Color(36, 82, 72);

        label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                JPanel parent = (JPanel) label.getParent();
                if (parent != lastClickedPanel) {
                    parent.setBackground(hoverColor);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                JPanel parent = (JPanel) label.getParent();
                if (parent != lastClickedPanel) {
                    parent.setBackground(defaultColor);
                }
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                JPanel parent = (JPanel) label.getParent();
                parent.setBackground(clickColor);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                JPanel parent = (JPanel) label.getParent();
                if (parent != lastClickedPanel) {
                    parent.setBackground(hoverColor);
                }
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JPanel parent = (JPanel) label.getParent();
                changeTabColor(parent); // kita aktifkan panel, bukan label
            }
        });
    }

    private void changeTabColor(JPanel newPanel) {
        Color defaultColor = new Color(24, 58, 51);
        Color clickColor = new Color(36, 82, 72);

        if (lastClickedPanel != null && lastClickedPanel != newPanel) {
            lastClickedPanel.setBackground(defaultColor);
        }

        newPanel.setBackground(clickColor);
        lastClickedPanel = newPanel;
    }

    public static void main(String args[]) {
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource("themes");
        FlatMacLightLaf.setup();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        EventQueue.invokeLater(() -> new loginregister().setVisible(true));
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FlatLightLaf.setup();
                new Login().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dashboard;
    private javax.swing.JLabel data_barang;
    private javax.swing.JLabel data_user;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel laporan_statistik;
    private javax.swing.JLabel lb_level;
    private javax.swing.JLabel lb_user;
    private javax.swing.JLabel logout;
    private javax.swing.JLabel manajemen_sampah;
    private javax.swing.JLabel nasabah;
    private grafik.panel.PanelGradient panelGradient1;
    private component.PanelSlide panelMain;
    private component.PanelSlide panelSlide1;
    private javax.swing.JPanel pn_header;
    private javax.swing.JPanel pn_sidebar;
    private component.ShadowPanel shadowDashboard;
    private component.ShadowPanel shadowDataBarang;
    private component.ShadowPanel shadowLaporan;
    private component.ShadowPanel shadowLogout;
    private component.ShadowPanel shadowNasabah;
    private component.ShadowPanel shadowSampah;
    private component.ShadowPanel shadowTransaksi;
    private component.ShadowPanel shadowUser;
    private javax.swing.JLabel transaksi;
    // End of variables declaration//GEN-END:variables
}
