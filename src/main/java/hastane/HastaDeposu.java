package hastane;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Hasta kayıtlarının SQLite işlemleri. Arayüzden ayrı olduğu için testlerde
 * geçici bir veritabanıyla doğrudan denenebilir.
 */
public class HastaDeposu {

    /** Aynı TC Kimlik No ile ikinci bir kayıt eklenmeye çalışıldığında fırlatılır. */
    public static class TcZatenKayitliException extends Exception {
        private static final long serialVersionUID = 1L;

        public TcZatenKayitliException(String tcNo) {
            super("TC Kimlik No zaten kayıtlı: " + tcNo);
        }
    }

    private final String dbUrl;

    public HastaDeposu(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    private Connection baglantiAc() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public void kurulum() throws SQLException {
        // UNIQUE Kuralı: Aynı TC No'ya sahip ikinci kayıt eklenemez.
        String sql = "CREATE TABLE IF NOT EXISTS hastalar ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "tc_no TEXT UNIQUE, "
                + "ad_soyad TEXT, telefon TEXT, "
                + "cinsiyet TEXT, poliklinik TEXT, sikayet TEXT, sehir TEXT, "
                + "kayit_tarihi TEXT DEFAULT CURRENT_TIMESTAMP)";
        try (Connection conn = baglantiAc(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /** Ad soyad veya TC numarasında arama metnini içeren kayıtlar; boş metin tüm kayıtları getirir. */
    public List<Hasta> ara(String arama) throws SQLException {
        String sql = "SELECT * FROM hastalar WHERE ad_soyad LIKE ? OR tc_no LIKE ? ORDER BY id";
        List<Hasta> sonuc = new ArrayList<>();
        try (Connection conn = baglantiAc(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + arama + "%");
            ps.setString(2, "%" + arama + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sonuc.add(new Hasta(
                            rs.getInt("id"),
                            rs.getString("tc_no"),
                            rs.getString("ad_soyad"),
                            rs.getString("telefon"),
                            rs.getString("cinsiyet"),
                            rs.getString("poliklinik"),
                            rs.getString("sikayet"),
                            rs.getString("sehir")));
                }
            }
        }
        return sonuc;
    }

    /** Kaydı ekler ve veritabanının verdiği id ile döner. */
    public Hasta ekle(Hasta hasta) throws SQLException, TcZatenKayitliException {
        String sql = "INSERT INTO hastalar (tc_no, ad_soyad, telefon, cinsiyet, poliklinik, sikayet, sehir) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = baglantiAc();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            alanlariDoldur(ps, hasta);
            ps.executeUpdate();
            try (ResultSet anahtar = ps.getGeneratedKeys()) {
                anahtar.next();
                return hasta.idIle(anahtar.getInt(1));
            }
        } catch (SQLException e) {
            if (tcCakismasiMi(e)) {
                throw new TcZatenKayitliException(hasta.tcNo());
            }
            throw e;
        }
    }

    /** Kayıt bulunduysa true döner. */
    public boolean guncelle(Hasta hasta) throws SQLException, TcZatenKayitliException {
        String sql = "UPDATE hastalar SET tc_no=?, ad_soyad=?, telefon=?, cinsiyet=?, poliklinik=?, sikayet=?, sehir=? "
                + "WHERE id=?";
        try (Connection conn = baglantiAc(); PreparedStatement ps = conn.prepareStatement(sql)) {
            alanlariDoldur(ps, hasta);
            ps.setInt(8, hasta.id());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            if (tcCakismasiMi(e)) {
                throw new TcZatenKayitliException(hasta.tcNo());
            }
            throw e;
        }
    }

    /** Kayıt bulunduysa true döner. */
    public boolean sil(int id) throws SQLException {
        try (Connection conn = baglantiAc();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM hastalar WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private static void alanlariDoldur(PreparedStatement ps, Hasta h) throws SQLException {
        ps.setString(1, h.tcNo().trim());
        ps.setString(2, h.adSoyad().trim());
        ps.setString(3, h.telefon().trim());
        ps.setString(4, h.cinsiyet());
        ps.setString(5, h.poliklinik());
        ps.setString(6, h.sikayet());
        ps.setString(7, h.sehir());
    }

    private static boolean tcCakismasiMi(SQLException e) {
        String mesaj = String.valueOf(e.getMessage());
        return mesaj.contains("UNIQUE") && mesaj.contains("tc_no");
    }
}
