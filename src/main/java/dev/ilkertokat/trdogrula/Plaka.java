package dev.ilkertokat.trdogrula;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Türk araç plakası (sivil, standart tescil plakası) doğrulaması.
 *
 * <p>Yapı: il kodu (01–81) + 1–3 harf + 2–5 rakam. Geçerli düzenler:
 * {@code 99 X 9999}, {@code 99 X 99999}, {@code 99 XX 999}, {@code 99 XX 9999}, {@code 99 XXX 99}, {@code 99 XXX 999}.
 * Harf grubunda Türkçeye özgü harfler (Ç, Ğ, İ, Ö, Ş, Ü) ile Q, W ve X kullanılmaz.
 *
 * <p>Not: Yalnızca biçim denetlenir; plakanın tescilli olduğunu doğrulamaz. Resmi, diplomatik ve
 * geçici plakalar kapsam dışıdır.
 */
public final class Plaka {

    private Plaka() {}

    private static final Pattern PARCALAR = Pattern.compile("(\\d{2})(\\p{L}{1,3})(\\d{2,5})");
    private static final String GECERLI_HARFLER = "ABCDEFGHIJKLMNOPRSTUVYZ";

    public static Sonuc dogrula(String girdi) {
        String s = temizle(girdi);
        Matcher m = PARCALAR.matcher(s);
        if (!m.matches()) return Sonuc.hatali(s, "Plaka biçimi geçersiz: il kodu, 1-3 harf ve 2-5 rakam olmalı");
        int il = Integer.parseInt(m.group(1));
        if (il < 1 || il > 81) return Sonuc.hatali(s, "İl kodu 01-81 arasında olmalı");
        String harfler = m.group(2);
        for (char c : harfler.toCharArray()) {
            if (GECERLI_HARFLER.indexOf(c) < 0) return Sonuc.hatali(s, "Plakada Ç, Ğ, İ, Ö, Ş, Ü, Q, W ve X harfleri kullanılmaz");
        }
        int rakam = m.group(3).length();
        boolean duzenUygun = switch (harfler.length()) {
            case 1 -> rakam == 4 || rakam == 5;
            case 2 -> rakam == 3 || rakam == 4;
            default -> rakam == 2 || rakam == 3;
        };
        if (!duzenUygun) {
            return Sonuc.hatali(s, harfler.length() + " harfli plakada rakam grubu " + rakamAraligi(harfler.length()) + " haneli olmalı");
        }
        return Sonuc.tamam(s);
    }

    public static boolean gecerliMi(String girdi) {
        return dogrula(girdi).gecerli();
    }

    /** Boşluk ve tireleri atar, büyük harfe çevirir (Locale.ROOT: "i" → "I", Türkçe "İ" değil). */
    public static String temizle(String girdi) {
        return girdi == null ? "" : girdi.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    /** Geçerli plakayı "34 ABC 123" biçiminde yazar; geçersizse {@link IllegalArgumentException}. */
    public static String bicimlendir(String girdi) {
        Sonuc sonuc = dogrula(girdi);
        if (!sonuc.gecerli()) throw new IllegalArgumentException(sonuc.hata());
        Matcher m = PARCALAR.matcher(sonuc.deger());
        m.matches();
        return m.group(1) + " " + m.group(2) + " " + m.group(3);
    }

    /** Geçerli plakanın il kodunu (1–81) döner; geçersizse {@link IllegalArgumentException}. */
    public static int ilKodu(String girdi) {
        Sonuc sonuc = dogrula(girdi);
        if (!sonuc.gecerli()) throw new IllegalArgumentException(sonuc.hata());
        return Integer.parseInt(sonuc.deger().substring(0, 2));
    }

    private static String rakamAraligi(int harfSayisi) {
        return switch (harfSayisi) {
            case 1 -> "4-5";
            case 2 -> "3-4";
            default -> "2-3";
        };
    }
}
