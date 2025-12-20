// Main.java — Students version
import java.io.*;
import java.util.*;

public class Main {

    static final int MONTHS = 12;
    static final int DAYS = 28;
    static final int COMMS = 5;


    static String[] commodities = {"Gold", "Oil", "Silver", "Wheat", "Copper"};


    static String[] months = {"January","February","March","April","May","June",
            "July","August","September","October","November","December"};


    static int[][][] data = new int[MONTHS][DAYS][COMMS];

    // ======== REQUIRED METHOD LOAD DATA ========
    public static void loadData() { //Dosyalardan yıl boyunca tüm gün–commodity–kâr verilerini okuyup data dizisine yükler.

        for (int ay = 0; ay < MONTHS; ay++) {
            for (int gun = 0; gun < DAYS; gun++) {
                for (int urun = 0; urun < COMMS; urun++) {
                    data[ay][gun][urun] = 0;
                }
            }
        }

        //  Her ay için ilgili dosyayı okuması
        for (int ay = 0; ay < MONTHS; ay++) {
            String dosyaAdi = months[ay] + ".txt";
            BufferedReader br = null;

            try {

                File dosya1 = new File("Data_Files/" + dosyaAdi);
                File dosya2 = new File(dosyaAdi);

                File use;
                if (dosya1.exists()) {
                    use = dosya1;
                } else {
                    use = dosya2;
                }

                // Dosya yoksa bu ayı atla
                if (!use.exists()) continue;

                br = new BufferedReader(new FileReader(use));

                // Eğer dosyanın ilk satırı header ise bu satır açılabilir:
                // br.readLine();

                String line;
                while ((line = br.readLine()) != null) {
                    // virgülle bölme
                    String[] parts = line.split(",");
                    if (parts.length != 3) continue; // bozuk satırları pas geç

                    int day = parseIntSafe(parts[0].trim(), -1);
                    int cIdx = commodityIndex(parts[1].trim());
                    int profit = parseIntSafe(parts[2].trim(), 0);


                    // Array index hatasını engellemek için  yanlış gün veya ürün varsa ignore.
                    if (day < 1 || day > DAYS) continue;
                    if (cIdx < 0) continue;


                    data[ay][day - 1][cIdx] = profit;
                }

            } catch (Exception e) {

                // dosya hatası varsa o ayı yok sayıyoruz.

            } finally {

                try {
                    if (br != null) br.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    // ======== REQUIRED METHODS ========

    public static String mostProfitableCommodityInMonth(int month) { // Verilen ayda toplam kârı en yüksek olan commodity
        //
        if (!validMonth(month)) return "INVALID_MONTH";

        int bestUrun = 0;
        int bestSum = Integer.MIN_VALUE; // tüm değerler negatif olsa bile doğru kıyas için

        // Her commodity için o ayın toplamını hesapla en büyüğünü seç
        for (int urun = 0; urun < COMMS; urun++) {
            int sum = 0;
            for (int gun = 0; gun < DAYS; gun++) {
                sum += data[month][gun][urun];
            }
            if (sum > bestSum) {
                bestSum = sum;
                bestUrun = urun;
            }
        }
        return commodities[bestUrun] + " " + bestSum;
    }

    public static int totalProfitOnDay(int month, int day) {  //Verilen ay ve günde tüm commodity’lerin toplam kârı

        if (!validMonth(month) || day < 1 || day > DAYS) return -99999;

        int total = 0;
        for (int urun = 0; urun < COMMS; urun++) {
            total += data[month][day - 1][urun];
        }
        return total;
    }

    public static int commodityProfitInRange(String commodity, int from, int to) { //Seçilen commodity için yıl boyunca verilen gün aralığındaki toplam kârı
        // commodity string indexe çevriliryoksa INVALID
        int cIdx = commodityIndex(commodity);
        if (cIdx < 0) return -99999; // hata onlemi

        // Range kontrol
        if (from < 1 || from > DAYS || to < 1 || to > DAYS) return -99999;
        if (from > to) return -99999;

        // Yıl boyunca from..to günleri arasındaki toplam profit
        int sum = 0;
        for (int ay = 0; ay < MONTHS; ay++) {
            for (int gun = from - 1; gun <= to - 1; gun++) {
                sum += data[ay][gun][cIdx];
            }
        }
        return sum;
    }

    public static int bestDayOfMonth(int month) { //Verilen ayda toplam kârın en yüksek olduğu gün
        if (!validMonth(month)) return -1;

        int bestGun = 1;
        int bestTotal = Integer.MIN_VALUE;

        // 1-28 günleri içinde totalProfit en yüksek olan gün
        for (int gun = 1; gun <= DAYS; gun++) {
            int total = totalProfitOnDay(month, gun);
            if (total > bestTotal) {
                bestTotal = total;
                bestGun = gun;
            }
        }
        return bestGun;
    }

    public static String bestMonthForCommodity(String comm) {  //Seçilen commodity için yıl boyunca en yüksek toplam kârın olduğu ay
        int cIdx = commodityIndex(comm);
        if (cIdx < 0) return "INVALID_COMMODITY";

        int bestAy = 0;
        int bestSum = Integer.MIN_VALUE;

        // Her ay için seçili commodity toplamını bul ve en yüksek ayı döndür
        for (int ay = 0; ay < MONTHS; ay++) {
            int sum = 0;
            for (int gun = 0; gun < DAYS; gun++) {
                sum += data[ay][gun][cIdx];
            }
            if (sum > bestSum) {
                bestSum = sum;
                bestAy = ay;
            }
        }
        return months[bestAy];
    }

    public static int consecutiveLossDays(String comm) { //Seçilen commodity için yıl boyunca arka arkaya gelen en uzun zarar günleri serisini hesaplar
        int cIdx = commodityIndex(comm);
        if (cIdx < 0) return -1;

        int best = 0;   // şimdiye kadarki en uzun seri
        int anlik = 0;  // şu anda devam eden seri

        // Yılı baştan sona tarayıp ardışık negatif günleri say
        for (int ay = 0; ay < MONTHS; ay++) {
            for (int gun = 0; gun < DAYS; gun++) {
                if (data[ay][gun][cIdx] < 0) {
                    anlik++;
                    if (anlik > best) best = anlik;
                } else {
                    // seri bozulursa sıfırla
                    anlik = 0;
                }
            }
        }
        return best;
    }

    public static int daysAboveThreshold(String comm, int threshold) { // Seçilen commodity’nin yıl boyunca belirlenen eşik değerin üzerinde kâr ettiği gün sayısı
        int cIdx = commodityIndex(comm);
        if (cIdx < 0) return -1;

        int sayi = 0;
        // thresholdu aşan günleri say
        for (int ay = 0; ay < MONTHS; ay++) {
            for (int gun = 0; gun < DAYS; gun++) {
                if (data[ay][gun][cIdx] > threshold) sayi++;
            }
        }
        return sayi;
    }

    public static int biggestDailySwing(int month) { // Verilen ayda ardışık günler arasındaki en büyük toplam kâr farkı
        if (!validMonth(month)) return -99999;

        int best = 0;
        int prev = totalProfitOnDay(month, 1);

        // Swing = |bugün - dün| en büyüğünü döndür
        for (int gun = 2; gun <= DAYS; gun++) {
            int anlik = totalProfitOnDay(month, gun);
            int fark = anlik - prev;
            if (fark < 0) fark = -fark; // mutlak değer
            if (fark > best) best = fark;
            prev = anlik;
        }
        return best;
    }

    public static String compareTwoCommodities(String c1, String c2) { // İki commodity’nin yıllık toplam kârını karşılaştır
        //  string index dönüşümü biri yoksa INVALID
        int i1 = commodityIndex(c1);
        int i2 = commodityIndex(c2);
        if (i1 < 0 || i2 < 0) return "INVALID_COMMODITY";

        int s1 = totalYearProfit(i1);
        int s2 = totalYearProfit(i2);

        if (s1 == s2) return "Equal";
        if (s1 > s2) return c1 + " is better by " + (s1 - s2);
        return c2 + " is better by " + (s2 - s1);
    }

    public static String bestWeekOfMonth(int month) {  //Verilen ayda 4 haftadan hangisinin toplam kârı en yüksek
        if (!validMonth(month)) return "INVALID_MONTH";


        int bestHafta = 1;
        int bestSum = Integer.MIN_VALUE;

        for (int hafta = 1; hafta <= 4; hafta++) {
            int startDay = (hafta - 1) * 7 + 1; // 1-based gün
            int endDay = hafta * 7;

            int sum = 0;
            for (int gun = startDay; gun <= endDay; gun++) {
                sum += totalProfitOnDay(month, gun);
            }

            if (sum > bestSum) {
                bestSum = sum;
                bestHafta = hafta;
            }
        }
        return "Week " + bestHafta;
    }

    public static void main(String[] args) {
        loadData();
        // System.out.println("Data loaded – ready for queries");
    }

    // ======== HELPERS ========

    static boolean validMonth(int month) {  //Ay indeksinin geçerli (0–11 aralığında) olup olmadığını kontrol eder

        return month >= 0 && month < MONTHS;
    }

    static int commodityIndex(String name) { // Commodity adını dizideki indexine çevirir geçersizse -1 döndürür

        if (name == null) return -1;
        for (int i = 0; i < COMMS; i++) {
            if (commodities[i].equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    static int parseIntSafe(String s, int fallback) {  //String değeri güvenli şekilde sayıya çevirir hata olursa varsayılan değer döndürür.
        // parse hatası olursa program kırılmasın diye fallback dönüyorum
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    static int totalYearProfit(int commIndex) {
        //Seçilen commodity’nin yıl boyunca toplam kârı
        int sum = 0;
        for (int ay = 0; ay < MONTHS; ay++) {
            for (int gun = 0; gun < DAYS; gun++) {
                sum += data[ay][gun][commIndex];
            }
        }
        return sum;
    }
}


