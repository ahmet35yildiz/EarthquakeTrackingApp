# QuakeAlert

> **Taslak (Türkçe).** Teslimden önce İngilizceye çevrilecek.

Dünya genelindeki son depremleri gösteren, kullanıcının seçtiği büyüklük eşiğine ve bölgeye (şehir + yarıçap) uyan
yeni bir deprem olduğunda bildirim gönderen Android uygulaması. Veri kaynağı USGS.

## Ürün
- **Hedef kullanıcı:** Önemsediği bir yerde (kendi şehri, ailesinin şehri) anlamlı bir deprem olduğunda hızlıca
  haberdar olmak isteyen, ama her gün olan küçük depremlerle rahatsız edilmek istemeyen kişi. Dünyanın her yerinden.
- **Temel ihtiyaç:** "Beni ilgilendiren bir deprem olduğunda haber ver; nerede, kaç büyüklüğünde, şehrime ne kadar
  uzak ve ne kadar derin olduğunu hızlıca görebileyim."
- **Yaklaşım:** Az ama doğru bildirim. Kullanıcı neyin önemli olduğunu bir kez seçer (minimum büyüklük ve isteğe
  bağlı şehir + yarıçap), uygulama arka planda USGS'i kontrol eder ve sadece eşleşen yeni depremler için bildirim
  gönderir.
- **Bu bir erken uyarı sistemi değildir.** Bildirimler depremden dakikalar sonra gelir (USGS'in yayın gecikmesi ve
  arka plan kontrol aralığı). Uygulama bunu onboarding'de açıkça söyler.

## Kapsam
- **Onboarding (3 adım):** tanıtım ve "erken uyarı değildir" notu → alarm kurulumu → bildirim izni.
- **Deprem listesi:** son 7 gün, M2.5+, tüm dünya. Bölge ve büyüklük filtreleri, sıralama (en yeni / en büyük /
  en yakın), çekerek yenileme; internet yokken son veri gösterilir.
