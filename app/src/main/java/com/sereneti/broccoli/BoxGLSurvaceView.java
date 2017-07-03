package com.sereneti.broccoli;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

import com.koushikdutta.async.http.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by xiuqi on 6/25/17.
 */
public class BoxGLSurvaceView extends GLSurfaceView implements WebSocket.StringCallback{
    private final BoxRenderer mRenderer;

    public BoxGLSurvaceView(Context context) {
        super(context);
        mRenderer = new BoxRenderer();

        setEGLContextClientVersion(2);

        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        //getHolder().setFormat(PixelFormat.TRANSLUCENT);

        setRenderer(mRenderer);

        setZOrderOnTop(true);

        //setZOrderMediaOverlay(true);//May need change to media on top??????
    }

    @Override
    public void onStringAvailable(String s) {
        //TODO Parse data and call requestRender()
        LinkedList<double[]> boxCoords = new LinkedList<>();
        LinkedList<Double> boxCred = new LinkedList<>();
        LinkedList<Integer> boxTag = new LinkedList<>();
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray boxesJsonArray = jsonObject.getJSONArray("boxes");
            JSONArray scoresJsonArray = jsonObject.getJSONArray("scores");
            for(int i = 0;i < boxesJsonArray.length();i++){
                JSONArray boxArray = (JSONArray)boxesJsonArray.get(i);
                JSONArray scoreArray = (JSONArray)scoresJsonArray.get(i);
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
                    double[] coords = {boxArray.getDouble(i*4),boxArray.getDouble(i*4+1),boxArray.getDouble(i*4+2),boxArray.getDouble(i*4+3)};
                    boxCoords.add(coords);
                    boxCred.add(max);
                    boxTag.add(max_index);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //
        requestRender();
    }

    public void renewData(ArrayList<double[]> boxCoords, ArrayList<Double> boxCred, ArrayList<Integer> boxTag) {
        mRenderer.renewData(boxCoords,boxCred,boxTag);
    }
}
