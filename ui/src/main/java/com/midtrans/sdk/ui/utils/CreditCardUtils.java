package com.midtrans.sdk.ui.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.support.annotation.NonNull;

import com.midtrans.sdk.analytics.MidtransAnalytics;
import com.midtrans.sdk.core.models.BankType;
import com.midtrans.sdk.core.models.snap.SavedToken;
import com.midtrans.sdk.core.models.snap.bins.BankBinsResponse;
import com.midtrans.sdk.core.models.snap.card.CreditCardPaymentResponse;
import com.midtrans.sdk.ui.constants.AnalyticsEventName;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by ziahaqi on 2/24/17.
 */

public class CreditCardUtils {

    public static String getMaskedCardNumber(String maskedCard) {
        StringBuilder builder = new StringBuilder();
        String bulletMask = "●●●●●●";
        String newMaskedCard = maskedCard.replace("-", bulletMask);

        for (int i = 0; i < newMaskedCard.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                builder.append(' ');
                builder.append(newMaskedCard.charAt(i));
            } else {
                builder.append(newMaskedCard.charAt(i));
            }
        }
        return builder.toString();
    }

    public static String getMaskedExpDate() {
        String bulletMask = "●●";
        return bulletMask + " / " + bulletMask;
    }

    public static String getMaskedCardCvv() {
        return "●●●";
    }

    public static List<BankBinsResponse> getBankBins(Context context) {
        List<BankBinsResponse> list = null;
        try {
            String data = readBankBinsFromJsonFile(context, "bank_bins.json");
            list = convertBankBinsFromJsonString(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static String readBankBinsFromJsonFile(Context context, String fileName) throws Exception {
        InputStream is = context.getAssets().open(fileName);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        return new String(buffer, "UTF-8");
    }

    private static List<BankBinsResponse> convertBankBinsFromJsonString(String data) {
        return new Gson().fromJson(data, getBankBinsType());
    }

    private static Type getBankBinsType() {
        return new TypeToken<List<BankBinsResponse>>() {
        }.getType();
    }

    public static String getBankByBin(List<BankBinsResponse> bankBins, String cardBin) {
        for (BankBinsResponse savedBankBin : bankBins) {
            if (savedBankBin.bins != null && !savedBankBin.bins.isEmpty()) {
                String bankBin = findBankByCardBin(savedBankBin, cardBin);
                if (bankBin != null) {
                    return bankBin;
                }
            }
        }
        return null;
    }

    private static String findBankByCardBin(BankBinsResponse savedBankBin, String cardBin) {
        for (String savedBin : savedBankBin.bins) {
            if (savedBin.contains(cardBin)) {
                return savedBankBin.bank;
            }
        }
        return null;
    }

    public static boolean isMandiriDebitCard(List<BankBinsResponse> bankBins, String cardBin) {
        BankBinsResponse mandiriDebitBins = getMandiriDebitResponse(bankBins);
        if (mandiriDebitBins != null) {
            String bankBin = findBankByCardBin(getMandiriDebitResponse(bankBins), cardBin);
            return bankBin != null;
        }
        return false;
    }

    private static BankBinsResponse getMandiriDebitResponse(List<BankBinsResponse> bankBins) {
        for (BankBinsResponse bankBinsResponse : bankBins) {
            if (bankBinsResponse.bank.equals(BankType.MANDIRI_DEBIT)) {
                return bankBinsResponse;
            }
        }
        return null;
    }

    public static void trackCreditCardFailure(CreditCardPaymentResponse object) {
        // track 3DS validation error
        if (object.validationMessages != null && object.validationMessages.get(0) != null) {
            if (object.validationMessages.get(0).contains("3d")) {
                Utils.trackEvent(AnalyticsEventName.CREDIT_CARD_3DS_ERROR, MidtransAnalytics.CARD_MODE_NORMAL);
            }
        }

        Utils.trackEvent(AnalyticsEventName.PAGE_STATUS_FAILED, MidtransAnalytics.CARD_MODE_NORMAL);
    }

    public static void trackCreditCardFailure(CreditCardPaymentResponse object, @NonNull SavedToken savedToken) {
        // track 3DS validation error
        if (object.validationMessages != null && object.validationMessages.get(0) != null) {
            if (object.validationMessages.get(0).contains("3d")) {
                if (savedToken.tokenType.equalsIgnoreCase(SavedToken.TWO_CLICKS)) {
                    Utils.trackEvent(AnalyticsEventName.CREDIT_CARD_3DS_ERROR, MidtransAnalytics.CARD_MODE_TWO_CLICK);
                    Utils.trackEvent(AnalyticsEventName.PAGE_STATUS_FAILED, MidtransAnalytics.CARD_MODE_TWO_CLICK);
                } else {
                    Utils.trackEvent(AnalyticsEventName.CREDIT_CARD_3DS_ERROR, MidtransAnalytics.CARD_MODE_ONE_CLICK);
                    Utils.trackEvent(AnalyticsEventName.PAGE_STATUS_FAILED, MidtransAnalytics.CARD_MODE_ONE_CLICK);
                }
            }
        }

        if (savedToken.tokenType.equalsIgnoreCase(SavedToken.TWO_CLICKS)) {
            Utils.trackEvent(AnalyticsEventName.PAGE_STATUS_FAILED, MidtransAnalytics.CARD_MODE_TWO_CLICK);
        } else {
            Utils.trackEvent(AnalyticsEventName.PAGE_STATUS_FAILED, MidtransAnalytics.CARD_MODE_ONE_CLICK);
        }
    }
}