package dev.ilkertokat.trdogrula;

/**
 * Vergi kimlik numarası (VKN, 10 hane) doğrulaması — Gelir İdaresi'nin kontrol hanesi algoritması.
 *
 * <p>İlk 9 hanenin her biri için: {@code t = (hane + 9 − i) mod 10}, {@code v = (t × 2^(9−i)) mod 9};
 * {@code t ≠ 0} iken {@code v = 0} çıkarsa {@code v = 9} alınır. Son hane = (10 − toplam mod 10) mod 10.
 */
public final class VergiNo {

    private VergiNo() {}

    public static Sonuc dogrula(String girdi) {
        String s = girdi == null ? "" : girdi.replaceAll("\\s", "");
        if (!s.matches("\\d{10}")) return Sonuc.hatali(s, "Vergi kimlik numarası 10 haneli ve yalnız rakamlardan oluşmalı");
        if (s.charAt(9) - '0' != kontrolHanesi(s.substring(0, 9))) return Sonuc.hatali(s, "Kontrol hanesi tutmuyor");
        return Sonuc.tamam(s);
    }

    public static boolean gecerliMi(String girdi) {
        return dogrula(girdi).gecerli();
    }

    /** İlk 9 haneden son (kontrol) hanesini hesaplar. */
    public static int kontrolHanesi(String ilk9) {
        if (ilk9 == null || !ilk9.matches("\\d{9}")) throw new IllegalArgumentException("9 rakam bekleniyor");
        int toplam = 0;
        for (int i = 0; i < 9; i++) {
            int t = (ilk9.charAt(i) - '0' + 9 - i) % 10;
            int v = (t * (1 << (9 - i))) % 9;
            if (t != 0 && v == 0) v = 9;
            toplam += v;
        }
        return (10 - toplam % 10) % 10;
    }
}
