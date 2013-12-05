package fi.vm.sade.generic.common;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public final class HetuUtils {
    private static final String CHECKSUM_CHARACTERS = "0123456789ABCDEFHJKLMNPRSTUVWXY";
    private static volatile Map<Integer, Character> separators = new HashMap<Integer, Character>();
    private static volatile Map<Character, Integer> invertedSeparators = new HashMap<Character, Integer>();

    static {
        separators.put(18, '+');
        separators.put(19, '-');
        separators.put(20, 'A');
        separators.put(21, 'B');

        @SuppressWarnings("unchecked")
        final Map<Character, Integer> inverted = MapUtils.invertMap(separators);
        invertedSeparators = inverted;
    }

    private HetuUtils() {

    }

    /**
     * Generates a valid Finnish National Identification Number, such as "040789-5863".
     *
     * @return
     */
    public static String generateHetu() {
        final Random rand = new Random();
        final int day = rand.nextInt(28) + 1;
        final int month = rand.nextInt(12) + 1;
        final int year = 1950 + rand.nextInt(40);
        final int gender = rand.nextInt(2); // 0 = female, 1 = male
        return generateHetuWithArgs(day, month, year, gender);
    }

    /**
     * Generates a valid Finnish National Identification Number with arguments.
     *
     * @param day day of birthday
     * @param month month of birthday
     * @param year year of birthday
     * @param gender 0 = female, 1 = male
     * @return
     */
    public static String generateHetuWithArgs(final int day, final int month, final int year, final int gender) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
            sdf.parse(String.format("%s%s%s",
                    StringUtils.leftPad(String.valueOf(day), 2, '0'),
                    StringUtils.leftPad(String.valueOf(month), 2, '0'),
                    String.valueOf(year)));
        } catch (final Exception e) {
            throw new IllegalArgumentException("error parsing birthday", e);
        }

        if (year < 1800 || year > 2199) {
            throw new IllegalArgumentException("year is invalid");
        }

        if (gender < 0 || gender > 1) {
            throw new IllegalArgumentException("gender is invalid; should be 0 (female) or 1 (male)");
        }

        final Random rand = new Random();
        int identifier = rand.nextInt(998) + 1;
        if ((gender == 0 && identifier % 2 != 0) || (gender == 1 && identifier % 2 == 0)) {
            identifier++;
        }
        final String partialHetu = String.format("%s%s%s%c%s",
                StringUtils.leftPad(String.valueOf(day), 2, '0'),
                StringUtils.leftPad(String.valueOf(month), 2, '0'),
                StringUtils.substring(String.valueOf(year), 2),
                separators.get(Integer.parseInt(StringUtils.substring(String.valueOf(year), 0, 2))),
                StringUtils.leftPad(String.valueOf(identifier), 3, '0'));
        final char checksumCharacter = getChecksumCharacter(partialHetu);

        return String.format("%s%c", partialHetu, checksumCharacter);
    }

    /**
     * Checks if a given Finnish National Identification Number is valid.
     *
     * @param hetu a Finnish National Identification Number to be validated, such as "040789-5863"
     * @return
     */
    public static boolean isHetuValid(final String hetu) {
        if (StringUtils.isBlank(hetu)) {
            return false;
        }

        final String localHetu = hetu.toUpperCase().trim();

        // validate form
        if (!localHetu.matches("\\d{6}[+-AB]\\d{3}[A-Z0-9]")) {
            return false;
        }

        final char separator = localHetu.charAt(6);

        try {
            // validate birth date
            final String fullLengthBirthDate = String.format("%d%s-%s-%s",
                    invertedSeparators.get(separator),
                    StringUtils.substring(localHetu, 4, 6),
                    StringUtils.substring(localHetu, 2, 4),
                    StringUtils.substring(localHetu, 0, 2));
            new SimpleDateFormat("yyyy-MM-dd").parse(fullLengthBirthDate);

            // validate checksum character
            final char checksumCharacter = getChecksumCharacter(hetu).charValue();
            if (checksumCharacter != hetu.charAt(10)) {
                return false;
            }
        } catch (final Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Checks if given Finnish National Identification Number is for male.
     *
     * @param hetu
     * @return
     */
    public static boolean isMaleHetu(final String hetu) {
        if (!isHetuValid(hetu)) {
            throw new IllegalArgumentException(String.format("Hetu [%s] is not valid", hetu));
        }

        return Integer.parseInt(StringUtils.substring(hetu, 9, 10)) % 2 != 0;
    }

    /**
     * Checks if given Finnish National Identification Number is for female.
     *
     * @param hetu
     * @return
     */
    public static boolean isFemaleHetu(final String hetu) {
        return !isMaleHetu(hetu);
    }

    private static Character getChecksumCharacter(final String partialHetu) {
        final long checkNumber = Long.parseLong(String.format("%s%s", StringUtils.substring(partialHetu, 0, 6),
                StringUtils.substring(partialHetu, 7, 10)));
        return CHECKSUM_CHARACTERS.charAt((int)(checkNumber % CHECKSUM_CHARACTERS.length()));
    }

    public static String maskHetu(final String hetu) {
        if (hetu == null)
            return null;

        if (hetu.length() > 6) {
            return maskHetuInternal(hetu, false);
        }

        return hetu;
    }

    public static String maskHetuFull(final String hetu) {
        if (hetu == null)
            return null;

        return maskHetuInternal(hetu, true);
    }

    private static String maskHetuInternal(final String hetu, final boolean maskAllChars) {
        final StringBuilder masked = new StringBuilder(maskAllChars ? "" : hetu.substring(0,6));
        for (int i = (maskAllChars ? 0 : 6); i < hetu.length(); i++) {
            masked.append('*');
        }
        return masked.toString();
    }
}
