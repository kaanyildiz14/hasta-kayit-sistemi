package hastane;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.text.MessageFormat; 

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class hastane2 extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    
    // Veritabanı bağlantı adresi
    private static final String DB_URL = "jdbc:sqlite:hastane.db";

    private JTextField txtTcNo;
    private JTextField txtAdSoyad;
    private JTextField txtTelefon;
    private JTextField txtArama;
    private JComboBox<String> cmbPoliklinik;
    private JComboBox<String> cmbSehir;
    private JTextArea txtSikayet;
    private JTable table;
    private DefaultTableModel model;
    
    private JRadioButton rbKadin;
    private JRadioButton rbErkek;
    private ButtonGroup bgCinsiyet; 
    
    private int seciliId = -1; 

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    hastane2 frame = new hastane2();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public hastane2() {
        // Tabloyu oluştur (Eğer yoksa)
        veritabaniKurulumu();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 850); 
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(null); 

        // --- 1. TC KİMLİK NO ---
        JLabel lblTc = new JLabel("TC Kimlik No:");
        lblTc.setFont(new Font("Dialog", Font.BOLD, 12));
        lblTc.setBounds(20, 20, 100, 20);
        contentPane.add(lblTc);

        txtTcNo = new JTextField();
        txtTcNo.setBounds(20, 45, 200, 30);
        contentPane.add(txtTcNo);
        txtTcNo.setColumns(10);
        
        // Sadece rakam girilmesini ve 11 haneyi geçmemesini sağlar
        txtTcNo.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || txtTcNo.getText().length() >= 11) {
                    e.consume();
                }
            }
        });

        // --- 2. AD SOYAD ---
        JLabel lblAd = new JLabel("Ad Soyad:");
        lblAd.setFont(new Font("Dialog", Font.BOLD, 12));
        lblAd.setBounds(20, 85, 100, 20);
        contentPane.add(lblAd);

        txtAdSoyad = new JTextField();
        txtAdSoyad.setBounds(20, 110, 200, 30);
        contentPane.add(txtAdSoyad);
        txtAdSoyad.setColumns(10);

        // --- 3. TELEFON ---
        JLabel lblTel = new JLabel("Telefon:");
        lblTel.setFont(new Font("Dialog", Font.BOLD, 12));
        lblTel.setBounds(20, 150, 100, 20);
        contentPane.add(lblTel);

        txtTelefon = new JTextField();
        txtTelefon.setBounds(20, 175, 200, 30);
        contentPane.add(txtTelefon);
        txtTelefon.setColumns(10);
        
        txtTelefon.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || txtTelefon.getText().length() >= 11) {
                    e.consume();
                }
            }
        });
        
        // --- 4. CİNSİYET ---
        JLabel lblCinsiyet = new JLabel("Cinsiyet:");
        lblCinsiyet.setFont(new Font("Dialog", Font.BOLD, 12));
        lblCinsiyet.setBounds(20, 215, 100, 20);
        contentPane.add(lblCinsiyet);
        
        rbKadin = new JRadioButton("Kadın");
        rbKadin.setBounds(20, 235, 80, 25);
        contentPane.add(rbKadin);
        
        rbErkek = new JRadioButton("Erkek");
        rbErkek.setBounds(100, 235, 80, 25);
        contentPane.add(rbErkek);
        
        bgCinsiyet = new ButtonGroup();
        bgCinsiyet.add(rbKadin);
        bgCinsiyet.add(rbErkek);

        // --- 5. ŞEHİR ---
        JLabel lblsehir = new JLabel("Şehir :");
        lblsehir.setFont(new Font("Dialog", Font.BOLD, 12));
        lblsehir.setBounds(20, 270, 100, 20);
        contentPane.add(lblsehir);
        
        String[] sehirler = {
            "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Aksaray", "Amasya", "Ankara", "Antalya", "Ardahan", "Artvin",
            "Aydın", "Balıkesir", "Bartın", "Batman", "Bayburt", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur",
            "Bursa", "Çanakkale", "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Düzce", "Edirne", "Elazığ", "Erzincan",
            "Erzurum", "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Iğdır", "Isparta", "İstanbul",
            "İzmir", "Kahramanmaraş", "Karabük", "Karaman", "Kars", "Kastamonu", "Kayseri", "Kilis", "Kırıkkale", "Kırklareli",
            "Kırşehir", "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Mardin", "Mersin", "Muğla", "Muş",
            "Nevşehir", "Niğde", "Ordu", "Osmaniye", "Rize", "Sakarya", "Samsun", "Şanlıurfa", "Siirt", "Sinop",
            "Sivas", "Şırnak", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Uşak", "Van", "Yalova", "Yozgat", "Zonguldak"
        };
        cmbSehir = new JComboBox<>(sehirler);
        cmbSehir.setBounds(20, 295, 200, 30);
        contentPane.add(cmbSehir);

        // --- 6. POLİKLİNİK ---
        JLabel lblPoli = new JLabel("Poliklinik:");
        lblPoli.setFont(new Font("Dialog", Font.BOLD, 12));
        lblPoli.setBounds(20, 340, 100, 20);
        contentPane.add(lblPoli);

        String[] poliklinikler = {"Dahiliye", "Kardiyoloji", "Göz", "KBB", "Nöroloji", "Acil"};
        cmbPoliklinik = new JComboBox<>(poliklinikler);
        cmbPoliklinik.setBounds(20, 365, 200, 30);
        contentPane.add(cmbPoliklinik);

        // --- 7. ŞİKAYET ---
        JLabel lblSikayet = new JLabel("Şikayet:");
        lblSikayet.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        lblSikayet.setBounds(20, 410, 100, 20);
        contentPane.add(lblSikayet);
        
        txtSikayet = new JTextArea(); 
        JScrollPane scrollSikayet = new JScrollPane(txtSikayet);
        scrollSikayet.setBounds(20, 435, 200, 80);
        contentPane.add(scrollSikayet);
        
        // --- BUTONLAR ---
        JButton btnEkle = new JButton("EKLE");
        btnEkle.setBounds(20, 530, 200, 35);
        btnEkle.setBackground(new Color(46, 204, 113)); 
        btnEkle.setForeground(Color.WHITE);
        btnEkle.setOpaque(true); btnEkle.setBorderPainted(false);
        contentPane.add(btnEkle);

        JButton btnGuncelle = new JButton("GÜNCELLE");
        btnGuncelle.setBounds(20, 575, 200, 35);
        btnGuncelle.setBackground(new Color(52, 152, 219)); 
        btnGuncelle.setForeground(Color.WHITE);
        btnGuncelle.setOpaque(true); btnGuncelle.setBorderPainted(false);
        contentPane.add(btnGuncelle);

        JButton btnSil = new JButton("SİL");
        btnSil.setBounds(20, 620, 200, 35);
        btnSil.setBackground(new Color(231, 76, 60)); 
        btnSil.setForeground(Color.WHITE);
        btnSil.setOpaque(true); btnSil.setBorderPainted(false);
        contentPane.add(btnSil);

        JButton btnTemizle = new JButton("TEMİZLE");
        btnTemizle.setBounds(20, 665, 200, 35); 
        btnTemizle.setBackground(Color.GRAY);
        btnTemizle.setForeground(Color.WHITE);
        btnTemizle.setOpaque(true); btnTemizle.setBorderPainted(false);
        contentPane.add(btnTemizle);

        // --- ARAMA VE YAZDIRMA (ARAMA ETİKETİNİ DEĞİŞTİRDİM) ---
        JLabel lblAra = new JLabel("Hasta Ara (İsim / TC):"); 
        lblAra.setBounds(250, 20, 150, 20);
        lblAra.setFont(new Font("Tahoma", Font.BOLD, 12));
        contentPane.add(lblAra);

        txtArama = new JTextField();
        txtArama.setBounds(400, 15, 150, 30);
        contentPane.add(txtArama);
        
        JButton btnYazdir = new JButton("YAZDIR / PDF");
        btnYazdir.setBounds(570, 15, 120, 30); 
        btnYazdir.setBackground(new Color(243, 156, 18)); 
        btnYazdir.setForeground(Color.WHITE);
        btnYazdir.setOpaque(true); btnYazdir.setBorderPainted(false);
        contentPane.add(btnYazdir);
        
        // Yazdır butonu aksiyonu
        btnYazdir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    MessageFormat header = new MessageFormat("HASTA LİSTESİ");
                    MessageFormat footer = new MessageFormat("Sayfa {0}");
                    table.print(JTable.PrintMode.FIT_WIDTH, header, footer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        // --- TABLO OLUŞTURMA ---
        String[] kolonlar = {"ID", "TC No", "Ad Soyad", "Telefon", "Cinsiyet", "Poliklinik", "Şikayet", "Şehir"};
        model = new DefaultTableModel(kolonlar, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(250, 55, 900, 650); 
        contentPane.add(scrollPane);

        // Arama kutusuna yazıldıkça filtrele
        txtArama.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                verileriGetir(txtArama.getText());
            }
        });

        // Tabloda tıklanan satırı form alanlarına doldur
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    seciliId = Integer.parseInt(model.getValueAt(row, 0).toString());
                    txtTcNo.setText(model.getValueAt(row, 1).toString());
                    txtAdSoyad.setText(model.getValueAt(row, 2).toString());
                    txtTelefon.setText(model.getValueAt(row, 3).toString());
                    
                    String cinsiyet = model.getValueAt(row, 4).toString();
                    if(cinsiyet.equals("Kadın")) rbKadin.setSelected(true);
                    else if(cinsiyet.equals("Erkek")) rbErkek.setSelected(true);
                    else bgCinsiyet.clearSelection();

                    cmbPoliklinik.setSelectedItem(model.getValueAt(row, 5).toString());
                    txtSikayet.setText(model.getValueAt(row, 6).toString());
                    if (model.getColumnCount() > 7) {
                        cmbSehir.setSelectedItem(model.getValueAt(row, 7).toString());
                    }
                }
            }
        });

        // Buton aksiyonları
        btnEkle.addActionListener(e -> ekle());
        btnGuncelle.addActionListener(e -> guncelle());
        btnSil.addActionListener(e -> sil());
        btnTemizle.addActionListener(e -> formuTemizle());

        // Başlangıçta verileri listele
        verileriGetir("");
    }

    private Connection baglantiAc() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void veritabaniKurulumu() {
        // UNIQUE Kuralı: Aynı TC No'ya sahip ikinci kayıt eklenemez.
        String sql = "CREATE TABLE IF NOT EXISTS hastalar (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "tc_no TEXT UNIQUE, " + 
                     "ad_soyad TEXT, telefon TEXT, " +
                     "cinsiyet TEXT, poliklinik TEXT, sikayet TEXT, sehir TEXT, " + 
                     "kayit_tarihi TEXT DEFAULT CURRENT_TIMESTAMP)";
        try (Connection conn = baglantiAc(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage());
        }
    }

    // !!! BURASI GÜNCELLENDİ: ARTIK HEM İSME HEM TC'YE BAKIYOR !!!
    private void verileriGetir(String arama) {
        model.setRowCount(0);
        try (Connection conn = baglantiAc()) {
            // "OR tc_no LIKE ?" eklendi
            String sql = "SELECT * FROM hastalar WHERE ad_soyad LIKE ? OR tc_no LIKE ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + arama + "%"); // İsim için
            ps.setString(2, "%" + arama + "%"); // TC için
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> v = new Vector<>();
                v.add(rs.getInt("id"));
                v.add(rs.getString("tc_no"));
                v.add(rs.getString("ad_soyad"));
                v.add(rs.getString("telefon"));
                v.add(rs.getString("cinsiyet")); 
                v.add(rs.getString("poliklinik"));
                v.add(rs.getString("sikayet"));
                v.add(rs.getString("sehir"));
                model.addRow(v);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void ekle() {
        // --- KONTROL (VALIDATION) BÖLÜMÜ ---
        
        // 1. TC Kontrolü
        String tc = txtTcNo.getText().trim();
        if (tc.length() != 11) {
            JOptionPane.showMessageDialog(this, "HATA: TC Kimlik Numarası eksik! Tam 11 hane olmalıdır.");
            return; 
        }

        // 2. İsim Kontrolü
        if (txtAdSoyad.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "HATA: Ad Soyad alanı boş bırakılamaz!");
            return;
        }

        // 3. Telefon Kontrolü
        if (txtTelefon.getText().trim().length() < 10) {
            JOptionPane.showMessageDialog(this, "HATA: Lütfen geçerli bir telefon numarası giriniz.");
            return;
        }

        // 4. Cinsiyet Kontrolü
        String cinsiyet = "";
        if(rbKadin.isSelected()) cinsiyet = "Kadın";
        else if(rbErkek.isSelected()) cinsiyet = "Erkek";
        else { 
            JOptionPane.showMessageDialog(this, "HATA: Lütfen cinsiyet seçiniz!"); 
            return; 
        }

        // --- VERİTABANI İŞLEMİ ---
        try (Connection conn = baglantiAc()) {
            String sql = "INSERT INTO hastalar (tc_no, ad_soyad, telefon, cinsiyet, poliklinik, sikayet, sehir) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tc);
            ps.setString(2, txtAdSoyad.getText().trim());
            ps.setString(3, txtTelefon.getText().trim());
            ps.setString(4, cinsiyet); 
            ps.setString(5, cmbPoliklinik.getSelectedItem().toString());
            ps.setString(6, txtSikayet.getText());
            ps.setString(7, cmbSehir.getSelectedItem().toString());
            
            ps.executeUpdate();
            
            verileriGetir(""); 
            formuTemizle();
            JOptionPane.showMessageDialog(this, "Hasta Başarıyla Eklendi!");
            
        } catch (SQLException e) {
            // Aynı TC varsa yakala ve uyar
            if(e.getMessage().contains("UNIQUE") || e.getMessage().contains("tc_no")) {
                JOptionPane.showMessageDialog(this, "HATA: Bu TC Kimlik Numarası (" + txtTcNo.getText() + ") sistemde zaten kayıtlı!");
            } else {
                JOptionPane.showMessageDialog(this, "Veritabanı Hatası: " + e.getMessage());
            }
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage()); 
        }
    }

    private void guncelle() {
        if(seciliId == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen tablodan güncellenecek kişiyi seçiniz.");
            return;
        }

        // --- KONTROL (VALIDATION) BÖLÜMÜ ---
        
        String tc = txtTcNo.getText().trim();
        if (tc.length() != 11) {
            JOptionPane.showMessageDialog(this, "HATA: TC Kimlik Numarası 11 hane olmalıdır.");
            return;
        }

        if (txtAdSoyad.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "HATA: Ad Soyad alanı boş bırakılamaz!");
            return;
        }
        
        if (txtTelefon.getText().trim().length() < 10) {
            JOptionPane.showMessageDialog(this, "HATA: Geçerli bir telefon numarası giriniz.");
            return;
        }

        String cinsiyet = "";
        if(rbKadin.isSelected()) cinsiyet = "Kadın";
        else if(rbErkek.isSelected()) cinsiyet = "Erkek";
        else {
             JOptionPane.showMessageDialog(this, "Lütfen cinsiyet seçimi yapınız.");
             return;
        }
            
        // --- VERİTABANI GÜNCELLEME ---
        try (Connection conn = baglantiAc()) {
            String sql = "UPDATE hastalar SET tc_no=?, ad_soyad=?, telefon=?, cinsiyet=?, poliklinik=?, sikayet=?, sehir=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tc);
            ps.setString(2, txtAdSoyad.getText().trim());
            ps.setString(3, txtTelefon.getText().trim());
            ps.setString(4, cinsiyet);
            ps.setString(5, cmbPoliklinik.getSelectedItem().toString());
            ps.setString(6, txtSikayet.getText());
            ps.setString(7, cmbSehir.getSelectedItem().toString());
            ps.setInt(8, seciliId);
            
            ps.executeUpdate();
            verileriGetir(""); 
            formuTemizle();
            JOptionPane.showMessageDialog(this, "Kayıt Başarıyla Güncellendi!");
            
        } catch (SQLException e) {
            // Başkasının TC'sini yazmaya çalışırsa engelle
            if(e.getMessage().contains("UNIQUE") || e.getMessage().contains("tc_no")) {
                JOptionPane.showMessageDialog(this, "HATA: Bu TC Kimlik Numarası başka bir hastada kayıtlı! Değişiklik yapılamadı.");
            } else {
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage()); }
    }

    private void sil() {
        if(seciliId == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek kişiyi seçiniz.");
            return;
        }
        if(JOptionPane.showConfirmDialog(this, "Kayıt Silinsin mi?", "Onay", JOptionPane.YES_NO_OPTION) == 0) {
            try (Connection conn = baglantiAc()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM hastalar WHERE id=?");
                ps.setInt(1, seciliId);
                ps.executeUpdate();
                verileriGetir(""); formuTemizle();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage()); }
        }
    }

    private void formuTemizle() {
        txtTcNo.setText(""); txtAdSoyad.setText(""); txtTelefon.setText(""); txtSikayet.setText("");
        if(cmbSehir != null) cmbSehir.setSelectedIndex(0);
        if(cmbPoliklinik != null) cmbPoliklinik.setSelectedIndex(0);
        if(bgCinsiyet != null) bgCinsiyet.clearSelection(); 
        seciliId = -1;
    }
}