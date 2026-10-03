# TR Doğrula — Kimlik, Vergi No ve IBAN Doğrulama

[![CI](https://github.com/ilkertokat/tr-dogrula/actions/workflows/ci.yml/badge.svg)](https://github.com/ilkertokat/tr-dogrula/actions)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Dependencies](https://img.shields.io/badge/dependencies-0-success)
![License](https://img.shields.io/badge/license-MIT-green)

**Türkiye'ye özgü numaraları doğrulayan, bağımlılıksız bir Java kütüphanesi:** T.C. kimlik numarası, vergi kimlik numarası (VKN) ve IBAN. Form doğrulaması, e-ticaret ödeme adımları ve fatura sistemlerinde kullanılmak üzere yazıldı. Geçersiz girdide nedenini kullanıcıya gösterilebilecek bir mesajla döner.

> *English summary below.*

## Kullanım

```java
TcKimlik.gecerliMi("10000000146");                 // true
TcKimlik.dogrula("10000000145").hata();            // "Kontrol haneleri tutmuyor"

VergiNo.gecerliMi("1234567890");                   // kontrol hanesine göre true/false
VergiNo.kontrolHanesi("123456789");                // son haneyi hesaplar

Iban.gecerliMi("tr33 0006 1005 1978 6457 8413 26");  // true — boşluk, tire, küçük harf kabul edilir
Iban.bicimlendir("TR330006100519786457841326");      // "TR33 0006 1005 1978 6457 8413 26"
Iban.dogrula("TR330006110519786457841326").hata();   // "TR IBAN'ın 10. hanesi (rezerv) 0 olmalı"
Iban.banka(iban);                                    // Optional["Garanti BBVA"] (bilinen banka kodları için)
```

Her `dogrula()` çağrısı bir `Sonuc` kaydı döner: `gecerli`, temizlenmiş `deger` ve geçersizse `hata`.

## Algoritmalar

| Numara | Kural |
|---|---|
| **T.C. kimlik** | 11 hane, 0 ile başlamaz · 10. hane = ((1+3+5+7+9. haneler) × 7 − (2+4+6+8. haneler)) mod 10 · 11. hane = ilk 10 hanenin toplamı mod 10 |
| **VKN** | Gelir İdaresi algoritması: ilk 9 hanenin her biri için `t = (hane + 9 − i) mod 10`, `v = (t × 2^(9−i)) mod 9` (t ≠ 0 iken v = 0 ise 9); son hane = (10 − Σv mod 10) mod 10 |
| **IBAN** | ISO 13616 mod-97 (ilk 4 karakter sona taşınır, harfler sayıya çevrilir, kalan 1 olmalı) · TR için ayrıca 26 karakter, yalnız rakam ve rezerv hanenin 0 olması |

Mod-97 hesabı `BigInteger` kullanmadan, rakam rakam kalan alınarak yapılır. Büyük harfe çevirme `Locale.ROOT` ile yapılır; Türkçe yerel ayarda `i → İ` dönüşümünün doğrulamayı bozması engellenir (bunun için ayrı bir test var).

> T.C. kimlik ve VKN doğrulaması numaranın **biçimsel** olarak geçerli olduğunu gösterir; gerçek bir kişiye ya da firmaya ait olduğunu doğrulamaz.

## Testler

```bash
mvn verify     # JUnit 5 — 23 test
```

- Bilinen örnek numaralar ve hata mesajları (parametreli testler)
- **Özellik testleri:** Rastgele üretilen 1.000 T.C. kimlik ve 1.000 VKN'nin geçerli olduğu, tek bir hanenin değişmesinin her zaman yakalandığı
- Türkiye, Almanya ve Birleşik Krallık örnek IBAN'ları; kontrol, uzunluk, rezerv hane ve biçim hataları
- Türkçe yerel ayarda büyük harf dönüşümü

CI testleri Java 17 ve 21 üzerinde çalıştırır.

---

## English

**TR Doğrula** is a dependency-free Java 17 library that validates Turkish national ID numbers (T.C. Kimlik No), tax numbers (VKN) and IBANs (ISO 13616 mod-97, plus Turkey-specific length and reserved-digit rules, bank lookup for common banks and pretty-printing). Every validation returns a result with a user-facing reason when invalid. It is covered by 23 JUnit 5 tests, including property tests over 1,000 generated numbers each and a regression test for the Turkish-locale `i → İ` upper-casing pitfall; CI runs on Java 17 and 21.

## Lisans

MIT © 2026 İlker Tokat
