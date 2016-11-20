package com.lance.dinapolilevelscalc;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final String TAG = "wuliang";
    private Unbinder mUnbinder;
    @BindView(R.id.focus)
    EditText mFocus;
    @BindView(R.id.r1)
    EditText mR1;
    @BindView(R.id.r2)
    EditText mR2;
    @BindView(R.id.r3)
    EditText mR3;
    @BindView(R.id.r4)
    EditText mR4;
    @BindView(R.id.r5)
    EditText mR5;
    @BindView(R.id.retraceBtn)
    Button mRetraceBtn;
    @BindView(R.id.retraceClear)
    Button mRetraceClear;
    @BindView(R.id.f3_1)
    TextView mF3_1;
    @BindView(R.id.f3_2)
    TextView mF3_2;
    @BindView(R.id.f3_3)
    TextView mF3_3;
    @BindView(R.id.f3_4)
    TextView mF3_4;
    @BindView(R.id.f3_5)
    TextView mF3_5;
    @BindView(R.id.f5_1)
    TextView mF5_1;
    @BindView(R.id.f5_2)
    TextView mF5_2;
    @BindView(R.id.f5_3)
    TextView mF5_3;
    @BindView(R.id.f5_4)
    TextView mF5_4;
    @BindView(R.id.f5_5)
    TextView mF5_5;
    @BindView(R.id.aValue)
    EditText mAValue;
    @BindView(R.id.bValue)
    EditText mBValue;
    @BindView(R.id.cValue)
    EditText mCValue;
    @BindView(R.id.objectiveBtn)
    Button mTargetBtn;
    @BindView(R.id.objectiveClear)
    Button mTargetClear;
    @BindView(R.id.cop)
    TextView mCOP;
    @BindView(R.id.op)
    TextView mOP;
    @BindView(R.id.xop)
    TextView mXOP;
    @BindView(R.id.seekBarLabel)
    TextView mSeekBarLabel;
    @BindView(R.id.compound)
    TextView mCompound;
    @BindView(R.id.seekBar)
    SeekBar mSeekBar;

    private float mMaxRatio = 0.25f;
    private List<Float[]> mRetracePoints = new ArrayList<>();
    private float mCopPoint, mOpPoint, mXopPoint;
    private int mDecimalPlaces;

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        private int mProgress;

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mProgress = progress;
            mSeekBarLabel.setText(
                    String.format(getResources().getString(R.string.seek_label), mProgress));
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            mMaxRatio = (float) 0.01 * mProgress;
            calcConfluenceAndAgreement();
            // Log.d(TAG, "onStopTrackingTouch = " + mProgress);
        }
    };

    public static float F3(float A, float F) {
        return (float) (F - 0.382 * (F - A));
    }

    public static float F5(float A, float F) {
        return (float) (F - 0.618 * (F - A));
    }

    public static float OP(float A, float B, float C) {
        return B - A + C;
    }

    public static float COP(float A, float B, float C) {
        return (float) (0.618 * (B - A) + C);
    }

    public static float XOP(float A, float B, float C) {
        return (float) (1.618 * (B - A) + C);
    }

    /**
     * Checks if an EditText instance has valid input.
     *
     * @param editText the EditText instance to be checked.
     * @return true if valid, false otherwise.
     */
    public static boolean checkInputValid(EditText editText) {
        return editText.getText() != null
                && editText.getText().length() != 0 && !editText.getText().toString().isEmpty();
    }

    /**
     * Retrieves the float number in the EditText
     *
     * @param editText the EditText instance to get in.
     * @return a float number
     */
    public static float getFloatNumber(EditText editText) {
        return Float.valueOf(editText.getText().toString());
    }

    /**
     * Count the digits after the decimal point for the given float number.
     *
     * @param num the float number to be examined.
     * @return the number of digits after the decimal points if any.
     */
    public static int countDecimalPlaces(float num) {
        String text = Float.toString(num);
        int integerPlaces = text.indexOf('.');
        if (integerPlaces < 0) {
            return 0;
        }
        int decimalPlaces = text.length() - integerPlaces - 1;
        if (decimalPlaces == 1 && '0' == text.charAt(integerPlaces + 1)) {
            // In this case it is an int number actually
            return 0;
        }
        return decimalPlaces;
    }

    /**
     * Calculate retrace levels and set the TextViews.
     *
     * @return the updated decimalPlaces
     */
    private int calcRetrace(float focus, EditText reaction
            , TextView f3View, TextView f5View, int decimalPlaces) {
        if (checkInputValid(reaction)) {
            float reactionNum, f3, f5;
            reactionNum = getFloatNumber(reaction);
            if (countDecimalPlaces(reactionNum) > decimalPlaces) {
                decimalPlaces = countDecimalPlaces(reactionNum);
            }
            f3 = F3(reactionNum, focus);
            f5 = F5(reactionNum, focus);
            // decimalPlaces should be at least 2, otherwise if both the focus
            // and the reactionNum are int numbers not bigger than 10,
            // the final results would seem meaningless.
            String fmt = "%." + (decimalPlaces > 2 ? decimalPlaces : 2) + "f";
            f3View.setText(String.format(fmt, f3));
            f5View.setText(String.format(fmt, f5));

            mRetracePoints.add(new Float[]{f3, f5, reactionNum});
        }

        return decimalPlaces;
    }

    /**
     * Calculate confluence areas and agreement areas
     * and set the TextView.
     */
    private void calcConfluenceAndAgreement() {
        final int decimalPlaces = mDecimalPlaces;
        final String fmt = "%." + (decimalPlaces > 2 ? decimalPlaces : 2) + "f";
        // First, calculate the confluence area
        // The distance of the confluence area should be no more than a preset distance,
        // say, the preset distance is a quarter of the distance between a pair of f3 and f5
        // a quarter is actually 0.25.
        // Then, evaluate the agreement area likewise.
        Resources res = getResources();
        String ptConfluence = res.getString(R.string.confluence_leading);
        String ptAgreement = res.getString(R.string.agreement_leading);
        float[] f3 = new float[5];
        float[] f5 = new float[5];
        final int numPairs = mRetracePoints.size();
        for (int i = 0; i < numPairs; i++) {
            f3[i] = mRetracePoints.get(i)[0];
            f5[i] = mRetracePoints.get(i)[1];
        }
        for (int i = 0; i < numPairs; ++i) {
            float gateDelta = mMaxRatio * Math.abs(f3[i] - f5[i]);
            float delta;
            // Log.d(TAG, "i = " + i);
            // Log.d(TAG, "-->  f3[" + i + "] = " + f3[i] + ", f5[" + i + "]=" + f5[i]);

            // confluence areas
            for (int j = 0; j < numPairs; j++) {
                if (j == i) {
                    continue;
                }
                // Log.d(TAG, "-->  j = " + j);
                delta = Math.abs(f3[i] - f5[j]);
                // Log.d(TAG, "-->  f3[" + i + "] = " + f3[i] + ", f5[" + j + "]=" + f5[j] + ", delta=" + delta);
                if (delta < gateDelta) {
                    // Log.d(TAG, "-->  f3[" + i + "] = " + f3[i] + ", f5[" + j + "]=" + f5[j]
                    //         + " are a pair of confluence points.");
                    // f3[i] and f5[j] are possibly a pair of confluence points
                    ptConfluence += "(" + (i + 1) + ")" + ". F3: " + String.format(fmt, f3[i])
                            + ", (" + (j + 1) + ")"  + ". F5: " + String.format(fmt, f5[j]) + "\n";
                }
            }

            // agreement areas
            delta = Math.abs(f3[i] - mCopPoint);
            if (delta < gateDelta) {
                ptAgreement += "(" + (i + 1) + ")" + ". F3: " + String.format(fmt, f3[i]) + ", COP: " + String.format(fmt, mCopPoint) + "\n";
            }
            delta = Math.abs(f3[i] - mOpPoint);
            if (delta < gateDelta) {
                ptAgreement += "(" + (i + 1) + ")" + ". F3: " + String.format(fmt, f3[i]) + ", OP: " + String.format(fmt, mOpPoint) + "\n";
            }
            delta = Math.abs(f3[i] - mXopPoint);
            if (delta < gateDelta) {
                ptAgreement += "(" + (i + 1) + ")" + ". F3: " + String.format(fmt, f3[i]) + ", XOP: " + String.format(fmt, mXopPoint) + "\n";
            }

            delta = Math.abs(f5[i] - mCopPoint);
            if (delta < gateDelta) {
                ptAgreement += "(" + (i + 1) + ")" + ". F5: " + String.format(fmt, f5[i]) + ", COP: " + String.format(fmt, mCopPoint) + "\n";
            }
            delta = Math.abs(f5[i] - mOpPoint);
            if (delta < gateDelta) {
                ptAgreement += "(" + (i + 1) + ")" + ". F5: " + String.format(fmt, f5[i]) + ", OP: " + String.format(fmt, mOpPoint) + "\n";
            }
            delta = Math.abs(f5[i] - mXopPoint);
            if (delta < gateDelta) {
                ptAgreement += "(" + (i + 1) + ")" + ". F5: " + String.format(fmt, f5[i]) + ", XOP: " + String.format(fmt, mXopPoint) + "\n";
            }
        }


        String compound = "";
        if (!ptConfluence.equals(res.getString(R.string.confluence_leading))) {
            compound += ptConfluence + "\n";
        }
        if (!ptAgreement.equals(res.getString(R.string.agreement_leading))) {
            compound += ptAgreement;
        }
        if (!compound.isEmpty()) {
            mCompound.setText(compound);
        }
    }

    @OnClick(R.id.retraceClear)
    public void retraceClear() {
        mFocus.setText("");
        mR1.setText("");
        mR2.setText("");
        mR3.setText("");
        mR4.setText("");
        mR5.setText("");
        mF3_1.setText("");
        mF5_1.setText("");
        mF3_2.setText("");
        mF5_2.setText("");
        mF3_3.setText("");
        mF5_3.setText("");
        mF3_4.setText("");
        mF5_4.setText("");
        mF3_5.setText("");
        mF5_5.setText("");
        mRetracePoints.clear();
        mCompound.setText("");
    }

    @OnClick(R.id.retraceBtn)
    public void retraceAnalysis() {
        if (!checkInputValid(mFocus)) {
            return;
        }

        mRetracePoints.clear();

        float focus = getFloatNumber(mFocus);
        int decimalPlaces = countDecimalPlaces(focus);
        decimalPlaces = calcRetrace(focus, mR1, mF3_1, mF5_1, decimalPlaces);
        decimalPlaces = calcRetrace(focus, mR2, mF3_2, mF5_2, decimalPlaces);
        decimalPlaces = calcRetrace(focus, mR3, mF3_3, mF5_3, decimalPlaces);
        decimalPlaces = calcRetrace(focus, mR4, mF3_4, mF5_4, decimalPlaces);
        decimalPlaces = calcRetrace(focus, mR5, mF3_5, mF5_5, decimalPlaces);
        mDecimalPlaces = decimalPlaces;

        calcConfluenceAndAgreement();
    }

    @OnClick(R.id.objectiveClear)
    public void targetClear() {
        mAValue.setText("");
        mBValue.setText("");
        mCValue.setText("");
        mCOP.setText("");
        mOP.setText("");
        mXOP.setText("");
        mCopPoint = 0;
        mOpPoint = 0;
        mXopPoint = 0;
    }

    @OnClick(R.id.objectiveBtn)
    public void targetAnalysis() {
        if (!checkInputValid(mAValue) || !checkInputValid(mBValue)
                || !checkInputValid(mCValue)) {
            return;
        }
        float aValue, bValue, cValue;
        try {
            aValue = getFloatNumber(mAValue);
            bValue = getFloatNumber(mBValue);
            cValue = getFloatNumber(mCValue);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return;
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return;
        }

        mCopPoint = COP(aValue, bValue, cValue);
        mOpPoint = OP(aValue, bValue, cValue);
        mXopPoint = XOP(aValue, bValue, cValue);

        mCOP.setText(Float.toString(mCopPoint));
        mOP.setText(Float.toString(mOpPoint));
        mXOP.setText(Float.toString(mXopPoint));

        calcConfluenceAndAgreement();
    }

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        SeekBar seekBar = mSeekBar;
        seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mSeekBarLabel.setText(
                String.format(getResources().getString(R.string.seek_label), seekBar.getProgress()));
        mMaxRatio = 0.01f * seekBar.getProgress();
        // Log.d(TAG, "seekBar=" + seekBar);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
