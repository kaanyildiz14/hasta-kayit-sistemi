package hastane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;

public class hastane2 extends JFrame {

    private static final long serialVersionUID = 1L;

    // Veritabanı bağlantı adresi
    private static final String DB_URL = "jdbc:sqlite:hastane.db";
    private final HastaDeposu depo = new HastaDeposu(DB_URL);

    // Renkler: sakin bir sağlık uygulaması paleti
    private static final Color ZEMIN = new Color(0xF3F6F8);
    private static final Color KART = Color.WHITE;
    private static final Color CIZGI = new Color(0xE2E8EC);
    private static final Color SOLUK_YAZI = new Color(0x5B6B75);
    private static final Color TEHLIKE = new Color(0xC2410C);

    private JTextField txtTcNo;
    private JTextField txtAdSoyad;
    private JTextField txtTelefon;
    private JTextField txtArama;
    private JComboBox<String> cmbPoliklinik;
    private JComboBox<String> cmbSehir;
    private JTextArea txtSikayet;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblKayitSayisi;

    private JRadioButton rbKadin;
    private JRadioButton rbErkek;
    private ButtonGroup bgCinsiyet;

    private int seciliId = -1;

    /** Uygulamanın teması: FlatLaf açık tema, deniz mavisi-yeşil vurgu ve Inter yazı tipi. */
    public static void temayiKur() {
        FlatInterFont.install();
        FlatLaf.setPreferredFontFamily(FlatInterFont.FAMILY);
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#0E7490"));
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.alternateRowColor", new Color(0xF7FAFB));
        UIManager.put("Table.selectionBackground", new Color(0xCFEAF0));
        UIManager.put("Table.selectionForeground", new Color(0x0B2530));
        UIManager.put("Table.selectionInactiveBackground", new Color(0xCFEAF0));
        UIManager.put("Table.selectionInactiveForeground", new Color(0x0B2530));
        UIManager.put("TextField.margin", new Insets(7, 10, 7, 10));
        UIManager.put("TextArea.margin", new Insets(6, 8, 6, 8));
        UIManager.put("Button.margin", new Insets(7, 14, 7, 14));
        UIManager.put("ComboBox.padding", new Insets(5, 8, 5, 8));
        UIManager.put("TableHeader.background", KART);
        UIManager.put("TableHeader.separatorColor", KART);
        UIManager.put("TableHeader.bottomSeparatorColor", CIZGI);
    }

