package com.thinkmobiles.coinsample.network;

import android.content.Context;
import android.net.Uri;

import com.coinbase.android.sdk.OAuth;
import com.coinbase.api.Coinbase;
import com.coinbase.api.CoinbaseBuilder;
import com.coinbase.api.entity.OAuthTokensResponse;
import com.coinbase.api.entity.Transaction;
import com.coinbase.api.entity.User;
import com.thinkmobiles.coinsample.presentation.SharedPref;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class API {

    private static final String CLIENT_ID = "55589ac34ca3df2be6fd8709e67d8949bb3da04b5289bd01ed5a71ab10e0b430";
    private static final String CLIENT_SECRET = "7958ec22aa7352d6f2ee8111b7adfdf3e541423f573c64d38e84258322f27599";
    private static final String REDIRECT_URI = "bwallet://coinbase-oauth";

    private static Coinbase coinbase;

    private static API api = new API();

    public static API getInstance() {
        return api;
    }

    public static void setToken(String accessToken) {
        coinbase = new CoinbaseBuilder()
                .withAccessToken(accessToken)
                .build();
    }

    private API() {
        coinbase = new CoinbaseBuilder().build();
    }

    private <T> Observable<T> getNetworkObservable(Observable<T> observable) {
        return observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<User> login(final Context context, final Uri uri) {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(ObservableEmitter<User> e) throws Exception {
                OAuthTokensResponse response = OAuth.completeAuthorization(context, CLIENT_ID, CLIENT_SECRET, uri);
                SharedPref.getInstance().saveOAuthResponse(response);
                coinbase = new CoinbaseBuilder()
                        .withAccessToken(response.getAccessToken())
                        .build();
                e.onNext(coinbase.getUser());
            }
        }));
    }

    public Observable<String> redirect(final Context context) {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                OAuth.beginAuthorization(context, CLIENT_ID, "user", REDIRECT_URI, null);
                e.onNext("OK");
            }
        }));
    }

    public Observable<User> refreshTokenAndLogin(final String refreshToken) {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(ObservableEmitter<User> e) throws Exception {
                OAuthTokensResponse response = coinbase.refreshTokens(CLIENT_ID, CLIENT_SECRET, refreshToken);
                SharedPref.getInstance().saveOAuthResponse(response);
                coinbase = new CoinbaseBuilder()
                                .withAccessToken(response.getAccessToken())
                                .build();
                e.onNext(coinbase.getUser());
            }
        }));
    }

    public Observable<User> getCurrentUser() {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(ObservableEmitter<User> e) throws Exception {
                e.onNext(coinbase.getUser());
            }
        }));
    }

    public Observable<Money> getSpotPrice() {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<Money>() {
            @Override
            public void subscribe(ObservableEmitter<Money> e) throws Exception {
                e.onNext(coinbase.getSpotPrice(CurrencyUnit.USD));
            }
        }));
    }

    public Observable<Map<String, BigDecimal>> getExchangeRates() {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<Map<String, BigDecimal>>() {
            @Override
            public void subscribe(ObservableEmitter<Map<String, BigDecimal>> e) throws Exception {
                e.onNext(coinbase.getExchangeRates());
            }
        }));
    }

    public Observable<Transaction> sendMoney(final Transaction transaction) {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<Transaction>() {
            @Override
            public void subscribe(ObservableEmitter<Transaction> e) throws Exception {
                e.onNext(coinbase.sendMoney(transaction));
            }
        }));
    }

    public Observable<Transaction> requestMoney(final Transaction transaction) {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<Transaction>() {
            @Override
            public void subscribe(ObservableEmitter<Transaction> e) throws Exception {
                e.onNext(coinbase.requestMoney(transaction));
            }
        }));
    }


}
