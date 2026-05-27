package nl.inholland.codegen.bankingapp.utils;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class IbanUtil {
    public static final String IBAN_COUNTRY_CODE = "NL";
    public static final String IBAN_BANK_CODE = "INHO";

    private static final long MAX_ACCOUNT_NUMBER = 9_999_999_999L; // 10 digits

    private final Random random = new Random();

    public boolean matches(long accountNumber, String iban) {
        if (iban == null) {
            return false;
        }

        return generateIban(accountNumber).equalsIgnoreCase(iban);
    }

    public long newAccountNumber() {
        return random.nextLong(MAX_ACCOUNT_NUMBER);
    }

    public String generateIban(Long accountNumber) {
        if (accountNumber == null) {
            throw new IllegalArgumentException("accountNumber must not be null");
        }

        String accountNumberStr = String.format("%010d", accountNumber);

        // checksum is not yet calculated
        String startingIban = IBAN_BANK_CODE + accountNumberStr + IBAN_COUNTRY_CODE + "00";

        // Convert letters to numbers (A=10, B=11, ..., Z=35)
        StringBuilder numeric = new StringBuilder();
        for (char c : startingIban.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append(c - 'A' + 10);
            } else {
                numeric.append(c);
            }
        }

        // Compute mod 97
        int mod = 0;
        for (char digit : numeric.toString().toCharArray()) {
            mod = (mod * 10 + Character.getNumericValue(digit)) % 97;
        }

        int checksum = 98 - mod;

        // Format checksum as 2 digits
        String checkDigits = String.format("%02d", checksum);

        return IBAN_COUNTRY_CODE + checkDigits + IBAN_BANK_CODE + accountNumberStr;
    }

    // Accepts a string (Expected to be either an IBAN or account number) and returns just the account number (if it's IBAN, convert)
    public long resolveAccountNumber(String input) {
        String normalized = input.replaceAll("\\s+", "").toUpperCase();

        if (normalized.length() == 18) {
            return ibanToAccountNumber(normalized);
        }

        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid account identifier");
        }
    }

    public Long ibanToAccountNumber(String iban) {
        // Validate country and bank code
        if (!iban.startsWith(IBAN_COUNTRY_CODE)) {
            throw new IllegalArgumentException("Invalid country code");
        }

        String bankCode = iban.substring(4, 8);
        if (!IBAN_BANK_CODE.equals(bankCode)) {
            throw new IllegalArgumentException("Invalid bank code");
        }

        // Extract account number part
        String accountNumberStr = iban.substring(8, 18);

        return Long.parseLong(accountNumberStr);
    }
}