- **Deprem detayı:** büyüklük, yer, yerel saat + UTC, derinlik, koordinat, şehre uzaklık, inceleme durumu, tsunami
  işareti. Haritada aç, "Hissettim" (USGS'in "Did You Feel It?" formu açılır), USGS'te görüntüle, paylaş.
- **Alarm ayarları:** aç/kapa, eşik (M2.5–8.0, 0.5 adım, varsayılan M4.5), bölge: tüm dünya (uyarıyla birlikte) veya
  ülke → şehir arama → yarıçap (50–1000 km, varsayılan 250 km). Şehir, "Konumumu kullan" ile tek dokunuşla da
  seçilebilir (yalnızca yaklaşık konum izni). Her değişiklik anında kaydedilir.
- **Arka plan kontrolü ve bildirimler:** 15 dakikada bir kontrol; en fazla 3 ayrı bildirim, daha fazlası için tek
  özet. Bildirime dokununca ilgili deprem detayı açılır.
- **Acil Durum sekmesi:** çakar flaş (telefon feneri saniyede iki kez yanıp söner, ekran açık kalır) ve deprem öncesi /
  anında / sonrası güvenlik rehberi (internetsiz çalışır).
- **Ayarlar:** uygulama dili (sistem, English, Türkçe), tema (sistem, açık, koyu), bildirim izni durumu, hakkında.
- **Geliştirici araçları (sadece debug build):** alarm simülasyonu, hemen kontrol, event log.
- Açık ve koyu tema, yatay ekran ve büyük yazı desteği. USGS'in İngilizce yer metinleri seçili dilde gösterilir
  (mesafe, yön, ülke, bölge ifadeleri ve bilinen okyanus/sırt/ada adları çevrilir; şehir adları olduğu gibi kalır).

## Çalıştırma
**Gereksinimler:** Android Studio Quail 4 (2026.1.4) veya üstü (AGP 9.4 bunu gerektiriyor), JDK 17+, Android SDK 36.
Emülatör ya da cihaz Android 8.0 (API 26) veya üstü olmalı. Şehir arama cihazın geocoding servisini kullandığı için
emülatörde **Google APIs** imajı gerekir.

1. Repoyu klonlayın ve Android Studio'da açın. Gradle sync'in bitmesini bekleyin.
2. `app` konfigürasyonunu bir emülatörde veya cihazda çalıştırın (ya da `./gradlew installDebug`).
3. Onboarding'i tamamlayın: eşik ve bölge seçin, bildirim iznini verin.

**Bildirimi beklemeden denemek için:** Settings → Developer tools → Alert testing.
- **Simulate now:** Seçilen büyüklük ve uzaklıkta bir test depremi üretir. Test depremi gerçek alarmlarla aynı
  eşleştirme ve bildirim adımından geçer; ayarlarınıza uyuyorsa bildirim gelir, uymuyorsa gelmez.
- **Schedule:** Aynı test depremini seçilen gecikmeden sonra, uygulama kapalıyken de gönderir.
- **Run check now:** Gerçek USGS kontrolünü hemen çalıştırır.
- **Event log:** Kaydedilen tüm event'leri gösterir.

**Testler:**
```bash
./gradlew testDebugUnitTest            # 440 unit test
./gradlew connectedDebugAndroidTest    # 88 instrumented test (emülatör açık olmalı)
./gradlew lintDebug
```

## Önemli kararlar
Tüm kararlar alternatifleriyle birlikte [docs/DECISIONS.md](docs/DECISIONS.md) dosyasında (46 kayıt). En önemlileri:

1. **Veri kaynağı: USGS.** Uygulama global. USGS anahtarsız, dünyayı tek entegrasyonla kapsıyor ve bu ürün için
   gereken iki özelliği doğrudan destekliyor: daire filtresi (`latitude`/`longitude`/`maxradiuskm`) ve geç yayınlanan
   depremleri yakalamak için `updatedafter`. Bölgesel kaynaklar (AFAD, Kandilli) sadece Türkiye'yi kapsıyor.
2. **Bölge modeli: şehir + yarıçap.** Depremler sınır tanımaz; kullanıcı için önemli olan mesafe. USGS'te ülke veya
   şehir filtresi yok; yer açıklamasından ülke ayıklamak güvenilir değil, ülke sınır kutuları da komşu ülkeleri
   kapsıyor. Ülke seçimi sadece şehir aramasını daraltıyor.
3. **Ek bağımlılık ve gömülü veri yok.** Ülke listesi Android'in kendi listesinden (`Locale.getISOCountries()`),
   şehir koordinatı Android `Geocoder` ile alınıyor. Geocoder, Android 13 ve sonrasında asenkron, öncesinde arka plan
   thread'inde çalışan tek bir sarmalayıcının arkasında. Her iki yol da API 31 ve API 34'te test edildi.
   "Konumumu kullan" da Android'in kendi konum servisini kullanıyor (Play Services yok) ve sadece yaklaşık konum
   izni istiyor: en küçük yarıçap 50 km olduğu için ~2 km hassasiyet yeterli. Konum dairenin merkezi olur, Geocoder
   sadece adını bulur.
4. **Arka plan kontrolü: WorkManager, 15 dakikada bir.** Sunucu olmadan en güvenilir ve pil dostu yöntem.
   - Exact alarm, kullanıcının ayrıca vermesi gereken bir izin istiyor.
   - Foreground service ise sürekli görünen bir bildirim demek.
   - Anında bildirimin tek yolu sunucu + push; bu yüzden sonraki adımların ilki.
5. **Tekrarsız ve geriye dönük olmayan bildirim.** Bildirilen deprem id'leri saklanır. Ayar değişikliğinden önceki ve
   6 saatten eski depremler bildirim üretmez. USGS'in geç yayınladığı depremler `updatedafter` ile yakalanır.
6. **Offline-first liste.** Son 7 günün M2.5+ verisi (~370 deprem) Room'da tutulur. Filtre ve sıralama yerelde,
   ana thread dışında çalışır; internet yokken son veri, tarihiyle birlikte gösterilir.
7. **Mimari: feature-first Clean Architecture, sunumda MVVM, tek modül.** Feature'lar (`earthquakes`, `alerts`,
   `settings`, `eventlog`) kendi data / domain / presentation katmanlarına sahip; ortak kod `core`'da. Feature'lar
   arasındaki tek bağımlılık `alerts` → `earthquakes.domain`. Ayarlanabilir değerler (eşik aralığı, yarıçaplar,
   aralıklar) tek bir config nesnesinde toplandı. Detay: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
