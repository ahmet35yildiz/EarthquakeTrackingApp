# QuakeAlert

> **Taslak (Türkçe).** Geliştirme bitene kadar Türkçe tutulur, teslimden önce İngilizceye çevrilir.
> Kısa kalmalı; detaylar `docs/` altındaki dökümanlarda.

Dünya genelindeki son depremleri gösteren; kullanıcının belirlediği büyüklük eşiğini ve bölgeyi (şehir + yarıçap)
aşan yeni bir deprem olduğunda bildirim gönderen Android uygulaması.

## Ürün
- **Hedef kullanıcı:** Önemsediği bir yerde (kendi şehri, ailesinin şehri) anlamlı bir deprem olduğunda hızlıca
  haberdar olmak isteyen, ancak her gün olan küçük depremlerle rahatsız edilmek istemeyen kişi.
- **Yaklaşım:** Az ama doğru bildirim. Kullanıcı neyin önemli olduğunu bir kez seçer (minimum büyüklük ve isteğe
  bağlı şehir + yarıçap); uygulama arka planda USGS'i kontrol eder ve sadece eşleşen yeni depremler için bildirim
  gönderir.
- **Bu bir erken uyarı sistemi değildir:** Bildirimler depremden birkaç dakika sonra gelir (USGS yayın gecikmesi ve
  arka plan kontrol aralığı).

## Özellikler
_Geliştirme ilerledikçe doldurulacak (ekran görüntüleri: `docs/images/`)._

## Çalıştırma
_Faz 0 sonunda doldurulacak: Android Studio sürümü, JDK 17, emülatör gereksinimi (Google APIs imajı), adımlar,
bildirimi hemen denemek için Geliştirici → "Alarm simüle et"._

## Mimari
Feature-first Clean Architecture (her feature'da data / domain / presentation), sunum katmanında MVVM, tek modül.
Detay: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Önemli kararlar
Tüm kararlar gerekçeleriyle: [docs/DECISIONS.md](docs/DECISIONS.md). Özet:
- **Veri kaynağı USGS:** Global, anahtarsız ve iyi dökümante edilmiş; daire filtresi (`latitude`/`longitude`/
  `maxradiuskm`) ve geç yayınlanan depremleri yakalamak için `updatedafter` destekliyor.
- **Arka plan kontrolü WorkManager ile 15 dakikada bir:** Sunucu olmadan en güvenilir ve pil dostu yöntem.
- **Bölge modeli şehir + yarıçap:** Depremler sınır tanımaz, önemli olan mesafe. Ülke listesi platformdan
  (`Locale.getISOCountries()`), şehir koordinatı Android `Geocoder` ile; uygulamaya veri gömülmedi, ek servis yok.
- **Offline-first liste:** Son 7 gün, M2.5+ veri Room'da cache'lenir, filtreler yerelde çalışır.
- **Tekrarsız bildirim:** Bildirilen deprem id'leri saklanır; ayar değişikliği öncesi ve 6 saatten eski depremler
  bildirim üretmez.
- **Çok dilli altyapı:** İngilizce + Türkçe; yeni dil eklemek sadece bir `strings.xml` eklemekten ibaret.

## Ölçüm
Ürünün işe yarayıp yaramadığı yerel event kaydıyla ölçülür (Ayarlar → Geliştirici araçları → Olay kaydı). Ana metrik:
bildirim açılma oranı. Diğerleri: aktivasyon, bildirim izni oranı, "fazla gürültü" sinyali (bildirimden sonra eşiği
yükseltme / alarmı kapatma). Detay: [docs/ANALYTICS.md](docs/ANALYTICS.md).

## Kapsam dışı
| Konu | Neden |
|---|---|
| Push bildirim (FCM) + backend | Sunucu gerektirir; anında bildirimin tek yolu. Sonraki adımın ilk maddesi. |
| Harita SDK'sı | API key ve kurulum gerektirir; "Haritada aç" butonu ihtiyacı karşılıyor. |
| Hesap / senkronizasyon | Temel kullanım için çoklu cihaz ihtiyacı yok. |
| Birden fazla bölge | Ayarları karmaşıklaştırır; tek bölge ana ihtiyacı karşılıyor. |
| Widget | Temel akışın parçası değil. |
| Bölgesel veri kaynakları (AFAD, EMSC…) | Uygulama global; USGS tek entegrasyonla dünyayı kapsıyor. |

## Bilinen kısıtlar
- Bildirimler 15 dakikaya kadar (Doze modunda daha uzun) gecikebilir.
- USGS'in küçük depremlerdeki kapsamı bölgeye göre değişiyor (ör. Türkiye'de bir haftada USGS 2, AFAD 713 deprem).
- Şehir arama cihazın geocoding servisine ihtiyaç duyar; servis yoksa bölge "Tüm dünya" olarak kalır.
- Şehir-devletleri (Singapur, Monako) uygulama dilindeki adlarıyla aranmalı (TR'de "Singapur", EN'de "Singapore").
- USGS'in yer açıklaması (`place`) sadece İngilizce; uygulamanın ürettiği tüm metinler yerelleştirilmiştir.

## Harcanan süre
_Geliştirme sonunda `docs/PLAN.md` iş kaydından özetlenecek._

## Sonraki adımlar
1. **Backend + FCM push:** Sunucu USGS'i sürekli izler, eşleşen kullanıcılara anında push gönderir (Doze'da bile).
2. **Uzak analytics:** Yerel event kaydını Firebase Analytics gibi bir servise bağlamak — `AnalyticsTracker`
   arayüzüne yeni bir implementasyon eklemek yeterli.
3. _Kapsama giremediyse:_ OneTimeWork zinciriyle cihaz aktifken ~5 dakikalık kontrol.
4. _Kapsama giremediyse:_ Konuma göre bölge ("Konumumu kullan").
5. Bölgesel veri kaynakları ile küçük depremlerde daha iyi yerel kapsam.

## AI kullanımı
Kullanılan araçlar, devredilen işler ve doğrulama yöntemi: [docs/AI_USAGE.md](docs/AI_USAGE.md).
