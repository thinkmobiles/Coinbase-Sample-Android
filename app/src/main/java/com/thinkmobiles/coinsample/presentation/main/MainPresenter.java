package com.thinkmobiles.coinsample.presentation.main;


import android.content.Context;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import com.coinbase.api.entity.Transaction;
import com.coinbase.api.entity.User;
import com.thinkmobiles.coinsample.network.API;
import com.thinkmobiles.coinsample.presentation.SharedPref;


import org.joda.money.CurrencyUnit;
import org.joda.money.Money;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MainPresenter implements MainContract.LoginPresenter {

    private MainContract.LoginView view;
    private CompositeDisposable subscriptions;
    private Map<String, BigDecimal> rates;

    private boolean isSend;

    public MainPresenter(MainContract.LoginView view) {
        this.view = view;
        subscriptions = new CompositeDisposable();
        rates = new HashMap<>();
    }

    @Override
    public void attach() {
        autoLogin();
        syncPrice();
        getRates();
    }

    @Override
    public void syncPrice() {
        subscriptions.add(API.getInstance().getSpotPrice()
                .subscribe(new Consumer<Money>() {
                    @Override
                    public void accept(Money money) throws Exception {
                        view.setRate(getMoney(money));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        view.showMsg(throwable.getMessage());
                    }
                }));
    }

    private void getRates() {
        subscriptions.add(API.getInstance().getExchangeRates()
                .subscribe(new Consumer<Map<String, BigDecimal>>() {
                    @Override
                    public void accept(Map<String, BigDecimal> exchangeRates) throws Exception {
                        rates = exchangeRates;
                        List<CurrencyUnit> list = CurrencyUnit.registeredCurrencies();
                        ArrayList<String> codes = new ArrayList<>();
                        for (CurrencyUnit unit : list) {
                            if (rates.containsKey(String.format(Locale.ENGLISH, "%s_to_btc", unit.getCode().toLowerCase()))) {
                                codes.add(unit.getCode());
                            }
                        }
                        view.setAvailableCodes(codes);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        view.showMsg(throwable.getMessage());
                    }
                }));
    }

    @Override
    public void autoLogin() {
        String token = SharedPref.getInstance().getAccessToken();
        if (token != null) {
            view.showProgress(true);
            subscriptions.add(API.getInstance().refreshTokenAndLogin(SharedPref.getInstance().getRefreshToken())
                    .subscribe(new Consumer<User>() {
                        @Override
                        public void accept(User user) throws Exception {
                            setUserData(user);
                            view.showProgress(false);
                            view.showLoginButton(false);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            view.showProgress(false);
                            view.showMsg(throwable.getMessage());
                            view.showLoginButton(true);
                        }
                    }));
        } else {
            view.showLoginButton(true);
        }
    }

    @Override
    public void login(final Context context) {
        subscriptions.add(API.getInstance().redirect(context)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d("tag", s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        view.showMsg(throwable.getMessage());
                    }
                }));
    }

    @Override
    public void completeLogin(final Context context, Uri uri) {
        view.showProgress(true);
        subscriptions.add(API.getInstance().login(context, uri)
                .subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        setUserData(user);
                        view.showProgress(false);
                        view.showLoginButton(false);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        view.showMsg(throwable.getMessage());
                    }
                }));
    }

    private void setUserData(User user) {
        view.setUserName(user.getName());
        view.serUserEmail(user.getEmail());
        view.setBalance(getMoney(user.getBalance() != null ? user.getBalance() : Money.zero(CurrencyUnit.USD)));
    }

    private Spannable getMoney(Money money) {
        String m = money.toString();
        SpannableString span = new SpannableString(m);
        span.setSpan(new RelativeSizeSpan(0.5f), 0, m.indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    @Override
    public void exchange(String code, String amount) {
        String key = code.toLowerCase().concat("_to_").concat("btc");
        BigDecimal rate = rates.get(key);
        if (rate != null) {
            view.setConvertResult(rate.multiply(BigDecimal.valueOf(Double.valueOf(amount))).toString());
        }
    }

    @Override
    public void detach() {
        subscriptions.clear();
    }

    @Override
    public void doSend() {
        isSend = true;
        view.openDialog();
    }

    @Override
    public void doRequest() {
        isSend = false;
        view.openDialog();
    }

    private void send(String email, int amount, String note) {
        Transaction t = new Transaction();
        t.setTo(email);
        t.setAmount(Money.of(CurrencyUnit.USD, amount));
        t.setNotes(note);
        subscriptions.add(API.getInstance().sendMoney(t).subscribe(new Consumer<Transaction>() {
            @Override
            public void accept(Transaction transaction) throws Exception {
                view.showMsg("Success");
                view.showProgress(false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                view.showMsg(throwable.toString());
            }
        }));
    }

    private void request(String email, int amount, String note) {
        Transaction t = new Transaction();
        t.setFrom(email);
        t.setAmount(Money.of(CurrencyUnit.USD, amount));
        t.setNotes(note);
        subscriptions.add(API.getInstance().sendMoney(t).subscribe(new Consumer<Transaction>() {
            @Override
            public void accept(Transaction transaction) throws Exception {
                view.showMsg("Success");
                view.showProgress(false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                view.showMsg(throwable.toString());
            }
        }));
    }

    @Override
    public void action(String email, int amount, String note) {
        view.showProgress(true);
        if (isSend) {
            send(email, amount, note);
        } else {
            request(email, amount, note);
        }
    }

    @Override
    public void logout() {
        SharedPref.getInstance().clearTokens();
        view.restart();
    }
}
