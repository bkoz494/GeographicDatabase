package com.example.geographicdatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    // UI objects
    TextView question;
    Button ansA, ansB, ansC, ansD;
    Button submitButton;
    TextView message;
    // Management objests
    DBHelper dbHelper;
    SQLiteDatabase sqldb = null;
    char userAns;
    Random rand = new Random();
    int NCOL = 4;
    int NROW = 26;
    char goodAns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        question = findViewById(R.id.quesion);
        ansA = findViewById(R.id.answerA);
        ansB = findViewById(R.id.answerB);
        ansC = findViewById(R.id.answerC);
        ansD = findViewById(R.id.answerD);
        message = findViewById(R.id.tvMessage);

        dbHelper = new DBHelper(this);
        sqldb = dbHelper.getWritableDatabase();
        Toast.makeText(this,"DB name:"+dbHelper.getDatabaseName(),
                Toast.LENGTH_LONG).show();
        System.out.println("======== \n "+"DB name:"+dbHelper.getDatabaseName());

        long recordsNumber = DatabaseUtils.queryNumEntries(sqldb, dbHelper.TABLE_NAME);
        if (recordsNumber == 0){
            initDB();
        }
        questionGenerate();
    }

    private void initDB(){
        // reading files
        String text = "";
        String[][] table;
        try {
            InputStream is = getAssets().open("data_to_update.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            text = new String(buffer);

            String[] lines = text.split("\n");
            table = new String[lines.length - 1][NCOL];
            for (int i = 1; i < lines.length; i++){
                String[] line = lines[i].split(":");
                for (int j = 0; j < line.length; j++){
                    table[i-1][j] = line[j];
                }
            }
        } catch (IOException ioe){
            table = new String[NROW][NCOL];
            System.out.println("============ opening problem ================");
        }
        // inserting values from files
        ContentValues newRecord = new ContentValues();
        for (int i = 0; i < table.length; i++){
            //newRecord.put(dbHelper._ID, i);
            newRecord.put(dbHelper.COUNTRY, table[i][0]);
            newRecord.put(dbHelper.POPULATION, Integer.parseInt(table[i][2]));
            newRecord.put(dbHelper.CURRENCY, table[i][3]);
            newRecord.put(dbHelper.CAPITAL, table[i][1]);
            sqldb.insert(dbHelper.TABLE_NAME, null, newRecord);
        }
    }

    public void buttonA(View v){
        userAns = 'a';
        submit();
    }
    public void buttonB(View v){
        userAns = 'b';
        submit();
    }
    public void buttonC(View v){
        userAns = 'c';
        submit();
    }
    public void buttonD(View v){
        userAns = 'd';
        submit();
    }
    private void submit(){
        if(userAns == goodAns){
            message.setText("Good!");
            questionGenerate();
        } else {
            message.setText("Wrong! Try again!");
        }
    }
//    public void start(View v){
//        questionGenerate();
//    }

    private void questionGenerate (){
        String[] column = {dbHelper.COUNTRY};
        Cursor c = sqldb.query(dbHelper.TABLE_NAME,
                column,
                null, null, null, null, null);
        LinkedList<String> countries = new LinkedList<>();
        while (c.moveToNext()){
            countries.add(c.getString(0));
        }
        String country = countries.get(rand.nextInt(countries.size()));


//        Integer id = new Integer(rand.nextInt(NROW));
        String selection = dbHelper.COUNTRY+"=?";
        String[] selectionArguments = {country};
        Cursor entries = sqldb.query(dbHelper.TABLE_NAME,
                dbHelper.columns,
                selection,
                selectionArguments,
                null, null, null);
        entries.moveToNext();
        LinkedList<String> answers;
        String goodAnswer = "";
        int x = rand.nextInt(3);
        LinkedList<String> list = new LinkedList<>(countries);
        list.removeFirstOccurrence(country);

        switch (x){
            case 0:
                goodAnswer = entries.getString(entries.getColumnIndex(dbHelper.CAPITAL));
                question.setText("What is capital of "+country+"?");
                answers = randomAnswer(dbHelper.CAPITAL, list);
                answers.add(goodAnswer);

                break;
            case 1:
                goodAnswer = entries.getString(
                        entries.getColumnIndex(dbHelper.POPULATION));
                question.setText("What is population of "+country+"?");
                answers = randomAnswer(dbHelper.POPULATION, list);
                answers.add(goodAnswer);
                break;
            case 2:
                goodAnswer = entries.getString(
                        entries.getColumnIndex(dbHelper.CURRENCY));
                question.setText("What is currency of "+country+"?");
                answers = randomAnswer(dbHelper.CURRENCY, list);
                answers.add(goodAnswer);
                break;
            default:
                answers = new LinkedList<>();
                System.out.println("=========== querying error ================ ");
                break;
        }
        Collections.shuffle(answers);
        int idx = answers.indexOf(goodAnswer);
        ansA.setText(answers.poll());
        ansB.setText(answers.poll());
        ansC.setText(answers.poll());
        ansD.setText(answers.poll());
        switch (idx){
            case 0:
                goodAns = 'a';
                break;
            case 1:
                goodAns = 'b';
                break;
            case 2:
                goodAns = 'c';
                break;
            case 3:
                goodAns = 'd';
                break;
        }
    }

    private LinkedList<String> randomAnswer(String colName, LinkedList<String> countries){
        LinkedList<String> result = new LinkedList<>();

        String[] col = {colName};
        for (int i = 0; i < 3; i++){
            Collections.shuffle(countries);
            String country = countries.pop();
            String[] selectionArguments = {country};
            Cursor c = sqldb.query(dbHelper.TABLE_NAME,
                    col,
                    dbHelper.COUNTRY+"=?",
                    selectionArguments,
                    null, null, null);
            c.moveToNext();
            String tmp = c.getString(0);
            if(result.contains(tmp))
                i--;
            else
                result.add(tmp);
        }
        return result;
    }
}