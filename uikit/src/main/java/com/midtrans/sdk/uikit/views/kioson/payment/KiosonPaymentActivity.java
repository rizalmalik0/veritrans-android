package com.midtrans.sdk.uikit.views.kioson.payment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import com.midtrans.sdk.corekit.models.TransactionResponse;
import com.midtrans.sdk.uikit.R;
import com.midtrans.sdk.uikit.abstracts.BasePaymentActivity;
import com.midtrans.sdk.uikit.abstracts.BasePaymentView;
import com.midtrans.sdk.uikit.utilities.UiKitConstants;
import com.midtrans.sdk.uikit.views.kioson.status.KiosonStatusActivity;
import com.midtrans.sdk.uikit.widgets.FancyButton;

/**
 * Created by Fajar on 26/10/17.
 */

public class KiosonPaymentActivity extends BasePaymentActivity implements BasePaymentView {

    private FancyButton buttonPrimary;

    private KiosonPaymentPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kioson_payment);
        initProperties();
        initActionButton();
        bindData();
    }

    private void bindData() {
        setPageTitle(getString(R.string.payment_method_kioson));
        buttonPrimary.setText(getString(R.string.confirm_payment));
        buttonPrimary.setTextBold();
    }

    private void initActionButton() {
        buttonPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressLayout(getString(R.string.processing_payment));
                presenter.startPayment();
            }
        });
    }

    private void initProperties() {
        this.presenter = new KiosonPaymentPresenter(this);
    }


    @Override
    public void bindViews() {
        buttonPrimary = (FancyButton) findViewById(R.id.button_primary);
    }

    @Override
    public void initTheme() {
        setPrimaryBackgroundColor(buttonPrimary);
    }

    @Override
    public void onPaymentSuccess(TransactionResponse response) {
        hideProgressLayout();
        if (isActivityRunning()) {
            Intent intent = new Intent(this, KiosonStatusActivity.class);
            intent.putExtra(KiosonStatusActivity.EXTRA_PAYMENT_STATUS, response);
            startActivityForResult(intent, UiKitConstants.INTENT_CODE_PAYMENT_STATUS);
        } else {
            finishPayment(RESULT_OK, presenter.getTransactionResponse());
        }
    }

    @Override
    public void onPaymentFailure(TransactionResponse response) {
        hideProgressLayout();
        showPaymentStatusPage(response, presenter.isShowPaymentStatusPage());
    }

    @Override
    public void onPaymentError(Throwable error) {
        hideProgressLayout();
        showOnErrorPaymentStatusmessage(error);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UiKitConstants.INTENT_CODE_PAYMENT_STATUS) {
            finishPayment(RESULT_OK, presenter.getTransactionResponse());
        }
    }
}
