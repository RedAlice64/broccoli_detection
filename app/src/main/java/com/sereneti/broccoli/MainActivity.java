package com.sereneti.broccoli;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.koushikdutta.async.http.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements WebSocket.StringCallback{

    private BoxGLSurvaceView glView;
    private CameraFragmentBase mCameraFragmentBase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        mCameraFragmentBase = CameraFragmentBase.newInstance();

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, mCameraFragmentBase)
                    .commit();
        }

        glView = new BoxGLSurvaceView(this);
        //glView.
        glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        //glView.setRenderer(new BoxRenderer());
        addContentView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStringAvailable(String s) {
        Log.d("websocket","callback");
        //TODO Parse data and call requestRender()
        int[] limits = mCameraFragmentBase.limits();
        ArrayList<double[]> boxCoords = new ArrayList<>();
        ArrayList<Double> boxCred = new ArrayList<>();
        ArrayList<Integer> boxTag = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(s);
            //Log.d("websocket",jsonObject.toString());
            JSONArray boxesJsonArray = jsonObject.getJSONArray("boxes");
            JSONArray scoresJsonArray = jsonObject.getJSONArray("scores");
            for(int i = 0;i < boxesJsonArray.length();i++){
                JSONArray boxArray = (JSONArray)boxesJsonArray.get(i);
                JSONArray scoreArray = (JSONArray)scoresJsonArray.get(i);
                Log.d("websocket",boxArray.toString());
                Log.d("websocket",scoreArray.toString());
                double max = 0.0;
                int max_index = 0;
                for(int j = 0;j < scoreArray.length();j++){
                    double score = scoreArray.getDouble(j);
                    if(score > max){
                        max = score;
                        max_index = j;
                    }
                }
                if(max > 0.8 && max_index != 0){
                    double[] coords = {boxArray.getDouble(i*4)/limits[0],boxArray.getDouble(i*4+1)/limits[1],boxArray.getDouble(i*4+2)/limits[0],boxArray.getDouble(i*4+3)/limits[1]};
                    boxCoords.add(coords);
                    boxCred.add(max);
                    boxTag.add(max_index);
                    Log.d("websocket",Double.toString(coords[0]) + " "+
                            Double.toString(coords[1]) + " "+
                            Double.toString(coords[2]) + " "+
                            Double.toString(coords[3]) + " "+
                            Double.toString(max));
                    Toast.makeText(this,Double.toString(coords[0]) + " "+
                            Double.toString(coords[1]) + " "+
                            Double.toString(coords[2]) + " "+
                            Double.toString(coords[3]) + " "+
                                    Double.toString(max),Toast.LENGTH_LONG).show();




                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        glView.renewData(boxCoords,boxCred,boxTag);

        glView.requestRender();
    }
}
