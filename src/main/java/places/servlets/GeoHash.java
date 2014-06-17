package places.servlets;

/**
 * Modified from https://github.com/davidmoten/geo 
 * @author gw
 *
 */

public class GeoHash {
    public static final int DEFAULT_MAX_HASHES = 12;
    public static final int SHORT_HASHES = 4;
    /**
     * Powers of 2 from 32 down to 1.
     */
    private static final int[] BITS = new int[] { 16, 8, 4, 2, 1 };
    /**
     * The characters used in base 32 representations.
     */
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

	/**
     * Returns a geohash of length DEFAULT_MAX_HASHES (12) for the given WGS84
     * point (latitude,longitude).
     * 
     * @param latitude
     *            in decimal degrees (WGS84)
     * @param longitude
     *            in decimal degrees (WGS84)
     * @return
     */
    public static String encodeHash(double latitude, double longitude) {
        return encodeHash(latitude, longitude, DEFAULT_MAX_HASHES);
    }


    /**
     * Returns a geohash of given length for the given WGS84 point
     * (latitude,longitude). If latitude is not between -90 and 90 throws an
     * {@link IllegalArgumentException}.
     * 
     * @param latitude
     *            in decimal degrees (WGS84)
     * @param longitude
     *            in decimal degrees (WGS84)
     * @return
     */
    // Translated to java from:
    // geohash.js
    // Geohash library for Javascript
    // (c) 2008 David Troy
    // Distributed under the MIT License
    public static String encodeHash(double latitude, double longitude,
            int length) {
        GeoHashPreconditions.checkArgument(length > 0,
                "length must be greater than zero");
        GeoHashPreconditions.checkArgument(latitude >= -90 && latitude <= 90,
                "latitude must be between -90 and 90 inclusive");
        longitude = GeoHashPosition.to180(longitude);

        boolean isEven = true;
        double[] lat = new double[2];
        double[] lon = new double[2];
        int bit = 0;
        int ch = 0;
        StringBuilder geohash = new StringBuilder();

        lat[0] = -90.0;
        lat[1] = 90.0;
        lon[0] = -180.0;
        lon[1] = 180.0;

        while (geohash.length() < length) {
            if (isEven) {
                double mid = (lon[0] + lon[1]) / 2;
                if (longitude > mid) {
                    ch |= BITS[bit];
                    lon[0] = mid;
                } else
                    lon[1] = mid;
            } else {
                double mid = (lat[0] + lat[1]) / 2;
                if (latitude > mid) {
                    ch |= BITS[bit];
                    lat[0] = mid;
                } else
                    lat[1] = mid;
            }

            isEven = !isEven;
            if (bit < 4)
                bit++;
            else {
                geohash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }
        return geohash.toString();
    }

    /**
     * Returns a latitude,longitude pair as the centre of the given geohash.
     * Latitude will be between -90 and 90 and longitude between -180 and 180.
     * 
     * @param geohash
     * @return
     */
    // Translated to java from:
    // geohash.js
    // Geohash library for Javascript
    // (c) 2008 David Troy
    // Distributed under the MIT License
    public static double[] decodeHash(String geohash) {
        GeoHashPreconditions.checkNotNull(geohash, "geohash cannot be null");
        boolean isEven = true;
        double[] lat = new double[2];
        double[] lon = new double[2];
        lat[0] = -90.0;
        lat[1] = 90.0;
        lon[0] = -180.0;
        lon[1] = 180.0;

        for (int i = 0; i < geohash.length(); i++) {
            char c = geohash.charAt(i);
            int cd = BASE32.indexOf(c);
            for (int j = 0; j < 5; j++) {
                int mask = BITS[j];
                if (isEven) {
                    refineInterval(lon, cd, mask);
                } else {
                    refineInterval(lat, cd, mask);
                }
                isEven = !isEven;
            }
        }
        
        double[] result = new double[2];
        result[0] = (lat[0] + lat[1]) / 2;
        result[1] = (lon[0] + lon[1]) / 2;

        return result;
    }

    /**
     * Refines interval by a factor or 2 in either the 0 or 1 ordinate.
     * 
     * @param interval
     * @param cd
     * @param mask
     */
    private static void refineInterval(double[] interval, int cd, int mask) {
        if ((cd & mask) != 0)
            interval[0] = (interval[0] + interval[1]) / 2;
        else
            interval[1] = (interval[0] + interval[1]) / 2;
    }
}
