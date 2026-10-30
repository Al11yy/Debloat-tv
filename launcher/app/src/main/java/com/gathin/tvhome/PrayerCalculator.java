package com.gathin.tvhome;

import java.util.Calendar;
import java.util.Locale;

/**
 * Solar prayer times calculator (100% offline, zero network permission needed).
 * Uses standard astronomical solar declination and equation of time formulas.
 * Tailored for Indonesian Ministry of Religious Affairs (KEMENAG RI) standards.
 */
public final class PrayerCalculator {

    // Default coords: Kecamatan Tanah Sareal, Kota Bogor, West Java, Indonesia (-6.5650, 106.7900, UTC+7).
    public static final double DEFAULT_LATITUDE = -6.5650;
    public static final double DEFAULT_LONGITUDE = 106.7900;
    public static final String DEFAULT_LOCATION_NAME = "Tanah Sareal, Bogor";

    private PrayerCalculator() {}

    public static class PrayerTime {
        public final String name;
        public final String indonesianName;
        public final int hour;
        public final int minute;
        public final String timeString; // "HH:mm"
        public final String description;

        public PrayerTime(String name, String indonesianName, int hour, int minute, String description) {
            this.name = name;
            this.indonesianName = indonesianName;
            this.hour = hour;
            this.minute = minute;
            this.timeString = String.format(Locale.US, "%02d:%02d", hour, minute);
            this.description = description;
        }

        public PrayerTime(String name, int hour, int minute) {
            this(name, name, hour, minute, "");
        }
    }

    public static class Schedule {
        public final PrayerTime imsak;
        public final PrayerTime fajr;
        public final PrayerTime sunrise;
        public final PrayerTime dhuha;
        public final PrayerTime dhuhr;
        public final PrayerTime asr;
        public final PrayerTime maghrib;
        public final PrayerTime isha;
        public final PrayerTime midnight;

        public Schedule(PrayerTime imsak, PrayerTime fajr, PrayerTime sunrise, PrayerTime dhuha,
                        PrayerTime dhuhr, PrayerTime asr, PrayerTime maghrib, PrayerTime isha, PrayerTime midnight) {
            this.imsak = imsak;
            this.fajr = fajr;
            this.sunrise = sunrise;
            this.dhuha = dhuha;
            this.dhuhr = dhuhr;
            this.asr = asr;
            this.maghrib = maghrib;
            this.isha = isha;
            this.midnight = midnight;
        }

        public PrayerTime[] all() {
            return new PrayerTime[]{fajr, sunrise, dhuhr, asr, maghrib, isha};
        }

        public PrayerTime[] detailed() {
            return new PrayerTime[]{imsak, fajr, sunrise, dhuha, dhuhr, asr, maghrib, isha};
        }
    }

    public static class NextPrayerInfo {
        public final String name;
        public final String indonesianName;
        public final String timeFormatted;
        public final boolean isTomorrow;

        public NextPrayerInfo(String name, String indonesianName, String timeFormatted, boolean isTomorrow) {
            this.name = name;
            this.indonesianName = indonesianName;
            this.timeFormatted = timeFormatted;
            this.isTomorrow = isTomorrow;
        }

        public NextPrayerInfo(String name, String timeFormatted, boolean isTomorrow) {
            this(name, name, timeFormatted, isTomorrow);
        }
    }

    /**
     * Compute prayer times schedule for a specific date and coordinates.
     * Default coords: Kecamatan Tanah Sareal, Bogor, Indonesia (-6.5650, 106.7900, UTC+7).
     */
    public static Schedule compute(Calendar cal, double lat, double lon, double tzOffset) {
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

        // Approximate fractional year in radians
        double gamma = 2 * Math.PI / 365.0 * (dayOfYear - 1);

        // Equation of time (in minutes)
        double eqTime = 229.18 * (0.000075 + 0.001868 * Math.cos(gamma) - 0.032077 * Math.sin(gamma)
                - 0.014615 * Math.cos(2 * gamma) - 0.040849 * Math.sin(2 * gamma));

        // Solar declination angle (in radians)
        double decl = 0.006918 - 0.399912 * Math.cos(gamma) + 0.070257 * Math.sin(gamma)
                - 0.006758 * Math.cos(2 * gamma) + 0.000907 * Math.sin(2 * gamma)
                - 0.002697 * Math.cos(3 * gamma) + 0.00148 * Math.sin(3 * gamma);

        double latRad = Math.toRadians(lat);

        // Solar noon (Dhuhr) in hours
        double noonHours = 12.0 + tzOffset - (lon / 15.0) - (eqTime / 60.0);

        // Hour angle for zenith angle z:
        // Indonesian Kemenag standard: Fajr angle = 20 deg below horizon (zenith = 110 deg)
        double fajrHa = hourAngle(latRad, decl, Math.toRadians(110.0));
        // Sunrise (zenith = 90.833 deg)
        double sunHa = hourAngle(latRad, decl, Math.toRadians(90.833));
        // Isha angle = 18 deg below horizon (zenith = 108 deg)
        double ishaHa = hourAngle(latRad, decl, Math.toRadians(108.0));

        // Asr: zenith angle when shadow length = object length + shadow at noon (Shafi'i standard)
        double noonZenith = Math.abs(latRad - decl);
        double asrZenith = Math.atan(1.0 + Math.tan(noonZenith));
        double asrHa = hourAngle(latRad, decl, Math.PI / 2.0 - asrZenith);

        double fajrH = noonHours - (fajrHa / 15.0);
        double imsakH = fajrH - (10.0 / 60.0); // 10 mins before Fajr
        double sunriseH = noonHours - (sunHa / 15.0);
        double dhuhaH = sunriseH + (20.0 / 60.0); // approx 20 mins after sunrise
        double dhuhrH = noonHours + (2.0 / 60.0); // 2 mins safety ihtiyat
        double asrH = noonHours + (asrHa / 15.0);
        double maghribH = noonHours + (sunHa / 15.0) + (2.0 / 60.0);
        double ishaH = noonHours + (ishaHa / 15.0);
        double midnightH = maghribH + ((fajrH + 24.0 - maghribH) / 2.0);

        return new Schedule(
                toPrayerTime("IMSAK", "Imsak", imsakH, "10 min before Subuh"),
                toPrayerTime("FAJR", "Subuh", fajrH, "Fajr prayer"),
                toPrayerTime("SUNRISE", "Syuruq", sunriseH, "Sunrise"),
                toPrayerTime("DHUHA", "Dhuha", dhuhaH, "Forenoon prayer"),
                toPrayerTime("DHUHR", "Dzuhur", dhuhrH, "Noon prayer"),
                toPrayerTime("ASR", "Ashar", asrH, "Afternoon prayer"),
                toPrayerTime("MAGHRIB", "Maghrib", maghribH, "Sunset prayer"),
                toPrayerTime("ISHA", "Isya", ishaH, "Night prayer"),
                toPrayerTime("MIDNIGHT", "Tahajjud", midnightH, "Qiyamullail")
        );
    }