8. **Çok dilli altyapı.** Şu an İngilizce ve Türkçe. Yeni dil eklemek için tek bir `values-<dil>/strings.xml` eklemek
   yeterli; dil listesi ve sistemin dil ayarı build sırasında otomatik üretiliyor.
9. **Simüle edilmiş alarmlar.** Gerçek alarm gerçek bir depreme ve 15 dakikalık kontrole bağlı. Geliştirici araçları
   bildirim akışını saniyeler içinde, gerçek eşleştirme ve bildirim adımından geçirerek gösteriyor.

## Ölçüm
Event'ler cihazda Room'a kaydedilir (harici servis yok) ve Event log ekranından incelenebilir. Koordinat veya şehir
adı kaydedilmez. Metrik tanımları ve event sözlüğü: [docs/ANALYTICS.md](docs/ANALYTICS.md).

| Soru | Metrik |
|---|---|
| Alarmlar işe yarıyor mu? (**ana metrik**) | Bildirim açılma oranı ve açılma süresi |
| Yeni kullanıcı kurulumu bitiriyor mu? | Onboarding tamamlama oranı, bildirim izni oranı |
| Alarmlar fazla mı gürültülü? | Bildirimden sonraki 24 saat içinde eşiği yükseltme veya alarmı kapatma |
| Liste tek başına faydalı mı? | Liste görüntüleme başına detay görüntüleme, harita / paylaş kullanımı |
| Altyapı güvenilir mi? | Arka plan kontrolü ve liste yenileme başarı oranı |

## Kapsam dışı
| Konu | Neden |
|---|---|
| Push bildirim (FCM) + backend | Sunucu gerektirir; anında bildirimin tek yolu. Sonraki adımların ilki. |
| Harita SDK'sı | API anahtarı ve kurulum gerektirir; "Haritada aç" ihtiyacı karşılıyor. |
| Hesap / senkronizasyon | Temel kullanım için birden fazla cihaz gerekmiyor. |
| Birden fazla bölge | Ayarları karmaşıklaştırır; tek bölge ana ihtiyacı karşılıyor. |
| Widget | Temel akışın parçası değil. |
| Bölgesel veri kaynakları (AFAD, EMSC…) | Uygulama global; USGS tek entegrasyonla dünyayı kapsıyor. |
| İngiliz ölçü birimleri | Mesafeler sadece kilometre. |

## Bilinen kısıtlar
- Bildirimler 15 dakikaya kadar gecikebilir; cihaz uzun süre boşta kalırsa (Doze) daha da uzun.
- USGS'in küçük depremlerdeki kapsamı bölgeye göre değişiyor. Örneğin Türkiye için bir haftada USGS'te 2, AFAD'da
  713 deprem vardı.
- Şehir arama cihazın geocoding servisine ihtiyaç duyar; servis yoksa bölge "Tüm dünya" olarak kalır.
- Şehir-devletleri (Singapur, Monako) uygulama dilindeki adlarıyla aranmalı (TR'de "Singapur", EN'de "Singapore").
- USGS'in yer açıklaması (ör. "15 km SSW of Hilvan, Turkey") sadece İngilizce; uygulamanın ürettiği tüm metinler
  yerelleştirilmiş.

