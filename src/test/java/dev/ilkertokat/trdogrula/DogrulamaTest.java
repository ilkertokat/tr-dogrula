package dev.ilkertokat.trdogrula;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogrulamaTest {

    // --- T.C. kimlik ---------------------------------------------------------------
    @Test
    void tcKimlik_kontrolHaneleri_bilinen_ornek() {
        // 10000000146 algoritmanın klasik örneğidir: 1,0,0,0,0,0,0,0,1 → 4, 6
        assertEquals("46", TcKimlik.kontrolHaneleri("100000001"));
        assertTrue(TcKimlik.gecerliMi("10000000146"));
        assertTrue(TcKimlik.gecerliMi("100 000 001 46"));
    }

    @ParameterizedTest
    @CsvSource({
            "10000000145, Kontrol haneleri tutmuyor",
            "01234567890, 0 ile başlayamaz",
            "1234567890, 11 haneli",
            "1000000014a, 11 haneli",
    })
    void tcKimlik_gecersizler_neden_ile(String no, String parca) {
        Sonuc s = TcKimlik.dogrula(no);
        assertFalse(s.gecerli());
        assertTrue(s.hata().contains(parca), s.hata());
    }

    @Test
    void tcKimlik_rastgele_uretilenler_gecerli_tek_hane_degisince_gecersiz() {
        Random r = new Random(42);
        for (int i = 0; i < 1000; i++) {
            String ilk9 = (1 + r.nextInt(9)) + String.format("%08d", r.nextInt(100_000_000));
            String no = ilk9 + TcKimlik.kontrolHaneleri(ilk9);
            assertTrue(TcKimlik.gecerliMi(no), no);
            int pos = r.nextInt(11);
            char eski = no.charAt(pos);
            char yeni = (char) ('0' + ((eski - '0' + 1 + r.nextInt(8)) % 10));
            String bozuk = no.substring(0, pos) + yeni + no.substring(pos + 1);
            assertFalse(TcKimlik.gecerliMi(bozuk), bozuk); // tek hane hatası her zaman yakalanır
        }
    }

    // --- Vergi no ------------------------------------------------------------------
    @Test
    void vergiNo_uretilen_numaralar_gecerli_tek_hane_hatasi_yakalanir() {
        Random r = new Random(7);
        int yakalanan = 0;
        for (int i = 0; i < 1000; i++) {
            String ilk9 = String.format("%09d", r.nextInt(1_000_000_000));
            String no = ilk9 + VergiNo.kontrolHanesi(ilk9);
            assertTrue(VergiNo.gecerliMi(no), no);
            String bozuk = ilk9 + (VergiNo.kontrolHanesi(ilk9) + 1) % 10;
            if (!VergiNo.gecerliMi(bozuk)) yakalanan++;
        }
        assertEquals(1000, yakalanan);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "12345678901", "12345678a0"})
    void vergiNo_bicim_hatalari(String no) {
        assertFalse(VergiNo.gecerliMi(no));
    }

    // --- IBAN ----------------------------------------------------------------------
    @ParameterizedTest
    @ValueSource(strings = {
            "TR330006100519786457841326",       // ISO 13616 / Wikipedia örnek TR IBAN
            "tr33 0006 1005 1978 6457 8413 26",
            "TR33-0006-1005-1978-6457-8413-26",
            "DE89370400440532013000",           // Almanya örnek IBAN
            "GB82WEST12345698765432",           // Birleşik Krallık örnek IBAN
    })
    void iban_gecerliler(String iban) {
        assertTrue(Iban.gecerliMi(iban), Iban.dogrula(iban).hata());
    }

    @ParameterizedTest
    @CsvSource({
            "TR330006100519786457841327, Kontrol haneleri",
            "TR33000610051978645784132, 26 karakter",
            "TR330006110519786457841326, rezerv",
            "TR33000610051978645784132X, yalnız rakam",
            "33TR0006100519786457841326, biçimi",
    })
    void iban_gecersizler(String iban, String parca) {
        Sonuc s = Iban.dogrula(iban);
        assertFalse(s.gecerli());
        assertTrue(s.hata().contains(parca), s.hata());
    }

    @Test
    void iban_bicimlendirme_ve_banka() {
        assertEquals("TR33 0006 1005 1978 6457 8413 26", Iban.bicimlendir("tr330006100519786457841326"));
        assertTrue(Iban.banka("TR330006100519786457841326").isEmpty()); // 00061 listede yok
        // Banka kodu 00062 olan sentetik bir IBAN üret: kontrol hanelerini mod-97 ile hesapla
        String govde = "0006200000000000012345678".substring(0, 22);
        int kontrol = 98 - Iban.mod97(govde + "TR00");
        String iban = "TR" + String.format("%02d", kontrol) + govde;
        assertTrue(Iban.gecerliMi(iban), iban);
        assertEquals("Garanti BBVA", Iban.banka(iban).orElseThrow());
    }

    @Test
    void iban_turkce_locale_buyuk_harf_sorunu_yok() {
        java.util.Locale eski = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"));
            assertTrue(Iban.gecerliMi("gb82west12345698765432")); // "i" → "İ" olsaydı geçersiz olurdu
        } finally {
            java.util.Locale.setDefault(eski);
        }
    }
}
