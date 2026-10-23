package hastane;

/** Bir hasta kaydı. id, veritabanına eklenmemiş kayıtlarda 0'dır. */
public record Hasta(
        int id,
        String tcNo,
        String adSoyad,
        String telefon,
        String cinsiyet,
        String poliklinik,
        String sikayet,
        String sehir) {

    public Hasta {
        // Ayrıca kontrol gerektirmesin diye null metinler boş metne çevrilir
        sikayet = sikayet == null ? "" : sikayet;
    }

    public Hasta idIle(int yeniId) {
        return new Hasta(yeniId, tcNo, adSoyad, telefon, cinsiyet, poliklinik, sikayet, sehir);
    }
}
