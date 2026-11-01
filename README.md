# IPTV Player 📺📱

IPTV Player, **Android TV** ve **Mobil** cihazlar için özel olarak tasarlanmış, modern, yüksek performanslı ve kullanıcı dostu bir M3U oynatıcıdır. (Eski adıyla *M3U Stream*)

## 🌟 Temel Özellikler

* **Hibrit Arayüz (TV & Mobil):** Cihazın türünü otomatik algılar. Televizyonlar için D-Pad (kumanda) odaklı, telefonlar için ise dokunmatik odaklı akıcı bir arayüz sunar.
* **Gelişmiş Medya Oynatıcı:** Gücünü ExoPlayer'dan alır. Donanımsal hızlandırma desteklenir ve TV'lerin çözemediği MKV ses formatları için **FFmpeg Audio Fallback** eklentisini barındırır.
* **Gelişmiş Kumanda (D-Pad) Navigasyonu:** Ekranda kaybolmayı engelleyen, Netflix tarzı renk geçişli (Kırmızı/Beyaz) focus (odak) sistemi.
* **Otomatik Güncelleme (OTA):** Uygulama açılışında GitHub Releases üzerinden güncellemeleri kontrol eder ve uygulama içinden tek tıkla yeni sürümü kurar.
* **Ses ve Altyazı Seçimi:** Videoların içindeki gömülü ses ve altyazı izlerini sorunsuzca ayrıştırır. TV ekranına özel sağdan açılan hafif menü ile izleme deneyimini bölmeden seçim yapılmasını sağlar.
* **Kaldığın Yerden Devam Et:** İzleme geçmişiniz Room Database ile cihazda tutulur, dizilere ve filmlere her zaman kaldığınız saniyeden devam edebilirsiniz.
* **Performanslı Veritabanı & Ön Bellek:** 50.000+ satırlık M3U listelerini hızlıca tarar, Coil ImageLoader ile afişleri belleği yormadan (Cache) yükler.

---

## 🚀 Sürüm Notları ve Güncellemeler (Changelog)

### v0.6 - Akıllı Dizi Tekilleştirme & İzleme Geçmişi İyileştirmesi (Güncel Sürüm)
* **[YENİ] Akıllı Dizi Tekilleştirme:** Aynı diziye ait birden fazla bölüm izlendiğinde "Son İzlediklerim" listesinde yan yana yığılması engellendi. Her dizi için yalnızca en son izlenen bölüm gösterilir.
* **[GELİŞTİRME] Genişletilmiş Geçmiş Havuzu:** İzleme geçmişi sorgu limiti 50'ye çıkarılarak çok bölümlü dizi izlemelerinde diğer içeriklerin kaybolması önlendi.
* **[GELİŞTİRME] Akıllı Geçmişten Kaldırma:** Dizi kartı üzerindeki X butonuna basıldığında o dizinin tüm geçmiş bölümleri temizlenir, eski bölümlerin tekrar öne çıkması engellenir.
* **[GELİŞTİRME] İndirme & Çevrimdışı İzleme Altyapısı (Test):** Bölümleri yerel hafızaya kaydedip internetsiz oynatma için Room DB ve WorkManager altyapısı hazırlandı.

### v0.5 - Birleşik Oynatıcı & Mobil Dokunmatik İlerleme Çubuğu
* **[GELİŞTİRME] Birleşik Oynatıcı Tasarımı:** TV ve Mobil sürümlerdeki oynatıcı ilerleme çubuğu Netflix tarzı kırmızı halkalı ince tasarıma kavuşturuldu.
* **[GELİŞTİRME] Dokunmatik Jestler:** Mobil ekranda ilerleme çubuğuna dokunarak ve sürükleyerek saniye atlama desteği eklendi.
* **[HATA ÇÖZÜMÜ] Yavaş Yüklenen HLS Akışları:** Yavaş bağlantılarda kaydedilen izleme dakikasının sıfırlanmasını önleyen gecikmeli seek mantığı devreye alındı.

### v0.4 - Oynatıcı UI & Kumanda Odak İyileştirmesi
* TV kumandası için ses ve altyazı menüsünde odak tuzağı (Focus Trap) ve doğrudan ilk öğeye odaklanma sağlandı.
* Süre göstergeleri ilerleme çubuğunun sağına ve soluna taşındı.

### v0.3 - OTA ve TV Performans Güncellemesi
* **[YENİ] GitHub OTA Entegrasyonu:** Uygulama artık açılışta `Quendic/iptvplayer` reposunu kontrol ederek otomatik güncelleme uyarısı veriyor ve APK kurulumunu doğrudan kendi içinden yapıyor.
* **[GELİŞTİRME] Saf Siyah Oynatıcı:** Film ve dizilerde alt/üstte kalan boşlukların ekran paneline tam uyum sağlaması için arka plan rengi saf siyaha (OLED Black) çevrildi.
* **[OPTİMİZASYON] Düşük Donanım TV İyileştirmeleri:** 
    * Ağır `Slider` bileşeni TV'lerde hafif `LinearProgressIndicator`'a dönüştürüldü (Mobilde Slider korundu).
    * `ModalBottomSheet` yerine animasyonlu hafif yan panel (Overlay) sistemine geçildi.
    * Recomposition hatalarını önlemek için `.scale()` animasyonları Compose'un çizim aşamasına (`.graphicsLayer`) taşındı.
    * Room Veritabanına `Index`'ler eklendi ve tüm ViewModel/State yapıları `@Immutable` olarak işaretlendi.
    * Coil 3 ImageLoader ön bellek optimizasyonları ve Ktor HTTP zaman aşımı ayarları (Timeout) yapıldı.
* **[HATA ÇÖZÜMÜ] Odak Kayıpları:** TV kumandasıyla oynatıcı butonlarına basılamama / odaklanamama sorunu Modifier sıralaması düzeltilerek giderildi.

### v0.2 - Medya Çözücü & UI Yenilenmesi
* **[YENİ] FFmpeg Ses Çözücü:** Cihazların (özellikle düşük donanımlı Android Box'ların) donanımsal olarak desteklemediği MKV vb. ses formatlarının oynatılabilmesi için `media3-ffmpeg-decoder` eklendi (`EXTENSION_RENDERER_MODE_ON`).
* **[YENİ] Markalaşma:** Uygulama adı "IPTV Player" olarak güncellendi, Android TV uyumlu yüksek çözünürlüklü Banner ve Launcher (Simge) tasarımları projeye dahil edildi.
* **[GELİŞTİRME] Hover Efektleri:** Eski çerçeve (border) odaklanma efekti kaldırılarak yerine modern metin ve ikon renk geçişi (Kırmızı/Beyaz) getirildi.
* **[GELİŞTİRME] D-Pad Tuş Haritası:** Focus tuzaklarını (focus trap) engelleyen özel `focusProperties` rotaları oluşturuldu.

### v0.1 - İlk Çıkış
* M3U listelerini okuyup Film, Dizi, Canlı TV kategorilerine ayıran çekirdek sistem.
* ExoPlayer entegrasyonu.
* Temel arama (Search) ve favoriye ekleme (Favorites) altyapısı.
