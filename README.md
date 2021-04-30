# IF3210-2021-Android-K3-15
## Work Out Application


## Deskripsi Aplikasi
Work It Out adalah aplikasi olahraga yang membantu pengguna dalam menjadwalkan dan mendata kegiatan workout. Pada aplikasi ini, terdapat 4 fungsionalitas utama yaitu:

1. Sport News: pengguna dapat melihat berita olahraga yang diperoleh melalui Indonesia Sports News API. Setiap berita dapat di-klik untuk menampilkan WebView dari konten berita.

2. Training Tracker: pengguna dapat melakukan "mulai" dan "selesai" mengenai pelacakan latihan yang memiliki beberapa jenis Cycling dan Walking. Pelacakan akan disimpan historynya dalam bentuk pencapaian (dan rute cycling) dan ditampilkan pada saat berakhir.

3. Training History: terdapat halaman history yang menyimpan seluruh kegiatan latihan pengguna yang dilacak, ditampilkan dalam bentuk kalender. Setiap hari pada kalender dapat dibuka untuk menampilkan list log latihan dan setiap log dapat dibuka untuk melihat detailnya.

4. Training Scheduler: pengguna dapat menentukan jadwal latihan yang dapat dikustomisasi untuk berjalan secara satu waktu spesifik dan 	rutin (setiap hari atau pada hari tertentu). Scheduler dapat diatur untuk secara otomatis melakukan tracking.


## Cara Kerja
Aplikasi dapat di-build dengan cara mendefinisikan variable berikut pada file local.properties:
```
MAPS_API_KEY=<--REDACTED-->
NEWS_API_KEY=<--REDACTED-->
ENABLE_ALERT_WINDOW=<true|false>
```

ENABLE_ALERT_WINDOW digunakan untuk melakukan over the top display untuk menampilkan history (tidak support pada Android Go sehingga digunakan variable ENV).

### Sport News (***SUKSES***)
1. Aplikasi WorkItOut kemudian dapat dibuka sehingga menampilkan tampilan awal berupa halaman Sport News. Untuk menampilkan halaman Sport News, kami menggunakan `interface client dan service Retrofit` untuk mengambil data dari News API melalui `view model` pada News. Dengan menggunakan view model, kita dapat menghindari fetching ulang akibat perubahan orientasi sehingga lebih efisien dalam penggunaan bandwidth. Data response dari News API akan diproses dan di-passing kepada `live data`. Live data tersebut telah di-observe oleh `NewsFragment` sehingga perubahannya akan diupdate pada sisi UI.

<center><img src="/uploads/287fd67dfde9c6e2db4fc15f67fdd2bf/Screenshot_1619753044.png" alt="SportsNewsUpright" width="200"/></center>

2. Tampilan list berita ditampilkan secara ***responsive***, yaitu pada landscape, akan ditampilkan menjadi 2 kolom (dengan grid). Untuk menghandle responsive ini, kami memanfaatkan `grid layout` untuk menampilkan recyclerview dan data `configuration` pada `activity's resource` berupa orientasi yang akan dicek apakah merupakan landscape atau portrait. Jika orientasi dalam landscape, grid layout akan di-set dengan 2 kolom. Untuk portrait, grid layout akan di-set dengan 1 kolom.

![SportsNewsLandscape](/uploads/ad9f55fb1e8d56aaa8f74828b4f6db3d/Screenshot_1619754107.png)

3. Jika suatu berita di-klik, akan menampilkan WebView dari konten beritanya. Untuk men-passing data URL dari berita, kami menggunakan argument pada library `Navigation`. Argument dapat di-pass melalui `action` yang dapat di-generate menggunakan kelas `direction` dari hasil build. URL tersebut kemudian akan diberikan kepada WebView untuk ditampilkan.

![NewsContent](/uploads/deb975515c9030e46f85fbfa2c624a42/NewsContent.png)

### Tracker
1. Klik navigasi Tracker di bawah sehingga halaman Tracker akan terbuka.

![Tracker](/uploads/411f7fb9de353ef2f6893c3a6076f2ec/Screenshot_1619753078.png)

2. ***Kompas*** akan berotasi menyesuaikan arah mata angin

![Compass](./screenshot/compass.jpg)

3. Tracking dapat dilakukan dengan memilih tipe latihan (Cycling atau Walking) dan menekan tombol Start Track.

![TrainingInProgress](./screenshot/tracker_inProgress.jpg)

