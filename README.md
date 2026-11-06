# LoopBreak

Farkında olmadan tekrarladığın telefon hareketlerini görünür kılan Android uygulaması.

Bir uygulamayı açıp kapatıp tekrar açıyorsun, sosyal medya akışına defalarca dönüyorsun. LoopBreak bu **açma-kapama döngülerini** tespit eder ve sana gösterir. Amaç seni kısıtlamak ya da suçlamak değil, davranışın farkına varmanı sağlamak.

## Temel fikir

Klasik ekran süresi uygulamaları "ne kadar kullandın" sorusuna cevap verir. LoopBreak "**nasıl kullandın**" sorusuna cevap verir:

- Aynı uygulamaya kısa aralıklarla tekrar tekrar dönüş
- Çok kısa süren refleks açmalar
- İki uygulama arasında gidip gelme (A → B → A → B)

## Durum

MVP kodu tamam: izin, veri toplama, döngü tespiti, Bugün / Geçmiş / Ayarlar ekranları, isteğe bağlı bildirim. Emülatörde doğrulandı; gerçek telefonda eşik kalibrasyonu bekliyor. Play Store yayını şimdilik kapsam dışı.

## Teknoloji özeti

| Katman | Seçim |
|---|---|
| Dil | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Veri toplama | `UsageStatsManager.queryEvents()` |
| Arka plan | WorkManager (periyodik senkronizasyon) |
| Yerel veritabanı | Room |
| Ayarlar | DataStore (Preferences) |
| Asenkron | Coroutines + Flow |
| Navigasyon | Navigation Compose |
| Grafikler | Compose Canvas ile elle çizim |
| DI | Hilt (`hilt-navigation-compose`, `hilt-work`) |
| Min SDK | 26 (Android 8.0) |

Gerekçeler için [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Gizlilik ilkesi

Tüm veri cihazda işlenir ve cihazda kalır. İnternet izni yoktur, analitik ya da üçüncü taraf SDK yoktur.

## Belgeler

- [docs/PRODUCT.md](docs/PRODUCT.md): ürün tanımı, kullanıcı hikayeleri, döngü tanımları
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): mimari, veri modeli, teknoloji kararları
- [docs/ROADMAP.md](docs/ROADMAP.md): aşamalar ve yapılacaklar
- [CLAUDE.md](CLAUDE.md): Claude Code için proje notları

## Çalıştırma

1. Android Studio ile projeyi aç, telefonu USB hata ayıklama ile bağla.
2. `app` yapılandırmasını çalıştır.
3. Uygulamada "Kullanım erişimi" iznini Ayarlar ekranından ver.
