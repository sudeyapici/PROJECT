import java.io.*;

public class Main {
    static final int MONTHS = 12;
    static final int DAYS = 28;
    static final int COMMS = 5;

    static String[] commodities = {"Gold", "Oil", "Silver", "Wheat", "Copper"};
    static String[] months = {"January","February","March","April","May","June",
            "July","August","September","October","November","December"};


    static int[][][] data = new int[MONTHS][DAYS][COMMS];

    // ======== REQUIRED METHOD LOAD DATA ========
    public static void loadData() {

        for (int ay = 0; ay < MONTHS; ay++) {
            for (int gun = 0; gun < DAYS; gun++) {
                for (int urun = 0; urun < COMMS; urun++) {
                    data[ay][gun][urun] = 0;
                }
            }
        }

        // ayların dosyasını okuma
        for (int ay = 0; ay < MONTHS; ay++) {
            String dosyaAdi = months[ay] + ".txt";
            BufferedReader br = null;
            try {
                // Try standard required path first
                File dosya1 = new File("Data_Files/" + dosyaAdi);
                File dosya2 = new File(dosyaAdi);

                File use; // dosyaya erişme yöntemi

                if (dosya1.exists()) {
                    use = dosya1;
                } else {
                    use = dosya2;
                } if (!use.exists()) continue;



                br = new BufferedReader(new FileReader(use));
                String line = br.readLine(); // sıradaki satırı okuma. Okunan değişken line a atanır.
                while ((line = br.readLine()) != null) {

                    String[] parts = line.split(","); // virgüle göre parçalama
                    if (parts.length != 3) continue;

                    int day = parseIntSafe(parts[0], -1); //Satırdan okunan gün bilgisini sayıya çevirir hatalıysa -1 alır
                    int cIdx = commodityIndex(parts[1]); // commodity adını dizideki index’ine çevirir (Gold=0, Oil=1, ...)
                    int profit = parseIntSafe(parts[2], 0); // profit bilgisini sayıya çevirir (hatalıysa 0 alır).

                    if (day < 1 || day > DAYS) continue; // programı bozma
                    if (cIdx < 0) continue;

                    data[ay][day - 1][cIdx] = profit;
                }
            } catch (Exception e) {

            } finally {
                try {
                    if (br != null) br.close();
                } catch (Exception e) {

                    // try-catch ile dosya okuma hatalarının programı çökertmesini engelliyorum, finally bloğunda da dosyayı mutlaka kapatıyorum

                }
            }
        }
    }


    public static String mostProfitableCommodityInMonth(int month) { // Verilen ay için toplam kârı en yüksek olan commodityi bulur
        if (!validMonth(month)) return "INVALID_MONTH"; // Geçersiz ay için hata döndürme

        int bestUrun = 0; //En kârlı commoditynin indexini tutar
        int bestSum = Integer.MIN_VALUE; // Karşılaştırma için en küçük olası değerden başlar

        for (int urun = 0; urun < COMMS; urun++) {
            int sum = 0;
            for (int gun = 0; gun < DAYS; gun++) {
                sum += data[month][gun][urun];   // Seçili ay, gün ve commodity için kârı toplar
            }
            if (sum > bestSum) {
                bestSum = sum;
                bestUrun = urun;
            }
        }
        return commodities[bestUrun] + " " + bestSum;
    }


    public static int totalProfitOnDay(int month, int day) {  // Verilen ay ve günde tüm commoditylerin toplam kârını hesaplar
        if (!validMonth(month) || day < 1 || day > DAYS) return -99999;    // Ay veya gün geçersizse hata kodu döndürür

        int total = 0;
        for (int urun = 0; urun < COMMS; urun++) {
            total += data[month][day - 1][urun];
        }
        return total;

    }


    public static int commodityProfitInRange(String commodity, int fromDay, int toDay) {  // Belirtilen commodity için yıl boyunca verilen gün aralığındaki toplam kârı hesaplar

        int cIdx = commodityIndex(commodity); // Commodity adını dizideki index’ine çevirir
        if (cIdx < 0) return -99999;// Commodity geçersizse hata kodu döndürür
        if (fromDay < 1 || fromDay > DAYS || toDay < 1 || toDay > DAYS) return -99999; // Gün aralığı geçersizse hata kodu döndürür
        if (fromDay > toDay) return -99999; // Başlangıç günü bitiş gününden büyükse hata döndürür


        int sum = 0;
        for (int ay = 0; ay < MONTHS; ay++) {
            for (int gun = fromDay - 1; gun <= toDay - 1; gun++) {
                sum += data[ay][gun][cIdx];
            }
        }
        return sum;
    }


    public static int bestDayOfMonth(int month) {  // Verilen ayda toplam kârı en yüksek olan günü bulur

        if (!validMonth(month)) return -1;

        int bestGun = 1;
        int bestTotal = Integer.MIN_VALUE; // Karşılaştırma için en küçük olası toplam kâr değeri


        for (int gun = 1; gun <= DAYS; gun++) {
            int total = totalProfitOnDay(month, gun);
            if (total > bestTotal) {
                bestTotal = total;
                bestGun = gun;
            }
        }
        return bestGun;
    }
    

    public static String bestMonthForCommodity(String commodity) {  // Verilen commodity için en yüksek toplam kârın olduğu ayı bulur

        int cIdx = commodityIndex(commodity); // Commodity adını commodities dizisindeki indexe çevirir

        if (cIdx < 0) return "INVALID_COMMODITY"; // Commodity geçersizse hata kodu


        int bestAy = 0; // En kârlı ayın indexi  (başlangıçta 0 = January)

        int bestSum = Integer.MIN_VALUE;

        for (int ay = 0; ay < MONTHS; ay++) {
            int sum = 0;
            for (int gun = 0; gun < DAYS; gun++) {
                sum += data[ay][gun][cIdx];

                // Bu ayda seçilen commodity’nin toplam kârını tutar

            }
            if (sum > bestSum) {
                bestSum = sum;
                bestAy = ay;
            }
        }
        return months[bestAy];
    }


    public static int consecutiveLossDays(String commodity) {   // Seçilen commodity için yıl boyunca arka arkaya gelen en uzun zarar günlerini bulur

        int cIdx = commodityIndex(commodity);
        if (cIdx < 0) return -1;      // Commodity geçersizse hata kodu


        int best = 0; // Şu ana kadar bulunan en uzun zarar sırası

        int anlik = 0; // O anki devam eden zarar günleri sayısı


        for (int ay = 0; ay < MONTHS; ay++) {
            for (int gun = 0; gun < DAYS; gun++) {
                if (data[ay][gun][cIdx] < 0) {
                    anlik++;
                    //  Ayları ve günleri sırayla dolaşıp devam eden zarar günü sayısını artırır

                    if (anlik > best) best = anlik;
                } else {
                    anlik = 0;
                }
            }
        }
        return best;
    }

    }