4. Notifikasi yang ditampilkan menunjukkan progress latihan, yang berjalan pada ***foreground service***.
- Untuk Cycling

![notification_cycling](./screenshot/foreground_cycling.jpg)

- Untuk Walking

![notification_running](./screenshot/foreground_running.jpg)

5. Tracking dihentikan sehingga menyimpan history dan menampilkannya menggunakan intent pada service. (Pada Android Go, digunakan notifikasi untuk menampilkan history)

![CyclingDetail](/uploads/872dc7cb69899831d5378b3438e7026b/CyclingDetail.png)

### Training History
1. Klik navigasi History di bawah sehingga halaman History akan terbuka.

![TrainingHistory](/uploads/503c5ce56bb589724e71e07901d629dd/Screenshot_1619753084.png)

2. Klik suatu tanggal pada kalendar untuk menampilkan log list
- Jika ada log

![HistoryListFilled](/uploads/ed382ab615d9b7767349718f8992c6a3/HistoryListFilled.png)

- Jika tidak ada log

![HistoryListEmpty](/uploads/90bb7daebf378f15fe7a78bb2ac49e40/HistoryListEmpty.png)

3. Jika terdapat log, klik panah hijau pada log untuk menampilkan detail log tersebut.
- Untuk Cycling

![CyclingDetail](/uploads/872dc7cb69899831d5378b3438e7026b/CyclingDetail.png)

- Untuk Walking

![HistoryDetailSteps](/uploads/bdb708939d00eedf10d29b3db53174ed/HistoryDetailSteps.png)

4. Selain itu, log history dan detailnya juga dapat ditampilkan secara ***responsive menggunakan fragment***.

![ResponsiveLog](/uploads/eb51bbc521a708df67adb9d5bafcaf27/ResponsiveLog.png)

### Training Scheduler
1. Klik navigasi Schedule di bawah sehingga halaman Schedule akan terbuka.

![TrackerDefault](/uploads/1a48df572be06248f43da8ea2e5ce48b/TrackerDefault.png)

2. Anda dapat mengaktifkan auto track dengan menekan tombol switch pada kanan atas.

![TrackerAutoTrackOn](/uploads/336c1b53b0ea6d445e4be6a1c7b260b4/TrackerAutoTrackOn.png)

3. Anda juga dapat menambahkan schedule baru dengan menekan tombol floating button pada kanan bawah sehingga ditampilkan dialog tipe schedule.

![TrackerChooseType](/uploads/24a57a456154d4921d4260caac62109a/TrackerChooseType.png)

4. Anda dapat memilih untuk menambahkan schedule satu waktu atau rutin (setiap hari atau pada hari tertentu).
- Satu waktu

![TrackerSingleType](/uploads/613854eb87aaf6d0e8472e94701d0f9d/TrackerSingleType.png)

- Rutin

![TrackerRoutineType](/uploads/7314967b18a27850e82307c6c914a86f/TrackerRoutineType.png)

5. Untuk mengisi date dan time, digunakan dialog fragment.

***GAMBAR***

***GAMBAR***

6. Untuk mengisi schedule rutin, klik tombol hari yang diinginkan (bisa lebih dari satu).

![TrackerTimeDialog](/uploads/6d274a0b3a1824eae620f1c5a7253463/TrackerTimeDialog.png)

![TrackerFilled](/uploads/0228a53e87a5f1fc6b462ac5691b1667/TrackerFilled.png)

7. Notifikasi akan ditampilkan saat schedule dimulai dan berakhir.

***GAMBAR***


8. Auto-track yang dilakukan saat schedule dimulai dan berakhir.

***GAMBAR***


## Library yang Digunakan

### Constraint Layout
Library ini digunakan untuk mendefinisikan Contraint Layout pada layout.

### AppCompat
Library ini digunakan untuk mendefinisikan AppCompatActivity yang mensupport Material Design.

### Google Material Design
Library ini digunakan untuk mendefinisikan komponen-komponen material seperti Snack dan FloatingActionButton.

Contoh penggunaan dari library ini pada aplikasi kami adalah:
- MainActivity: untuk mendefinisikan BottomNavigationView
- ScheduleFragment (pada package ui.schedule): untuk mendefinisikan FloatingActionButton

### Room Persistence
Library ini digunakan untuk membaca dan menuliskan data pada SQLite dengan cara memberikan layer abstraksi.

