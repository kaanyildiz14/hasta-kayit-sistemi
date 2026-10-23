package hastane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HastaDogrulamaTest {

    private static Hasta hasta(String tc, String ad, String tel, String cinsiyet) {
        return new Hasta(0, tc, ad, tel, cinsiyet, "Dahiliye", "Baş ağrısı", "İzmir");
    }

    @Test
    void gecerliKayitHataVermez() {
        assertTrue(HastaDogrulama.dogrula(hasta("12345678901", "Ayşe Yılmaz", "05321234567", "Kadın")).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "123", "1234567890", "123456789012", "1234567890a" })
    void tcKimlikTamOnBirRakamOlmali(String tc) {
        assertEquals("HATA: TC Kimlik Numarası eksik! Tam 11 hane olmalıdır.",
                HastaDogrulama.dogrula(hasta(tc, "Ayşe", "05321234567", "Kadın")).orElseThrow());
    }

    @Test
    void adSoyadBosOlamaz() {
        assertEquals("HATA: Ad Soyad alanı boş bırakılamaz!",
                HastaDogrulama.dogrula(hasta("12345678901", "   ", "05321234567", "Erkek")).orElseThrow());
    }

    @Test
    void telefonEnAzOnHaneOlmali() {
        assertEquals("HATA: Lütfen geçerli bir telefon numarası giriniz.",
                HastaDogrulama.dogrula(hasta("12345678901", "Ali", "0532", "Erkek")).orElseThrow());
    }

    @Test
    void cinsiyetSecilmeli() {
        assertEquals("HATA: Lütfen cinsiyet seçiniz!",
                HastaDogrulama.dogrula(hasta("12345678901", "Ali", "05321234567", "")).orElseThrow());
    }
}
