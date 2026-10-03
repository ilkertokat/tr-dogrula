package dev.ilkertokat.trdogrula;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * IBAN doğrulaması (ISO 13616, mod-97) ve Türkiye IBAN'ları için ayrıntılar.
 *
 * <p>TR IBAN yapısı (26 karakter): {@code TR kk BBBBB 0 HHHHHHHHHHHHHHHH} — kk kontrol, BBBBB banka kodu,
 * 0 rezerv hane, son 16 hane hesap numarası.
 */
public final class Iban {

    private Iban() {}

    /** Sık kullanılan bankaların EFT kodları (tam liste değildir). */
    private static final Map<String, String> BANKALAR = Map.of(
            "00010", "Ziraat Bankası",
            "00012", "Halkbank",
            "00015", "VakıfBank",
            "00046", "Akbank",
            "00062", "Garanti BBVA",
            "00064", "İş Bankası",
            "00067", "Yapı Kredi",
            "00111", "QNB",
            "00134", "DenizBank");

    public static Sonuc dogrula(String girdi) {
        String s = temizle(girdi);
        if (s.length() < 15 || s.length() > 34 || !s.matches("[A-Z]{2}\\d{2}[A-Z0-9]+")) {
            return Sonuc.hatali(s, "IBAN biçimi geçersiz");
        }
        if (s.startsWith("TR")) {
            if (s.length() != 26) return Sonuc.hatali(s, "TR IBAN 26 karakter olmalı");
            if (!s.substring(4).matches("\\d{22}")) return Sonuc.hatali(s, "TR IBAN'da ülke kodundan sonra yalnız rakam olmalı");
            if (s.charAt(9) != '0') return Sonuc.hatali(s, "TR IBAN'ın 10. hanesi (rezerv) 0 olmalı");
        }
        if (mod97(s.substring(4) + s.substring(0, 4)) != 1) return Sonuc.hatali(s, "Kontrol haneleri tutmuyor");
        return Sonuc.tamam(s);
    }

    public static boolean gecerliMi(String girdi) {
        return dogrula(girdi).gecerli();
    }

    /** Boşluk ve tireleri atar, büyük harfe çevirir (Türkçe "i" sorunu olmaması için Locale.ROOT). */
    public static String temizle(String girdi) {
        return girdi == null ? "" : girdi.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    /** Dörder karakterlik gruplarla yazar: TR33 0006 1005 … */
    public static String bicimlendir(String girdi) {
        return temizle(girdi).replaceAll("(.{4})(?!$)", "$1 ");
    }

    /** Geçerli bir TR IBAN'ından banka adını döner (bilinen bankalar için). */
    public static Optional<String> banka(String girdi) {
        String s = temizle(girdi);
        if (!s.startsWith("TR") || !gecerliMi(s)) return Optional.empty();
        return Optional.ofNullable(BANKALAR.get(s.substring(4, 9)));
    }

    /** Büyük sayıyı BigInteger'a çevirmeden, parça parça mod 97 hesaplar. */
    static int mod97(String yeniden) {
        int kalan = 0;
        for (char c : yeniden.toCharArray()) {
            int deger = Character.isDigit(c) ? c - '0' : c - 'A' + 10;
            kalan = deger >= 10 ? (kalan * 100 + deger) % 97 : (kalan * 10 + deger) % 97;
        }
        return kalan;
    }
}
