package places.servlets;

public class GeoHashPosition {
    /**
     * Converts an angle in degrees to range -180< x <= 180.
     * 
     * @param d
     * @return
     */
    public static double to180(double d) {
        if (d < 0)
            return -to180(Math.abs(d));
        else {
            if (d > 180) {
                long n = Math.round(Math.floor((d + 180) / 360.0));
                return d - n * 360;
            } else
                return d;
        }
    }
}
