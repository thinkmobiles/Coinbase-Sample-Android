package com.thinkmobiles.coinsample.presentation.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.thinkmobiles.coinsample.R;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MainContract.LoginView {

    private MainContract.LoginPresenter presenter;

    private View rootView;
    private View divider;
    private Button btnLogin;
    private LinearLayout llOperations;
    private TextView tvRate;
    private TextView tvConverted;
    private TextView tvBalanceTitle;
    private TextView tvBalance;
    private EditText etMoney;
    private Spinner spCodes;

    private Snackbar snackbar;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        presenter = new MainPresenter(this);

        presenter.attach();
    }

    private void initViews() {
        rootView = findViewById(android.R.id.content);
        divider = findViewById(R.id.vDivider);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvRate = (TextView) findViewById(R.id.tvRate);
        tvBalance = (TextView) findViewById(R.id.tvBalance);
        tvBalanceTitle = (TextView) findViewById(R.id.tvBalanceTitle);
        tvConverted = (TextView) findViewById(R.id.tvConverted);
        etMoney = (EditText) findViewById(R.id.etMoney);
        llOperations = (LinearLayout) findViewById(R.id.llOperations);
        spCodes = (Spinner) findViewById(R.id.spCodes);

        etMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0)
                    presenter.exchange(adapter.getItem(spCodes.getSelectedItemPosition()), s.toString());
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.login(MainActivity.this);
            }
        });

        findViewById(R.id.btnRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.doRequest();
            }
        });

        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.doSend();
            }
        });

        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<String>());
        spCodes.setAdapter(adapter);
        spCodes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String amount = etMoney.getText().toString();
                if (amount.length() > 0)
                    presenter.exchange(adapter.getItem(position), amount);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("android.intent.action.VIEW")) {
            presenter.completeLogin(MainActivity.this, intent.getData());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miSync:
                tvRate.setEnabled(false);
                presenter.syncPrice();
                return true;
            case R.id.miLogout:
                presenter.logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detach();
    }

    @Override
    public void showLoginButton(boolean show) {
        btnLogin.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            tvBalanceTitle.setVisibility(View.GONE);
            tvBalance.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setVisibility(View.GONE);
            tvBalanceTitle.setVisibility(View.VISIBLE);
            tvBalance.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            llOperations.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showProgress(boolean enabled) {
        if (enabled) {
            snackbar = Snackbar.make(rootView, "Please wait...", BaseTransientBottomBar.LENGTH_INDEFINITE);
            snackbar.show();
        } else {
            snackbar.dismiss();
        }
    }

    @Override
    public void showMsg(String text) {
        Snackbar.make(rootView, text, BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    @Override
    public void setUserName(String userName) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(userName);
        }
    }

    @Override
    public void serUserEmail(String userEmail) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(userEmail);
        }
    }

    @Override
    public void setRate(Spannable price) {
        tvRate.setEnabled(true);
        tvRate.setText(price);
    }

    @Override
    public void setBalance(Spannable balance) {
        tvBalance.setText(balance);
    }

    @Override
    public void setAvailableCodes(ArrayList<String> codes) {
        adapter.clear();
        adapter.addAll(codes);
    }

    @Override
    public void setConvertResult(String result) {
        tvConverted.setText(result);
    }

    @Override
    public void openDialog() {
        BottomSheetDialogFragment dialogFragment = new TransactionDialog();
        dialogFragment.show(getSupportFragmentManager(), "Action");
    }

    public void setDialogResult(String email, int amount, String note) {
        presenter.action(email, amount, note);
    }

    @Override
    public void restart() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
