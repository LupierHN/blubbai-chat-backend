package chat.dvai.backend.utils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class PhoneUtil {

    public static int getCountryCodeNumber(String country) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.getCountryCodeForRegion(country.toUpperCase());
    }

    public static String getCountryCodeString(int country) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.getRegionCodeForCountryCode(country);
    }
}