    public static void main(String[] args) {
        temayiKur();
        EventQueue.invokeLater(() -> {
            try {
                hastane2 frame = new hastane2();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public hastane2() {
        // Tabloyu oluştur (Eğer yoksa)
        veritabaniKurulumu();

        setTitle("Hasta Kayıt Sistemi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setSize(1240, 780);
        setLocationRelativeTo(null);

        JPanel kok = new JPanel(new BorderLayout(20, 20));
        kok.setBackground(ZEMIN);
        kok.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setContentPane(kok);

        kok.add(ustBolum(), BorderLayout.NORTH);
        kok.add(formKarti(), BorderLayout.WEST);
        kok.add(tabloKarti(), BorderLayout.CENTER);

        // Başlangıçta verileri listele
        verileriGetir("");
    }

    // --- ÜST BÖLÜM: başlık, arama, yazdırma ---
    private JComponent ustBolum() {
        JPanel ust = new JPanel(new BorderLayout());
        ust.setOpaque(false);

        JPanel baslik = new JPanel();
        baslik.setOpaque(false);
        baslik.setLayout(new BoxLayout(baslik, BoxLayout.Y_AXIS));
        JLabel lblBaslik = new JLabel("Hasta Kayıt Sistemi");
        lblBaslik.putClientProperty(FlatClientProperties.STYLE, "font: bold +10");
        lblKayitSayisi = new JLabel(" ");
        lblKayitSayisi.setForeground(SOLUK_YAZI);
        baslik.add(lblBaslik);
        baslik.add(Box.createVerticalStrut(4));
        baslik.add(lblKayitSayisi);
        ust.add(baslik, BorderLayout.WEST);

        JPanel sag = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        sag.setOpaque(false);

        txtArama = new JTextField(22);
        txtArama.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Hasta ara: isim veya TC Kimlik No");
        txtArama.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtArama.putClientProperty(FlatClientProperties.STYLE, "margin: 6,10,6,10");
        // Arama kutusuna yazıldıkça filtrele
        txtArama.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                verileriGetir(txtArama.getText());
            }
        });
        sag.add(txtArama);

        JButton btnYazdir = new JButton("Yazdır / PDF");
        btnYazdir.putClientProperty(FlatClientProperties.STYLE, "margin: 6,14,6,14");
        btnYazdir.addActionListener(e -> {
            try {
                MessageFormat header = new MessageFormat("HASTA LİSTESİ");
                MessageFormat footer = new MessageFormat("Sayfa {0}");
                table.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        sag.add(btnYazdir);
        ust.add(sag, BorderLayout.EAST);
        return ust;
    }

    // --- SOL: HASTA FORMU ---
    private JComponent formKarti() {
        JPanel kart = kart();
        kart.setLayout(new GridBagLayout());
        kart.setPreferredSize(new Dimension(330, 0));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridwidth = 2;

        JLabel lblForm = new JLabel("Hasta bilgileri");
        lblForm.putClientProperty(FlatClientProperties.STYLE, "font: bold +3");
        g.insets = new Insets(0, 0, 14, 0);
        kart.add(lblForm, g);

        txtTcNo = new JTextField();
        txtTcNo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "11 haneli");
        // Sadece rakam girilmesini ve 11 haneyi geçmemesini sağlar
        txtTcNo.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || txtTcNo.getText().length() >= 11) {
                    e.consume();
                }
            }
        });
        alanEkle(kart, g, "TC Kimlik No", txtTcNo);

        txtAdSoyad = new JTextField();
        alanEkle(kart, g, "Ad Soyad", txtAdSoyad);

        txtTelefon = new JTextField();
        txtTelefon.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "05xx xxx xx xx");
        txtTelefon.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || txtTelefon.getText().length() >= 11) {
                    e.consume();
                }
            }
        });
        alanEkle(kart, g, "Telefon", txtTelefon);

        rbKadin = new JRadioButton("Kadın");
        rbErkek = new JRadioButton("Erkek");
        rbKadin.setOpaque(false);
        rbErkek.setOpaque(false);
        bgCinsiyet = new ButtonGroup();
        bgCinsiyet.add(rbKadin);
        bgCinsiyet.add(rbErkek);
        JPanel cinsiyet = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cinsiyet.setOpaque(false);
        cinsiyet.add(rbKadin);
        cinsiyet.add(Box.createHorizontalStrut(16));
        cinsiyet.add(rbErkek);
        alanEkle(kart, g, "Cinsiyet", cinsiyet);

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
        String[] poliklinikler = { "Dahiliye", "Kardiyoloji", "Göz", "KBB", "Nöroloji", "Acil" };
        cmbPoliklinik = new JComboBox<>(poliklinikler);
        JPanel ikili = new JPanel(new GridLayout(1, 2, 10, 0));
        ikili.setOpaque(false);
        ikili.add(etiketli("Şehir", cmbSehir));
        ikili.add(etiketli("Poliklinik", cmbPoliklinik));
        g.insets = new Insets(0, 0, 12, 0);
        kart.add(ikili, g);

        txtSikayet = new JTextArea(4, 20);
        txtSikayet.setLineWrap(true);
        txtSikayet.setWrapStyleWord(true);
        JScrollPane scrollSikayet = new JScrollPane(txtSikayet);
        // GridBagLayout dar alanda yüksekliği sıfıra indirmesin
        scrollSikayet.setMinimumSize(new Dimension(0, 96));
        scrollSikayet.setPreferredSize(new Dimension(0, 96));
        alanEkle(kart, g, "Şikayet", scrollSikayet);

        // --- BUTONLAR ---
        JButton btnEkle = new JButton("Hasta ekle");
        btnEkle.putClientProperty(FlatClientProperties.STYLE,
                "background: $Component.accentColor; foreground: #ffffff; font: bold; borderWidth: 0; margin: 8,14,8,14");
        JButton btnGuncelle = new JButton("Güncelle");
        JButton btnSil = new JButton("Sil");
        btnSil.setForeground(TEHLIKE);
        JButton btnTemizle = new JButton("Formu temizle");
        btnTemizle.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        btnTemizle.setForeground(SOLUK_YAZI);

        g.insets = new Insets(8, 0, 8, 0);
        kart.add(btnEkle, g);
        JPanel ikincil = new JPanel(new GridLayout(1, 2, 10, 0));
        ikincil.setOpaque(false);
        ikincil.add(btnGuncelle);
        ikincil.add(btnSil);
        g.insets = new Insets(0, 0, 4, 0);
        kart.add(ikincil, g);
        kart.add(btnTemizle, g);

        // Kalan boşluk en alta itilir
        g.weighty = 1;
        kart.add(Box.createVerticalGlue(), g);

        // Buton aksiyonları
        btnEkle.addActionListener(e -> ekle());
        btnGuncelle.addActionListener(e -> guncelle());
        btnSil.addActionListener(e -> sil());
        btnTemizle.addActionListener(e -> formuTemizle());
        return kart;
    }

    // --- SAĞ: HASTA TABLOSU ---
    private JComponent tabloKarti() {
        String[] kolonlar = { "ID", "TC No", "Ad Soyad", "Telefon", "Cinsiyet", "Poliklinik", "Şikayet", "Şehir" };
        model = new DefaultTableModel(kolonlar, 0) {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xEEF2F4));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "font: bold; height: 38");
        // Başlıklar hücrelerle aynı hizada (sola dayalı) olsun
        if (table.getTableHeader().getDefaultRenderer() instanceof JLabel baslikRenderer) {
            baslikRenderer.setHorizontalAlignment(JLabel.LEFT);
        }
        int[] genislikler = { 48, 115, 140, 115, 75, 95, 150, 85 };
        for (int i = 0; i < genislikler.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(genislikler[i]);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(56);

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
                    if (cinsiyet.equals("Kadın")) rbKadin.setSelected(true);
                    else if (cinsiyet.equals("Erkek")) rbErkek.setSelected(true);
                    else bgCinsiyet.clearSelection();

                    cmbPoliklinik.setSelectedItem(model.getValueAt(row, 5).toString());
                    txtSikayet.setText(model.getValueAt(row, 6).toString());
                    cmbSehir.setSelectedItem(model.getValueAt(row, 7).toString());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(KART);

        JPanel kart = kart();
        kart.setLayout(new BorderLayout());
        kart.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(CIZGI),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        kart.add(scrollPane, BorderLayout.CENTER);
        return kart;
    }

    private static JPanel kart() {
        JPanel kart = new JPanel();
        kart.setBackground(KART);
        kart.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(CIZGI),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        return kart;
    }

    private static JComponent etiketli(String etiket, JComponent alan) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel lbl = new JLabel(etiket);
        lbl.setForeground(SOLUK_YAZI);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        p.add(lbl, BorderLayout.NORTH);
        p.add(alan, BorderLayout.CENTER);
        return p;
    }

    private static void alanEkle(JPanel kart, GridBagConstraints g, String etiket, JComponent alan) {
        g.insets = new Insets(0, 0, 12, 0);
        kart.add(etiketli(etiket, alan), g);
    }

    private void veritabaniKurulumu() {
        try {
            depo.kurulum();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage());
        }
    }

    // Arama hem isme hem TC'ye bakar
    private void verileriGetir(String arama) {
        model.setRowCount(0);
        try {
            var hastalar = depo.ara(arama);
            for (Hasta h : hastalar) {
                model.addRow(new Object[] { h.id(), h.tcNo(), h.adSoyad(), h.telefon(), h.cinsiyet(),
                        h.poliklinik(), h.sikayet(), h.sehir() });
            }
            lblKayitSayisi.setText(arama.isBlank() ? hastalar.size() + " kayıtlı hasta"
                    : "\u201C" + arama + "\u201D için " + hastalar.size() + " sonuç");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + e.getMessage());
        }
    }

    /** Formdaki değerlerden bir hasta kaydı oluşturur. */
    private Hasta formdanHasta(int id) {
        String cinsiyet = rbKadin.isSelected() ? "Kadın" : rbErkek.isSelected() ? "Erkek" : "";
        return new Hasta(id, txtTcNo.getText(), txtAdSoyad.getText(), txtTelefon.getText(), cinsiyet,
                cmbPoliklinik.getSelectedItem().toString(), txtSikayet.getText(),
                cmbSehir.getSelectedItem().toString());
    }

    /** Kurala uymayan alan varsa uyarı gösterir ve false döner. */
    private boolean gecerliMi(Hasta hasta) {
        Optional<String> hata = HastaDogrulama.dogrula(hasta);
        hata.ifPresent(mesaj -> JOptionPane.showMessageDialog(this, mesaj));
        return hata.isEmpty();
    }

    private void ekle() {
        Hasta hasta = formdanHasta(0);
        if (!gecerliMi(hasta)) {
            return;
        }
        try {
            depo.ekle(hasta);
            verileriGetir("");
            formuTemizle();
            JOptionPane.showMessageDialog(this, "Hasta Başarıyla Eklendi!");
        } catch (HastaDeposu.TcZatenKayitliException e) {
            JOptionPane.showMessageDialog(this,
                    "HATA: Bu TC Kimlik Numarası (" + hasta.tcNo().trim() + ") sistemde zaten kayıtlı!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Veritabanı Hatası: " + e.getMessage());
        }
    }

    private void guncelle() {
        if (seciliId == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen tablodan güncellenecek kişiyi seçiniz.");
            return;
        }
        Hasta hasta = formdanHasta(seciliId);
        if (!gecerliMi(hasta)) {
            return;
        }
        try {
            depo.guncelle(hasta);
            verileriGetir("");
            formuTemizle();
            JOptionPane.showMessageDialog(this, "Kayıt Başarıyla Güncellendi!");
        } catch (HastaDeposu.TcZatenKayitliException e) {
            JOptionPane.showMessageDialog(this,
                    "HATA: Bu TC Kimlik Numarası başka bir hastada kayıtlı! Değişiklik yapılamadı.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void sil() {
        if (seciliId == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek kişiyi seçiniz.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Kayıt Silinsin mi?", "Onay", JOptionPane.YES_NO_OPTION) == 0) {
            try {
                depo.sil(seciliId);
                verileriGetir("");
                formuTemizle();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
            }
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