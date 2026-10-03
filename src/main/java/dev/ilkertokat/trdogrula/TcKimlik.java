package dev.ilkertokat.trdogrula;

/**
 * T.C. kimlik numarası doğrulaması.
 *
 * <p>Kurallar: 11 hane, ilk hane 0 olamaz;
 * 10. hane = ((1+3+5+7+9. haneler) × 7 − (2+4+6+8. haneler)) mod 10;
 * 11. hane = (ilk 10 hanenin toplamı) mod 10.
 *
 * <p>Not: Algoritma yalnızca numaranın <em>biçimsel</em> geçerliliğini denetler; numaranın gerçek bir kişiye ait
 * olduğunu doğrulamaz (bunun için NVİ servisi gerekir).
 */
public final class TcKimlik {

    private TcKimlik() {}

    public static Sonuc dogrula(String girdi) {
        String s = girdi == null ? "" : girdi.replaceAll("\\s", "");
        if (!s.matches("\\d{11}")) return Sonuc.hatali(s, "T.C. kimlik numarası 11 haneli ve yalnız rakamlardan oluşmalı");
        if (s.charAt(0) == '0') return Sonuc.hatali(s, "T.C. kimlik numarası 0 ile başlayamaz");
        String beklenen = kontrolHaneleri(s.substring(0, 9));
        if (!s.substring(9).equals(beklenen)) return Sonuc.hatali(s, "Kontrol haneleri tutmuyor");
        return Sonuc.tamam(s);
    }

    public static boolean gecerliMi(String girdi) {
        return dogrula(girdi).gecerli();
    }

    /** İlk 9 haneden kontrol hanelerini (10. ve 11.) hesaplar. Test verisi üretmek için de kullanılır. */
    public static String kontrolHaneleri(String ilk9) {
        if (ilk9 == null || !ilk9.matches("[1-9]\\d{8}")) throw new IllegalArgumentException("İlk 9 hane 1-9 ile başlayan 9 rakam olmalı");
        int[] d = ilk9.chars().map(c -> c - '0').toArray();
        int tek = d[0] + d[2] + d[4] + d[6] + d[8];
        int cift = d[1] + d[3] + d[5] + d[7];
        int h10 = Math.floorMod(tek * 7 - cift, 10);
        int h11 = (tek + cift + h10) % 10;
        return "" + h10 + h11;
    }
}
