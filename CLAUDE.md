# LoopBreak: Claude Code notları

Telefon kullanımındaki açma-kapama döngülerini tespit edip gösteren Android uygulaması. Ayrıntılar: [README.md](README.md), [docs/PRODUCT.md](docs/PRODUCT.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/ROADMAP.md](docs/ROADMAP.md).

## Teknoloji

Kotlin, Jetpack Compose (Material 3), Room, WorkManager, DataStore, Coroutines/Flow, Navigation Compose, Hilt. Tek modül `:app`, paket `com.loopbreak`.

## Kurallar

- **Gizlilik**: `INTERNET` iznini, analitik ya da üçüncü taraf SDK'yı ekleme. Veri cihazdan çıkmaz.
- **Katman yönü**: `ui → domain ← data`. `domain` paketi Android sınıfı içermez; tespit mantığı saf Kotlin kalır ve birim testle doğrulanır.
- **Eşikler** (`DetectionConfig`) sabit kodlanmaz, tek yerde durur.
- Bağımlılıklar `gradle/libs.versions.toml` üzerinden eklenir; sürümü doğrudan `build.gradle.kts` içine yazma.
- Room ve Hilt için KSP kullan (kapt değil). KSP sürümü projedeki Kotlin sürümüyle eşleşmeli ve Hilt eklentisi AGP 9 ile uyumlu olmalı. Eklerken derleyip doğrula.
- `domain` paketi Hilt'e de bağımlı olmasın; `@Inject constructor` yerine bağımlılıkları `data`/`ui` katmanındaki modüllerde bağla.
- Play Store şimdilik kapsam dışı. Sadece debug APK ile kendi cihazında çalışacak şekilde ilerle.
- Aşamaları [docs/ROADMAP.md](docs/ROADMAP.md) sırasıyla takip et; bir aşama bittiğinde işaretle.

## Tasarım (shadcn/ui)

Arayüz ui.shadcn.com tasarım dilini izler (zinc paleti). Renk token'ları `ui/theme/Color.kt` içinde (`ShadColors`, `Shad.colors`), bileşenler `ui/components/Shad.kt` içinde (`ShadCard`, `ShadButton`, `ShadIconButton`, `ShadBadge`, `ShadTabs`, `ShadSeparator`). Marka rengi teal (`primary`: açık #0F766E, koyu #2DD4BF); tür renkleri: tekrar dönüş teal, refleks açma amber (`warning`), ping-pong mor (`violet`). Öne çıkan öğeler: `LoopMark` (logo), `RadialHourChart` (24 saatlik halka, ortada skor), `heroBrush()` (hero kartların degradesi), `ShadProgress`, `ShadBadge(dot=...)`. Yazı tipleri `res/font` içinde gömülü değişken fontlar: Space Grotesk (başlıklar, skor), Inter (gövde), JetBrains Mono (saatler); tanımları `ui/theme/Type.kt`. Yeni font eklersen lisansını `licenses/fonts` altına koy. Yeni ekranlarda Material bileşenlerini doğrudan kullanmak yerine bunları kullan; dinamik renk kapalı, kartlar gölgesiz ve 1dp kenarlıklı.

## Sürüm notları

- `compileSdk = 37`: `core-ktx`, navigation ve lifecycle'ın güncel sürümleri bunu istiyor (`targetSdk` 36 kalır).
- AGP 9'da Kotlin yerleşik; ayrıca `kotlin-android` eklentisi yok. KSP 2.3.13 ve Hilt 2.60.1 bu kurulumla derleniyor.
- `DetectionConfig` varsayılanları: tekrar dönüş 10 dk içinde ≥ 2, refleks açma < 5 sn, ping-pong 5 dk içinde 3 geçiş.

- Varsayılan hariç listesini ya da tespit kurallarını değiştirirsen `LoopRepository.DETECTION_VERSION` değerini artır; saklanan geçmiş bir sonraki eşitlemede yeniden hesaplanır.
- Launcher tespiti `FallbackHome` etkinliklerini yok sayar (Ayarlar uygulaması HOME kategorisi için bir tane kaydediyor ve aksi halde launcher sanılıyor).

## Komutlar

```bash
./gradlew assembleDebug        # debug APK derle
./gradlew testDebugUnitTest    # birim testleri
./gradlew installDebug         # bağlı cihaza kur
```

## Dil

Kullanıcıyla ve belgelerde Türkçe; kod, tanımlayıcılar ve commit mesajları İngilizce.
