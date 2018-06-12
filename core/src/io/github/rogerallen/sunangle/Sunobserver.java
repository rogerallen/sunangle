package io.github.rogerallen.sunangle;

import com.badlogic.gdx.Gdx;

import java.util.Calendar;
import java.util.Date;

public class Sunobserver {
    private double observer_latitude;
    private double observer_longitude;
    private Date   observer_date;
    private double sun_right_ascension;
    private double sun_declination;
    private double sun_altitude;
    private double sun_azimuth;
    private double JD;      // Julian Day
    private double T;       //
    private double Theta0;  // sidereal time at Greenwich (in degrees)

    public Sunobserver(double lat, double lon) {
        observer_latitude = lat;
        observer_longitude = lon;
        setObserverDate();
        compute();
        //unitTests();
    }

    public void setObserverDate() {
        observer_date = new Date();
        Calendar time = Calendar.getInstance();
        time.setTime(observer_date);
        // adjust the time to GMT via negative TimeZone offset
        int offset = -time.getTimeZone().getOffset(time.getTimeInMillis());
        time.add(Calendar.MILLISECOND, offset);
        Gdx.app.log("Sunobserver", "GMT = "+time.getTime());
        // check JD vs http://aa.usno.navy.mil/data/docs/JulianDate.php
        JD = julianDay(time.get(Calendar.YEAR), time.get(Calendar.MONTH) + 1, dayTime(time));
        Gdx.app.log("Sunobserver", "JD  = "+JD);
        T = (JD - 2451545.0) / 36525.0;
        Theta0 = 280.46061837 + 360.98564736629*(JD-2451545.0) + 0.000387933*T*T - T*T*T/38710000;
    }

    private double dayTime(Calendar t) {
        // Convert date day + hours, mins, seconds to one floating point value
        // DAY_OF_MONTH is 1..31
        double v = t.get(Calendar.DAY_OF_MONTH);
        double hms = (double)t.get(Calendar.HOUR_OF_DAY)/24 +
                (double)t.get(Calendar.MINUTE)/(24*60) +
                (double)t.get(Calendar.SECOND)/(24*60*60) +
                (double)t.get(Calendar.MILLISECOND)/(24*60*60*1000);
        return v + hms;
    }

    private int INT(double x) {
        // INT in Meeus' book always rounds towards negative infinity
        if (x < 0) {
            x = x - 1;
        }
        return (int)x;
    }

    private double julianDay(double year, double month, double day) {
        Gdx.app.debug("Sunobserver", "julianDay("+year+"/"+month+"/"+day+")");
        // year = year, month = month-of-year (1..12), day = day-of-year (1..365)
        // FIXME JD not the same as found on internet
        // Take current YMD & get the Julian Day.  See Formula 7.1
        if (month <= 2) {
            year = year - 1;
            month = month + 12;
        }
        int A = INT(year/100.);
        int B = 2 - A + INT(A/4.);
        if ((year + month/12. + day/(12*31)) < (1582 + 10./12. + 5/(12*31))) {
            // Julian Calendar
            B = 0;
        }
        double JD0 = INT(365.25*(year+4716.)) + INT(30.6001*(month+1.)) + day + B - 1524.5;
        return JD0;
    }

    private void compute() {
        {
            // compute the RA/Dec position of the sun. See Chapter 24.
            double L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T * T;
            double M = 357.52910 + 35999.05030 * T + 0.0001559 * T * T - 0.00000048 * T * T * T;
            double e = 0.016708617 - 0.000042037 * T + 0.0000001236 * T * T;
            double C = (1.914600 - 0.004817 * T - 0.000014 * T * T) * Math.sin(M) + (0.019993 - 0.000101 * T) * Math.sin(2 * M) + 0.000290 * Math.sin(3 * M);
            double Theta = L0 + C;
            double v = M + C;
            double R = 1.000001018 * (1 - e * e) / (1 + e * Math.cos(v));
            double Omega = 125.04 - 1934.136 * T;
            double Lambda = Theta - 0.00569 - 0.00478 * Math.sin(Omega);
            double epsilon = 23. + 26. / 60. + 21.448 / (60. * 60.) - 46.8150 * T / (60. * 60.) - 0.00059 * T * T / (60. * 60.) + 0.001813 * T * T * T / (60. * 60.);
            // R.A. = alpha, Declination = delta
            double alpha_apparent = Math.toDegrees(Math.atan2(Math.cos(epsilon) * Math.sin(Lambda), Math.cos(Lambda)));
            double delta_apparent = Math.toDegrees(Math.asin(Math.sin(epsilon) * Math.sin(Lambda)));
            sun_right_ascension = alpha_apparent;
            sun_declination = delta_apparent;
        }
        {
            // compute the altitude & azimuth (from North).  See Chapter 12.
            // NOTE longitude is opposite sign than the book!
            double H = Theta0 + observer_longitude - sun_right_ascension;
            double delta = sun_declination;
            double phi = observer_latitude;
            // A = azimuth (from north, not as book has it)
            // h = altitude
            double A = Math.toDegrees(Math.atan2(Math.sin(H), Math.cos(H) * Math.sin(phi) - Math.tan(delta) * Math.cos(phi))) + 180.;
            double h = Math.toDegrees(Math.asin(Math.sin(phi) * Math.sin(delta) + Math.cos(phi) * Math.cos(delta) * Math.cos(H)));
            sun_azimuth = A;
            sun_altitude = h;
        }
    }

    private void unitTests() {
        System.err.println("Testing...");
        double v = julianDay(2000, 1, 1.5);
        if(2451545.0 != v) { System.err.println("FAIL 2000/1/1.5"); }
        v = julianDay(2018, 1, 1.5);
        if(2458120.0 != v) { System.err.println("FAIL 2018/1/1.5"); }
        v = julianDay(2018, 6, 1.5);
        if(2458271.0 != v) { System.err.println("FAIL 2018/6/1.5"); }
        // http://aa.usno.navy.mil/data/docs/JulianDate.php
        v = julianDay(2018, 6, 12.0+(23.0/24)+(30.0/(24*60)));
        if(Math.abs(2458282.479167 - v) > 1e-6) { System.err.println("FAIL 2018/6/12.9 "+Math.abs(2458282.479167 - v)); }
    }
}
