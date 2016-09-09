package com.ugurdede.smalltaskrxjavaandroid;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().isEmpty()) {
                    button.setEnabled(false);
                    createObservableAndObserver();
                    editText.setEnabled(false);
                    editText.setText("");
                    textView.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Empty input!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //create an alert dialog to show message box
    private void createAlertDialog(String message) {

        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle("Alert Dialog");

        // Setting Dialog Message
        alertDialog.setMessage("Welcome, " + message);

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    //create observable emitting the items and observer consuming the emitted items
    private void createObservableAndObserver() {
        Observable<String> myObservable = Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> sub) {

                        final String text = editText.getText().toString();
                        sub.onNext(text);

                        //subscription for alert dialog
                        sub.add(Subscriptions.create(new Action0() {
                            @Override
                            public void call() {
                                //call alertDialog method
                                createAlertDialog(" " + text);

                            }
                        }));
                        sub.onCompleted();

                    }
                }
        ).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                //transform the input to uppercase
                return s.toUpperCase();
            }
        }).filter(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String s) {

                return s.length() > 0;
            }
        }).delay(5000, TimeUnit.MILLISECONDS);

        //observer consuming the emitted items
        Observer<String> myObserver = new Observer<String>() {

            @Override
            public void onCompleted() {
                //Enable UI elements after completed
                button.setEnabled(true);
                editText.setEnabled(true);

            }

            @Override
            public void onError(Throwable e) {
                //in case an error occurs
                Log.d("DEDE", "observerOnError:" + e.getMessage());
            }

            @Override
            public void onNext(String string) {
                //set textview text to emitted item
                textView.setText(string);

            }
        };
        //make the observer subscribe the observable
        myObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(myObserver);
    }
}