## Harcanan süre
Toplam **~11 saat 5 dakika** (2026-09-25 – 2026-09-27, iş kaydından).

| İş | Süre |
|---|---|
| Analiz, araştırma, ürün ve mimari kararlar, planlama | 1s 15dk |
| Altyapı: araç zinciri, bağımlılıklar, iskelet, çoklu dil, tasarım sistemi | 1s 47dk |
| Çekirdek + deprem listesi ve detayı | 2s 43dk |
| Alarmlar: şehir arama, ayarlar, bildirimler, arka plan kontrolü, onboarding, geliştirici araçları | 3s 5dk |
| Ayarlar, event log, cila, instrumented testler, API 31 + 34 QA | 2s 15dk |

## Sonraki adımlar
1. **Backend + FCM push:** Sunucu USGS'i sürekli izler ve eşleşen kullanıcılara anında push gönderir (Doze'da bile).
2. **Uzak analytics:** Yerel event kaydını Firebase Analytics gibi bir servise bağlamak. `AnalyticsTracker`
   arayüzüne yeni bir implementasyon eklemek yeterli.
3. **Bölgesel veri kaynakları:** Küçük depremlerde daha iyi yerel kapsam için (ör. Türkiye için AFAD).

## AI kullanımı
**Araçlar:**
- **Claude Code (VS Code eklentisi):** araştırma, planlama dökümanları, kod, testler, emülatör testleri.
- **Google Stitch:** ekran tasarımları ve tasarım sistemi. Tasarım, Claude Code'dan Stitch MCP sunucusu üzerinden
  okunup uygulandı.

**Devredilen işler:**
- Araştırma: USGS API parametrelerinin gerçek isteklerle denenmesi, Android'in arka plan ve Geocoder kısıtları.
- Planlama dökümanlarının yazılması (spec, mimari, karar kayıtları, test planı), benim verdiğim kararlara göre.
- Stitch için tasarım brief'inin yazılması; Stitch'teki tasarımın MCP ile okunup renk, tipografi ve ekranlara
  uygulanması.
- Kodun yazılması: plandaki her görev, unit ve instrumented testleriyle birlikte.
- Emülatör testleri (adb ile), İngilizce ve Türkçe metinler.

**Bende kalanlar:** Hedef kullanıcı, kapsam ve öncelikler; veri kaynağı, bölge modeli ve arka plan yöntemi gibi ürün
kararları; her görevin incelenmesi ve kabulü; gerektiğinde değişiklik istemek (ör. liste sıralaması, geliştirici
araçlarının ayrı ekrana taşınması, parametreli alarm simülasyonu).

**Çıktıların doğrulanması:**
- API davranışı, üzerine tasarım yapılmadan önce gerçek isteklerle denendi.
- Her görev `assembleDebug`, `testDebugUnitTest` ve `lintDebug` geçmeden tamamlanmış sayılmadı.
- Her görev API 31 ve API 34 emülatörlerinde elle doğrulandı. Android 13 öncesi ve sonrası farklı çalışan kısımlar
  (Geocoder, bildirim izni, uygulama dili) iki tarafta da test edildi.
- Son aşamada tam QA matrisi iki emülatörde çalıştırıldı ([docs/TESTING.md](docs/TESTING.md)).
- Emülatörde bulunan hatalar kök nedenleriyle düzeltildi. Örnekler: bildirim izni tanımının eksik olması, arka
  planda bildirim dilinin yanlış gelmesi, uygulama kapatıldıktan sonra bildirime dokununca yanlış ekranın açılması.
  Tasarımı değiştiren düzeltmeler karar kayıtlarına işlendi.

**Usage Report:** Bu projenin tüm Claude Code oturumlarında tek model kullanıldı: %100 Claude Opus 5.5. Oturum bazında
token dağılımı ve raporun nasıl üretildiği: [docs/AI_USAGE.md](docs/AI_USAGE.md).
