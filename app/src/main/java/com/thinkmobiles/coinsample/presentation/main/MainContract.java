package com.thinkmobiles.coinsample.presentation.main;


import android.content.Context;
import android.net.Uri;
import android.text.Spannable;

import java.util.ArrayList;

public interface MainContract {

    interface LoginView {
        void showLoginButton(boolean show);
        void showProgress(boolean enabled);
        void showMsg(String text);
        void setUserName(String userName);
        void serUserEmail(String userEmail);
        void setRate(Spannable price);
        void setBalance(Spannable balance);
        void setAvailableCodes(ArrayList<String> codes);
        void setConvertResult(String result);
        void openDialog();
        void restart();
    }

    interface LoginPresenter {
        void attach();
        void syncPrice();
        void autoLogin();
        void login(final Context context);
        void logout();
        void completeLogin(final Context context, Uri uri);
        void detach();
        void exchange(String code, String amount);
        void doSend();
        void doRequest();
        void action(String email, int amount, String note);
    }

}
