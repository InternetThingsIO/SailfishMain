package io.internetthings.sailfish;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class FTUE extends Activity {

    RadioGroup rg;
    Button next;

    int selectedID;
    String selectedEMAIL;
    String emailList[] = {"test@gmail.com",
                            "test2@gmail.com",
                            "test3@gmail.com",
                            "test4@gmail.com",
                            "test5@gmail.com"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftue);

        addRadioButtons(emailList.length);

    }


    private void addRadioButtons(int number){

        LinearLayout ll = (LinearLayout) findViewById(R.id.radiogroup);
        rg = new RadioGroup(this);
        next = (Button) findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedID = rg.getCheckedRadioButtonId();
                selectedEMAIL = emailList[selectedID - 1];
                Log.i("", selectedEMAIL + "\n"+ "RadioButton ID: " + String.valueOf(selectedID));
            }
        });


        TextView title = new TextView(this);
        title.setText("Select Email:");
        title.setTextColor(Color.RED);
        ll.addView(title);

        final RadioButton[] rb = new RadioButton[emailList.length];

        rg.setOrientation(RadioGroup.VERTICAL);

        for(int i = 0; i < number; i++) {
            rb[i] = new RadioButton(this);
            rg.addView(rb[i]);
            rb[i].setText(emailList[i]);
        }
        ll.addView(rg);

    }
}
