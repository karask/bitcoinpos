package gr.cryptocurrencies.bitcoinpos;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AboutFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutFragment extends DialogFragment {

    private OnFragmentInteractionListener mListener;

    private Button mOkButton;
    private TextView mAppNameVersionTextView, mCopyrightAuthorNameTextView;
    public AboutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AboutFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Find screen size
        WindowManager manager = (WindowManager) getActivity().getSystemService(getContext().WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;

        // dialog is 3/4 of max screen width and height
        getDialog().getWindow().setLayout(width * 5/6, height * 2/3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_about, container, false);

        mOkButton = (Button) fragmentView.findViewById(R.id.about_ok_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call parents method to close dialog fragment
                mListener.onAboutOk();
            }
        });

        Resources res = getResources();

        mAppNameVersionTextView = (TextView) fragmentView.findViewById(R.id.appname_version_textview);
        mAppNameVersionTextView.setText(String.format(res.getString(R.string.app_name_version), res.getString(R.string.app_name), res.getString(R.string.version)));

        mCopyrightAuthorNameTextView = (TextView) fragmentView.findViewById(R.id.copyright_author_textview);
        mCopyrightAuthorNameTextView.setText(String.format(res.getString(R.string.copyright_author), res.getString(R.string.copyright), res.getString(R.string.konstantinos_karasavvas)));

        return fragmentView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAboutOk();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onAboutOk();
    }

}
