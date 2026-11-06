# Ürün Tanımı

## Problem

İnsanlar telefonda farkında olmadan refleks hareketler yapar: bir uygulamayı kapatıp birkaç saniye sonra aynısını yeniden açmak, aynı sosyal medya akışına defalarca dönmek. Mevcut ekran süresi araçları toplam süreyi gösterir ama bu **tekrar eden davranışı** göstermez.

## Hedef kullanıcı

Telefon alışkanlığının farkında olmak isteyen, ama sert engelleyici/kilitleyici uygulamalar istemeyen kişi. İlk kullanıcı: geliştiricinin kendisi.

## Kapsam dışı (şimdilik)

- Play Store yayını ve politika uyumu
- Uygulama engelleme / zorla kapatma
- Bulut senkronizasyonu, hesap, analitik
- iOS

## Döngü tanımları

Başlangıç eşikleri aşağıdadır. Hepsi ayarlanabilir olmalı ve gerçek veriye bakarak kalibre edilmeli.

| Tür | Tanım | Başlangıç eşiği |
|---|---|---|
| **Tekrar dönüş** (re-open) | Aynı uygulama, bir pencere içinde N kez ön plana geldi | 10 dk içinde ≥ 2 kez |
| **Refleks açma** (quick peek) | Oturum çok kısa sürdü | < 5 sn |
| **Ping-pong** | İki uygulama arasında gidip gelme | A→B→A→B, 5 dk içinde |

Bir uygulamanın "oturumu", `ACTIVITY_RESUMED` ile `ACTIVITY_PAUSED` olayları arasındaki süredir. Çok kısa aralıkla birbirini izleyen iki oturum aynı kullanım parçası sayılabilir; bu birleştirme kuralı ayrıca netleştirilecek.

### Hariç tutulacaklar

Launcher, sistem arayüzü, klavye, telefon/mesajlaşma çağrı ekranı gibi paketler döngü sayılmaz. Kullanıcı ayrıca kendi hariç listesini yönetebilmeli.

## Loop skoru

Gün başına tek bir sayı: tespit edilen döngü olaylarının toplamı, ağırlıklandırılmış. Amaç karşılaştırma ("dün 14, bugün 9") ve trend görmek. Formül MVP'de basit tutulur: `tekrar dönüş sayısı + refleks açma sayısı`.

## Kullanıcı hikayeleri

1. İlk açılışta izni neden istediğimi anlayıp tek dokunuşla Ayarlar'a gidebilirim.
2. Bugün en çok hangi uygulamaya refleks olarak döndüğümü bir bakışta görürüm.
3. Gün içinde döngülerin hangi saatlerde yoğunlaştığını görürüm.
4. Son 7 günün trendini görürüm.
5. Bir uygulamayı analizden hariç tutabilirim.
6. (Sonra) Üst üste döngüye girdiğimde nazik bir bildirim alırım.

## Ekranlar

1. **Onboarding / İzin**: neden izin gerekiyor, Ayarlar'a git butonu
2. **Bugün**: loop skoru, en çok döngüye giren uygulamalar, gün içi zaman çizelgesi
3. **Geçmiş**: 7/30 günlük trend, saat bazlı ısı haritası
4. **Ayarlar**: eşikler, hariç tutulan uygulamalar, veriyi sil

## Başarı ölçütü (MVP)

Kendi telefonumda bir gün kullandıktan sonra, ekranda gördüğüm döngülerin "evet, bunu gerçekten yapıyorum" dedirtmesi.
