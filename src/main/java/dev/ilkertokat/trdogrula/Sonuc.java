package dev.ilkertokat.trdogrula;

/**
 * Doğrulama sonucu. Geçersizse neden geçersiz olduğu kullanıcıya gösterilebilecek bir mesajla döner.
 *
 * @param gecerli  değer geçerli mi
 * @param deger    boşlukları ve ayırıcıları temizlenmiş değer
 * @param hata     geçersizse açıklama, geçerliyse {@code null}
 */
public record Sonuc(boolean gecerli, String deger, String hata) {

    static Sonuc tamam(String deger) {
        return new Sonuc(true, deger, null);
    }

    static Sonuc hatali(String deger, String hata) {
        return new Sonuc(false, deger, hata);
    }
}