    private static double hourAngle(double latRad, double declRad, double zenithRad) {
        double cosH = (Math.cos(zenithRad) - Math.sin(latRad) * Math.sin(declRad))
                / (Math.cos(latRad) * Math.cos(declRad));
        if (cosH > 1.0) cosH = 1.0;
        if (cosH < -1.0) cosH = -1.0;
        return Math.toDegrees(Math.acos(cosH));
    }

    private static PrayerTime toPrayerTime(String name, String indoName, double decimalHours, String desc) {
        decimalHours = (decimalHours + 24.0) % 24.0;
        int hour = (int) decimalHours;
        int minute = (int) Math.round((decimalHours - hour) * 60.0);
        if (minute >= 60) {
            hour = (hour + 1) % 24;
            minute = 0;
        }
        return new PrayerTime(name, indoName, hour, minute, desc);
    }

    private static PrayerTime toPrayerTime(String name, double decimalHours) {
        return toPrayerTime(name, name, decimalHours, "");
    }

    /**
     * Compute next upcoming prayer from current time.
     */
    public static NextPrayerInfo getNextPrayer(Calendar now, double lat, double lon, double tzOffset) {
        Schedule today = compute(now, lat, lon, tzOffset);
        int curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        PrayerTime[] prayers = new PrayerTime[]{today.fajr, today.dhuhr, today.asr, today.maghrib, today.isha};

        for (PrayerTime p : prayers) {
            int pMinutes = p.hour * 60 + p.minute;
            if (pMinutes > curMinutes) {
                return new NextPrayerInfo(p.name, p.indonesianName, p.timeString, false);
            }
        }

        // Otherwise next is tomorrow's Fajr
        Calendar tomorrow = (Calendar) now.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Schedule tomorrowSchedule = compute(tomorrow, lat, lon, tzOffset);
        return new NextPrayerInfo(tomorrowSchedule.fajr.name, tomorrowSchedule.fajr.indonesianName, tomorrowSchedule.fajr.timeString, true);
    }

    /**
     * Human-readable remaining countdown to the next prayer.
     */
    public static String getRemainingTime(Calendar now, double lat, double lon, double tzOffset) {
        Schedule today = compute(now, lat, lon, tzOffset);
        int curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        PrayerTime[] prayers = new PrayerTime[]{today.fajr, today.dhuhr, today.asr, today.maghrib, today.isha};

        for (PrayerTime p : prayers) {
            int pMinutes = p.hour * 60 + p.minute;
            if (pMinutes > curMinutes) {
                int diff = pMinutes - curMinutes;
                int h = diff / 60;
                int m = diff % 60;
                if (h > 0) return h + "h " + m + "m";
                return m + "m";
            }
        }

        Calendar tomorrow = (Calendar) now.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Schedule tomorrowSchedule = compute(tomorrow, lat, lon, tzOffset);
        int diff = (1440 - curMinutes) + (tomorrowSchedule.fajr.hour * 60 + tomorrowSchedule.fajr.minute);
        int h = diff / 60;
        int m = diff % 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }

    /**
     * Calculates Qibla direction in degrees clockwise from true North.
     * Kaaba coordinates: 21.4225 N, 39.8262 E.
     */
    public static double computeQibla(double lat, double lon) {
        double kaabaLat = Math.toRadians(21.4225);
        double kaabaLon = Math.toRadians(39.8262);
        double userLat = Math.toRadians(lat);
        double userLon = Math.toRadians(lon);

        double deltaLon = kaabaLon - userLon;
        double y = Math.sin(deltaLon);
        double x = Math.cos(userLat) * Math.tan(kaabaLat) - Math.sin(userLat) * Math.cos(deltaLon);
        double qibla = Math.toDegrees(Math.atan2(y, x));
        return (qibla + 360.0) % 360.0;
    }
}
