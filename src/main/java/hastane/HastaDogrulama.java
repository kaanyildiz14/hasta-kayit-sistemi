package hastane;

import java.util.Optional;

/** Form doğrulama kuralları. Ekleme ve güncelleme aynı kuralları kullanır. */
public final class HastaDogrulama {

    private HastaDogrulama() {
    }

    /** Hata varsa kullanıcıya gösterilecek mesajı, yoksa boş döner. */
    public static Optional<String> dogrula(Hasta hasta) {
        String tc = hasta.tcNo() == null ? "" : hasta.tcNo().trim();
        if (tc.length() != 11 || !tc.chars().allMatch(Character::isDigit)) {
            return Optional.of("HATA: TC Kimlik Numarası eksik! Tam 11 hane olmalıdır.");
        }
        if (hasta.adSoyad() == null || hasta.adSoyad().isBlank()) {
            return Optional.of("HATA: Ad Soyad alanı boş bırakılamaz!");
        }
        if (hasta.telefon() == null || hasta.telefon().trim().length() < 10) {
            return Optional.of("HATA: Lütfen geçerli bir telefon numarası giriniz.");
        }
        if (!"Kadın".equals(hasta.cinsiyet()) && !"Erkek".equals(hasta.cinsiyet())) {
            return Optional.of("HATA: Lütfen cinsiyet seçiniz!");
        }
        return Optional.empty();
    }
}
