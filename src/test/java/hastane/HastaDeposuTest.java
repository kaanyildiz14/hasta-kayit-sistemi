package hastane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Her test kendi geçici SQLite dosyasıyla, gerçek veritabanı üzerinde çalışır. */
class HastaDeposuTest {

    @TempDir
    Path klasor;

    private HastaDeposu depo;

    private static Hasta hasta(String tc, String ad) {
        return new Hasta(0, tc, ad, "05321234567", "Kadın", "Kardiyoloji", "Çarpıntı", "İzmir");
    }

    @BeforeEach
    void kur() throws Exception {
        depo = new HastaDeposu("jdbc:sqlite:" + klasor.resolve("test.db"));
        depo.kurulum();
    }

    @Test
    void kurulumTekrarCalistirilabilir() throws Exception {
        depo.kurulum();
        assertTrue(depo.ara("").isEmpty());
    }

    @Test
    void eklenenHastaIdIleDonerVeListelenir() throws Exception {
        Hasta eklenen = depo.ekle(hasta("12345678901", "Ayşe Yılmaz"));
        assertTrue(eklenen.id() > 0);
        assertEquals(List.of(eklenen), depo.ara(""));
    }

    @Test
    void ayniTcIleIkinciKayitEklenemez() throws Exception {
        depo.ekle(hasta("12345678901", "Ayşe Yılmaz"));
        assertThrows(HastaDeposu.TcZatenKayitliException.class, () -> depo.ekle(hasta("12345678901", "Başka Biri")));
        assertEquals(1, depo.ara("").size());
    }

    @Test
    void isimVeyaTcIleArar() throws Exception {
        depo.ekle(hasta("11111111111", "Ayşe Yılmaz"));
        depo.ekle(hasta("22222222222", "Mehmet Demir"));
        assertEquals("Mehmet Demir", depo.ara("Demir").get(0).adSoyad());
        assertEquals("Ayşe Yılmaz", depo.ara("1111").get(0).adSoyad());
        assertEquals(2, depo.ara("").size());
    }

    @Test
    void guncellemeKaydiDegistirir() throws Exception {
        Hasta h = depo.ekle(hasta("12345678901", "Ayşe Yılmaz"));
        assertTrue(depo.guncelle(new Hasta(h.id(), h.tcNo(), "Ayşe Kaya", h.telefon(), h.cinsiyet(), "Göz",
                h.sikayet(), "Ankara")));
        Hasta guncel = depo.ara("").get(0);
        assertEquals("Ayşe Kaya", guncel.adSoyad());
        assertEquals("Göz", guncel.poliklinik());
    }

    @Test
    void guncellemedeBaskasininTcsiKullanilamaz() throws Exception {
        depo.ekle(hasta("11111111111", "Ayşe"));
        Hasta mehmet = depo.ekle(hasta("22222222222", "Mehmet"));
        Hasta cakisan = new Hasta(mehmet.id(), "11111111111", "Mehmet", mehmet.telefon(), mehmet.cinsiyet(),
                mehmet.poliklinik(), mehmet.sikayet(), mehmet.sehir());
        assertThrows(HastaDeposu.TcZatenKayitliException.class, () -> depo.guncelle(cakisan));
    }

    @Test
    void silmeKaydiKaldirir() throws Exception {
        Hasta h = depo.ekle(hasta("12345678901", "Ayşe"));
        assertTrue(depo.sil(h.id()));
        assertFalse(depo.sil(h.id()));
        assertTrue(depo.ara("").isEmpty());
    }

    @Test
    void aramaSqlInjectionaKarsiGuvenli() throws Exception {
        depo.ekle(hasta("12345678901", "Ayşe"));
        assertTrue(depo.ara("' OR '1'='1").isEmpty());
        assertEquals(1, depo.ara("").size());
    }
}