Contoh penggunaan dari library ini pada aplikasi kami adalah:
- kelas-kelas entity (pada package entity) : untuk mendefinisikan tabel-tabel pada SQLite
- interface dao (pada package dao): untuk mendefinisikan operasi query berupa select, delete, insert, dan update pada entity
- kelas-kelas converter (pada package entity): untuk melakukan konversi antara data pada SQLite dan data pada Kotlin (berlaku 2 arah)
- kelas database (pada package database): untuk mendefinisikan database pada SQLite yang menyimpan entity.

### Retrofit
Library ini digunakan untuk mengambil data dari sebuah REST API dan di-mapping ke sebuah data class.

Contoh penggunaan dari library ini pada aplikasi kami adalah:
- NewsAPIClient (pada package data.api): untuk membangun klien yang dapat mengakses data API 	
- NewsAPIService (pada package data.api): untuk mendefinisikan REST API yang digunakan dan data yang diterimanya
- NewsViewModel (pada package data.api): untuk melakukan pemanggilan REST API News, serta memperoleh response dan error

### Navigation
Library ini digunakan untuk mendefinisikan dan mengatur navigasi.

Contoh penggunaan dari library ini pada aplikasi kami adalah:
- MainActivity: untuk mendefinsikan dan mensetup navigation ui (bottom navigation)
- AddScheduleFragment (pada package ui.schedule): untuk memperoleh argument pada fragment tersebut (safe args)
- ScheduleTypeDialog (pada package ui.schedule): untuk melakukan navigasi ke fragment AddScheduleFragment setelah menekan tombol

### Lifecycle
Library ini digunakan untuk mendefinisikan komponen lifecycle seperti ViewModel dan live data

Contoh penggunaan dari library ini pada aplikasi kami adalah:
- HistoryViewModel (pada package ui.history): untuk mendefinisikan ViewModel dan providernya, serta mendefinisikan live data

### Kotlin Coroutine
Library ini digunakan untuk mendefinisikan coroutine yang mengerjakan suatu task dan berjalan di luar UI thread.

Contoh penggunaan dari library ini pada aplikasi kami adalah:
- WorkOutApplication: untuk mendefinisikan application scope coroutine
- ScheduleViewModel (pada package ui.schedule): untuk menjalankan operasi pada viewmodel scope sehingga tidak membebani UI thread (non-blocking)
- TrackingService (pada package services): untuk melakukan insert history dan memperoleh id
- AddScheduleFragment (pada package ui.schedule): untuk melakukan insert schedule dan memperoleh id
- ScheduleReceiver (pada package receivers): untuk mengambil data schedule yang berkaitan
- ScheduleDao (pada package dao): untuk memperoleh data Flow, async data yang akan diupdate jika terjadi perubahan data pada database

### Google Mobile Services
Library ini digunakan untuk menyediakan servis google pada mobile berupa location tracking dan Map API.

Contoh penggunaan dari library ini pada aplikasi kami adalah:
- HistoryDetailFragment (pada package ui.history): untuk me-load dan menampilkan google map (rute cycling) pada 
- TrackingService (pada package services): untuk memperoleh informasi lokasi sekarang secara berkala

### Glide
Library ini digunakan untuk melakukan fetching gambar dan menampilkan.

Contoh penggunaan dari library ini pada aplikasi kami adalah:
- NewsAdapter (pada package ui.news): untuk mengambil gambar headline news dan menampilkannya pada ImageView


## Screenshot Aplikasi

![SportsNewsUpright](/uploads/287fd67dfde9c6e2db4fc15f67fdd2bf/Screenshot_1619753044.png)

![SportsNewsLandscape](/uploads/ad9f55fb1e8d56aaa8f74828b4f6db3d/Screenshot_1619754107.png)

![Tracker](/uploads/411f7fb9de353ef2f6893c3a6076f2ec/Screenshot_1619753078.png)

![TrainingHistory](/uploads/503c5ce56bb589724e71e07901d629dd/Screenshot_1619753084.png)

![HistoryLogWalking](/uploads/3e4491ed5c6f2db9646dbdeb25947a7b/Screenshot_1619753101.png)

![ScehduledExercises](/uploads/e5a1f5048e05944aa53be60e6886fb9d/Screenshot_1619753325.png)

## Pembagian Kerja
1. Muhammad Ravid Valiandi (13518099)
2. Muhammad Firas (13518117)
3. Yonatan Viody (13518120)
