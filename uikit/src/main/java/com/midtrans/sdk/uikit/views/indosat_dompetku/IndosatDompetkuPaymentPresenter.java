package com.midtrans.sdk.uikit.views.indosat_dompetku;

import com.midtrans.sdk.corekit.callback.TransactionCallback;
import com.midtrans.sdk.corekit.models.TransactionResponse;
import com.midtrans.sdk.uikit.abstracts.BasePaymentPresenter;
import com.midtrans.sdk.uikit.abstracts.BasePaymentView;
import com.midtrans.sdk.uikit.constants.AnalyticsEventName;

/**
 * Created by Fajar on 31/10/17.
 */

public class IndosatDompetkuPaymentPresenter  extends BasePaymentPresenter<BasePaymentView> {

    public IndosatDompetkuPaymentPresenter(BasePaymentView view) {
        super();
        this.view = view;
    }

    public void startPayment(String msidn) {
        getMidtransSDK().paymentUsingIndosatDompetku(getMidtransSDK().readAuthenticationToken(), msidn,
            new TransactionCallback() {
                @Override
                public void onSuccess(TransactionResponse response) {
                    transactionResponse = response;
                    view.onPaymentSuccess(response);
                    trackEvent(AnalyticsEventName.PAGE_STATUS_PENDING);
                }

                @Override
                public void onFailure(TransactionResponse response, String reason) {
                    transactionResponse = response;
                    view.onPaymentFailure(response);
                    trackEvent(AnalyticsEventName.PAGE_STATUS_FAILED);
                }

                @Override
                public void onError(Throwable error) {
                    view.onPaymentError(error);
                }
            });
    }
}
