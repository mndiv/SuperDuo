package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.services.BookService;


public class AddBook extends Fragment {
    private boolean mHasNetwork;
    private EditText ean;
    private final String EAN_CONTENT = "eanContent";


    public AddBook() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Get results of scanning and pass to EditText
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                isNetworkAvailable();
                if (mHasNetwork) {
                    //Get the ean number
                    String isbnNumber = result.getContents();
                    ean.setText(isbnNumber);

                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        Button searchView = (Button) rootView.findViewById(R.id.search_button);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateIsbn()) return;

                String isbnNumber = ean.getText().toString();
                ean.setText("");
                ean.setHint(R.string.input_hint);

                isNetworkAvailable();
                if (mHasNetwork) {
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, isbnNumber);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);

                    ((Callback) getActivity()).onItemSelected(isbnNumber);
                } else {
                    Toast.makeText(getActivity(), R.string.empty_book_list_no_network,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        ean.addTextChangedListener(new MyTextWatcher(ean));

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.
                ArrayList<String> formats = new ArrayList<>();
                formats.add("EAN_13");
                IntentIntegrator.forSupportFragment(AddBook.this).setPrompt("Scan a barcode")
                        .setCameraId(0)
                        .setBeepEnabled(false)
                        .initiateScan();

            }
        });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint(R.string.input_hint);
        }

        return rootView;
    }

    /**
     * TextWatcher class to watch EditText
     */
    private class MyTextWatcher implements TextWatcher {

        private final View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            if (view.getId() == R.id.ean)
                validateIsbn();
        }
    }

    /**
     * Helper method to validate ISBN
     */
    private boolean validateIsbn() {
        String isbnNumber = ean.getText().toString();
        if (isbnNumber.length() == 10 && !isbnNumber.startsWith("978")) {
            isbnNumber = "978" + isbnNumber;
        }
        return isbnNumber.length() >= 13;
    }

    /**
     * Returns true if the network is available or about to become available.
     */
    private void isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mHasNetwork = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }
}
