# Mimari

## Genel akış

```
UsageStatsManager ──► Collector (WorkManager) ──► Room (usage_events)
                                                      │
                                                      ▼
                                              LoopDetector (saf Kotlin)
                                                      │
                                                      ▼
                                              Room (loop_events) ──► Repository ──► ViewModel ──► Compose UI
```

Üç ayrı sorumluluk vardır ve birbirinden bağımsız test edilebilmelidir:

1. **Toplama**: sistemden ham olayları alıp kendi veritabanımıza kopyalar.
2. **Tespit**: ham olaylardan döngü olayları çıkarır. Android'e bağımlı olmayan saf Kotlin.
3. **Sunum**: veritabanını okuyup ekrana çizer.

## Katmanlar ve paket yapısı

Tek modül (`:app`), paketlerle ayrım. MVP için çoklu modül gereksiz yük.

```
com.loopbreak
├── data
│   ├── usage        UsageStatsManager sarmalayıcı, Collector, WorkManager worker
│   ├── db           Room: Database, Dao, Entity
│   └── settings     DataStore
├── domain
│   ├── model        UsageEvent, Session, LoopEvent, LoopType
│   └── detect       LoopDetector, SessionBuilder (saf Kotlin, birim testli)
└── ui
    ├── onboarding
    ├── today
    ├── history
    ├── settings
    ├── components   Canvas grafikleri
    └── theme
```

Bağımlılık yönü: `ui → domain ← data`. `domain` Android sınıfı içermez.

## Teknoloji kararları

| Karar | Seçim | Neden |
|---|---|---|
| Veri kaynağı | `UsageStatsManager.queryEvents()` | Uygulama bazında ön plan/arka plan olaylarını verir, `AccessibilityService`'ten çok daha hafif ve az müdahaleci. |
| Kalıcılık | Room | Sistem olayları yalnızca birkaç gün saklıyor; geçmiş ve trend için kendi kopyamız şart. |
| Arka plan | WorkManager periyodik iş (~15 dk) | Pil dostu, yeniden başlatmadan sağ çıkar. Foreground service'e gerek yok, çünkü olaylar sonradan sorgulanabilir. |
| UI | Compose + Material 3 | Proje zaten Compose ile kurulu. Görünüm shadcn/ui token'ları ve bileşenleriyle (`ShadColors`, `Shad.kt`) özelleştirilmiş: zinc paleti, kenarlıklı kartlar, pill sekmeler, açık/koyu tema. |
| Grafik | Elle Canvas | Zaman çizelgesi ve ısı haritası basit; kütüphane bağımlılığı değmez. |
| DI | Hilt | ViewModel, Room DAO'ları ve WorkManager worker'ı (`@HiltWorker`) için standart çözüm. Test sırasında bağımlılıkları değiştirmeyi kolaylaştırır. Gerekli artifact'lar: `hilt-android`, `hilt-compiler` (KSP), `hilt-navigation-compose`, `hilt-work`. |
| Ayarlar | DataStore | SharedPreferences'ın modern karşılığı, Flow ile uyumlu. |
| Test | JUnit (domain) | Asıl risk tespit mantığında; onu birim testle sabitliyoruz. |

## Veri modeli (taslak)

**usage_events**: sistemden kopyalanan ham olaylar

| Alan | Tip | Not |
|---|---|---|
| id | Long (PK) | otomatik |
| packageName | String | |
| type | Int | RESUMED / PAUSED |
| timestamp | Long | epoch ms |

`(packageName, type, timestamp)` üzerinde unique index; aynı olay iki kez yazılmasın.

**loop_events**: tespit edilen döngüler

| Alan | Tip | Not |
|---|---|---|
| id | Long (PK) | |
| type | Int | RE_OPEN / QUICK_PEEK / PING_PONG |
| packageName | String | ping-pong için ilk uygulama |
| otherPackage | String? | ping-pong için ikinci uygulama |
| startTime / endTime | Long | |
| count | Int | pencere içindeki tekrar sayısı |

## Toplama mantığı

- Worker son senkron zamanını DataStore'da tutar, `queryEvents(lastSync, now)` çağırır.
- Yalnızca `ACTIVITY_RESUMED` ve `ACTIVITY_PAUSED` olaylarını alır (API 29+; eski adlar `MOVE_TO_FOREGROUND` / `MOVE_TO_BACKGROUND`). Min SDK 26 olduğu için ikisi de ele alınmalı.
- Uygulama açılışında da bir senkron çalışır, böylece ekran her zaman güncel olur.
- Tespit, yeni olaylar yazıldıktan sonra yalnızca etkilenen zaman aralığı için yeniden çalışır; tüm geçmişi baştan hesaplamaz.

## Tespit mantığı

1. `SessionBuilder`: RESUMED/PAUSED çiftlerinden `Session(package, start, end)` üretir. Eksik PAUSED (ör. cihaz kapandı) durumunda oturum sınırlı bir azami süreyle kapatılır.
2. `LoopDetector`: oturum listesini zaman sırasıyla gezer, [PRODUCT.md](PRODUCT.md) içindeki eşiklere göre `LoopEvent` üretir.
3. Eşikler bir `DetectionConfig` veri sınıfında toplanır; sabit kodlanmaz.

## İzinler

- `PACKAGE_USAGE_STATS`: kullanıcı Ayarlar → Kullanım erişimi ekranından elle verir. Manifest'te `tools:ignore="ProtectedPermissions"` gerekir.
- `INTERNET` **eklenmez**.
- `POST_NOTIFICATIONS`: yalnızca kullanıcı Ayarlar'dan nazik hatırlatmayı açarsa istenir (Android 13+).
- Paket görünürlüğü: uygulama adı/ikonunu göstermek için `QUERY_ALL_PACKAGES` ya da `<queries>` gerekir. Play Store dışı olduğu için `QUERY_ALL_PACKAGES` kabul edilebilir.

## Bilinen riskler

- **Üretici kısıtları**: bazı üreticiler (Xiaomi, Huawei, Samsung) arka plan işlerini agresif keser. Worker sıklığı garanti değildir; olaylar sonradan sorgulandığı için veri kaybı olmaz, sadece güncelleme gecikir.
- **Sistem olay saklama süresi**: birkaç günle sınırlı. İlk kurulumda geçmiş veri sınırlı olacak.
- **AGP 9 + Kotlin 2.2 ile KSP ve Hilt uyumu**: Room ve Hilt için KSP gerekir. KSP sürümü, projedeki Kotlin sürümüyle birebir eşleşmeli; Hilt Gradle eklentisinin sürümü de AGP 9'u desteklemeli. Eklerken Gradle çıktısına bakılmalı.
- **Hilt + WorkManager**: Worker'lar `@HiltWorker` ile yazılır ve `Application` sınıfı `Configuration.Provider` olarak `HiltWorkerFactory` vermelidir; varsayılan WorkManager başlatıcısı manifest'ten kaldırılır.
- **Eşiklerin kalibrasyonu**: başlangıç değerleri tahmindir; gerçek veriyle ayarlanacak.
