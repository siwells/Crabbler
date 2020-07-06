package com.github.hintofbasil.crabbler.Questions;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.hintofbasil.crabbler.AboutUsActivity;
import com.github.hintofbasil.crabbler.Keys;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.ChoiceSelectExpander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.UserDetailsExpander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.DateRangeExpander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.DateRangeSelectExpander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.DayNightChoiceExpander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.DoneExpander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.Expander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.TwoChoiceExpander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.TwoChoiceDateExpander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.TwoPictureLayoutExpander;
import com.github.hintofbasil.crabbler.Questions.QuestionExpanders.YesNoExtraExpander;
import com.github.hintofbasil.crabbler.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class QuestionActivity extends AppCompatActivity {

    Expander expander;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            int questionId = getIntent().getIntExtra(Keys.QUESTION_ID_KEY, 0);
            if(QuestionManager.get() == null) {
                QuestionManager.init(this);
            }
            QuestionManager qr = QuestionManager.get();
            JSONObject questionJson = null;

            // TODO make done an excludeFromCount question
            if(questionId==qr.getRealQuestionCount()) {
                expander = new DoneExpander(this, null);
            } else {
                questionJson = qr.getJsonQuestion(questionId);
                switch (questionJson.getString("questionType")) {
                    case "TwoPictureChoice":
                        expander = new TwoPictureLayoutExpander(this, questionJson);
                        break;
                    case "TwoChoiceDate":
                        expander = new TwoChoiceDateExpander(this, questionJson);
                        break;
                    case "TwoChoice":
                        expander = new TwoChoiceExpander(this, questionJson);
                        break;
                    case "DateRange":
                        expander = new DateRangeExpander(this, questionJson);
                        break;
                    case "DateRangeSelect":
                        expander = new DateRangeSelectExpander(this, questionJson);
                        break;
                    case "ListSelect":
                        expander = new ChoiceSelectExpander(this, questionJson);
                        break;
                    case "YesNoExtra":
                        expander = new YesNoExtraExpander(this, questionJson);
                        break;
                    case "DayNightChoice":
                        expander = new DayNightChoiceExpander(this, questionJson);
                        break;
                    case "UserDetails":
                        expander = new UserDetailsExpander(this, questionJson);
                        break;
                    case "Done":
                        expander = new DoneExpander(this, questionJson);
                        break;
                    default:
                        Log.e("QuestionActivity", "Unknown question type.");
                        return;
                }
            }
            expander.expandLayout();

            try {
                questionJson.getBoolean("hideBackNext");
                ImageView previousButton = (ImageView) findViewById(R.id.back_button);
                ImageView nextButton = (ImageView) findViewById(R.id.forward_button);
                previousButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
            } catch (JSONException|NullPointerException e) {

            }

            TextView pageOf = (TextView) findViewById(R.id.page_of);
            int realQuestionCount = qr.getRealQuestionCount();
            int definedQuestionCount = qr.getQuestionCount();
            try {
                qr.getJsonQuestion(questionId).getInt("questionNumber");
                pageOf.setText(String.format("%d/%d", questionId + 1, definedQuestionCount));
            } catch (JSONException|NullPointerException e) {
                pageOf.setVisibility(View.INVISIBLE);
                Log.i("QuestionActivity", "No question number.  Not displaying question of question");
            }

            //Disable previous
            if(questionId==0) {
                ImageView previousButton = (ImageView) findViewById(R.id.back_button);
                previousButton.setEnabled(false);
            }

            //Disable next
            if(questionId==realQuestionCount) {
                ImageView nextButton = (ImageView) findViewById(R.id.forward_button);
                nextButton.setEnabled(false);
            }

            if(questionId > 0) { // Only show menu button on first question
                LinearLayout menuButton = (LinearLayout) findViewById(R.id.toolbar_menu_button);
                menuButton.setVisibility(View.GONE);
            } else { //Hide images if menu button is present
                LinearLayout toolbarImages = (LinearLayout) findViewById(R.id.toolbar_images);
                toolbarImages.setVisibility(View.GONE);
            }

        } catch (IOException|JSONException e) {
            Log.e("QuestionActivity", "Error reading questions\n" + Log.getStackTraceString(e));
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(expander!=null) {
            expander.setPreviousAnswer();
            expander.enableDisableNext();
        }
    }

    public void previousQuestion(View view) {
        expander.previousQuestion();
    }

    public void nextQuestion(View view) {
        expander.nextQuestion(0);
    }

    public void menuClick(View view) {
        Log.i("QuestionActivity", "Launching about us");
        Intent intent = new Intent(getBaseContext(),
                AboutUsActivity.class);
        startActivity(intent);
    }
}
