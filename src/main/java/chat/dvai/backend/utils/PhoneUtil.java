package chat.dvai.backend.utils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class PhoneUtil {

    /**
     * Returns the country code number for a given country name.
     *
     * @param country The name of the country (e.g., "India", "United States").
     * @return The country code number (e.g., 91 for India, 1 for the United States).
     */
    public static int getCountryCodeNumber(String country) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.getCountryCodeForRegion(country.toUpperCase());
    }

    /**
     * Returns the country code string for a given country code number.
     *
     * @param country The country code number (e.g., 91 for India, 1 for the United States).
     * @return The country code string (e.g., "IN" for India, "US" for the United States).
     */
    public static String getCountryCodeString(int country) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.getRegionCodeForCountryCode(country);
    }
}