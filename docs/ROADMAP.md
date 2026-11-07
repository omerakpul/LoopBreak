# Yol Haritası

Her aşama sonunda uygulama telefonda çalışır durumda olmalı.

Durum notu: kod yazıldı ve `./gradlew lintDebug assembleDebug testDebugUnitTest` temiz geçiyor (21 birim testi). Uygulama Android emülatöründe (API 37) çalıştırılıp üç sekme, eşik değişimi ve ikon kontrol edildi. **Gerçek telefonda henüz denenmedi**; kalan tek madde gerçek kullanım verisine bakarak eşikleri kalibre etmek.

## Aşama 0: Temel (yapıldı)

- [x] Android Studio ile Compose projesi (`com.loopbreak`, minSdk 26)
- [x] README ve belgeler

## Aşama 1: Veri toplama

- [x] `PACKAGE_USAGE_STATS` izni: manifest, izin kontrolü (`UsageStatsReader.hasPermission`), Ayarlar'a yönlendirme
- [x] Onboarding / izin ekranı
- [x] `UsageStatsManager` sarmalayıcı ve ham olay okuma
- [x] KSP + Hilt kurulumu (`@HiltAndroidApp`, `@AndroidEntryPoint`)
- [x] Room kurulumu, `usage_events` tablosu, Hilt modülü
- [x] WorkManager (`@HiltWorker`) ile periyodik senkron (15 dk) + uygulama öne gelince senkron
- [x] Debug ekranı: ham olayları listele, "sıfırla ve yeniden eşitle"
- [x] Emülatörde doğrulandı: olaylar veritabanına geliyor. Gerçek telefonda ayrıca bakılmalı.

## Aşama 2: Döngü tespiti

- [x] `SessionBuilder` + birim testleri
- [x] `LoopDetector` (re-open, quick peek, ping-pong) + birim testleri
- [x] Varsayılan hariç tutulanlar (kendi uygulama, launcher, sistem arayüzü, klavye)
- [x] `loop_events` tablosu ve tespit hattının bağlanması (`LoopRepository.sync`)
- [x] Kullanıcının kendi hariç listesi (Ayarlar)
- [x] Emülatörde doğrulandı: yapay açma-kapama davranışı döngü olarak çıkıyor, eşik değişince geçmiş yeniden hesaplanıyor. Gerçek kullanım verisiyle ayrıca bakılmalı.

## Aşama 3: Arayüz

- [x] "Bugün" ekranı: loop skoru, en çok döngüye giren uygulamalar, döngü listesi
- [x] Saate göre döngü grafiği (Canvas)
- [x] "Geçmiş" ekranı: 7/30 gün trendi, gün x saat ısı haritası, en çok döngüye giren uygulamalar
- [x] "Ayarlar" ekranı: eşikler (değişince tüm geçmiş yeniden hesaplanır), hariç listesi, bildirim, veriyi sil
- [x] Gün seçici (Bugün ekranında ‹ ›)

## Aşama 4: Cilalama ve geri bildirim

- [x] Kalibrasyon aracı: Ayarlar'daki "Eşik ipucu" son 7 günün oturum süresi ve dönüş aralığı dağılımını gösteriyor (`CalibrationStats`).
- [ ] Eşik değerlerini gerçek telefonda birkaç gün kullanıp bu dağılıma göre seçmek. Veri senin telefonunda oluşacağı için burada yapılamaz.
- [x] Hata düzeltildi: Ayarlar uygulaması launcher sanılıp hariç tutuluyordu (emülatörde bulundu ve doğrulandı)
- [x] Sistem gürültüsü (izin denetleyici, paket yükleyici, Play Hizmetleri, Ayar Hizmetleri) varsayılan olarak hariç
- [x] Nazik bildirim (varsayılan kapalı, Ayarlar'dan açılır): son 10 dk içinde bir uygulamaya ≥ 3 dönüş
- [x] Koyu tema (emülatörde kontrol edildi, pencere arka planı da koyu), boş ve eşitleme hatası durumları
- [x] Uygulama ikonu (kırık sonsuzluk, adaptive + monochrome + bildirim simgesi)
- [x] Gece yarısını aşan döngüler (yeniden hesaplama gün başından 1 saat önce başlıyor)

## Aşama 5: Sağlamlaştırma (yapıldı)

- [x] Yedekleme kapatıldı (gizlilik açığı: otomatik yedekleme veritabanını buluta alabiliyordu)
- [x] Erişilebilirlik: düğme ve grafik açıklamaları
- [x] Gece yarısı sonrası tarih yenileme, veri saklama süresi (90 / 365 gün)
- [x] Ek birim testleri (özel eşikler, gece yarısı, uzun ping-pong, ayar varsayılanları), toplam 32
- [x] GitHub Actions CI (`.github/workflows/android.yml`): lint, derleme, test. Henüz çalıştırılmadı, ilk push'ta görülecek.
- [x] Bildirim emülatörde uçtan uca doğrulandı

## Aşama 6: Ürün genişletme (yapıldı)

- [x] MIT lisansı
- [x] İngilizce arayüz (varsayılan İngilizce, `values-tr` Türkçe; uygulama başına dil seçici)
- [x] Uygulama detay ekranı: Bugün ve Geçmiş'teki uygulamaya dokununca skor, günlük trend, saat dağılımı, son döngüler
- [x] Önceki günle karşılaştırma rozeti (`DayDelta`; büyük yüzdelerde düz fark)
- [x] Günlük hedef: Bugün'de ilerleme çubuğu, Geçmiş'te hedefte kalınan günler ve seri (`GoalStreak`), grafikte hedef çizgisi. Takip başlamadan önceki günler sayılmaz.
- [x] Haftalık özet bildirimi (`WeeklySummary`, `WeeklySchedule`): pazar 19:00, isteğe bağlı, "şimdi örnek gönder" düğmesi. Emülatörde doğrulandı.
- [x] Birim testleri toplam 53

## Aşama 7: Cilalama (yapıldı)

- [x] "Skor nasıl hesaplanır?" alt sayfası: skor kartındaki "?" düğmesi, kullanıcının kendi eşikleriyle açıklar
- [x] Giriş animasyonları: halka ve çubuk grafikler büyüyerek gelir, skor sayarak yükselir, ilerleme çubukları dolar (Android'in "animasyonları kaldır" ayarına uyar)
- [x] Yükleme iskeleti (yalnızca hiç veri yokken ilk eşitlemede) ve aşağı çekip yenileme
- [x] Yumuşatma turu: tonlu nötr renkler, sessiz kenarlıklar, daha yuvarlak köşeler ve boşluk, hafif yazı ağırlıkları, çizilmiş şevron okları, kayan sekme göstergesi, halkanın arkasında yumuşak ışıma
- [ ] Grafik çubuğuna dokununca değer ipucu
- [ ] Geri bildirim mesajı (snackbar)

## Fikir havuzu (sıralanmamış)

- Uygulama bazlı hedefler (örneğin bir uygulamanın döngülerini yarıya indir)
- Döngü anında "nefes al" ekranı
- Bildirimde "1 saat sessize al" eylemi
- Widget
- Dışa aktarma (CSV)
