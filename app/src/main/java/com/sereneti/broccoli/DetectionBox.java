package com.sereneti.broccoli;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by xiuqi on 6/25/17.
 */
public class DetectionBox {

    private String tag;

    private FloatBuffer boxVertexBuffer;
    private ShortBuffer boxDrawListBuffer;

    private static GLText glText;

    private static final String[] tags= {"background","broccoli","chicken","beef","potato"};

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private static final int COORDS_PER_VERTEX = 4;

    private static final int vertexCount = 4;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex


    private short boxDrawOrder[] = {0,1,2,0,2,3};

    static float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f,  0.5f, 0.0f }; // top right


    private final float[] coords;
    private final float cred;


    private int mProgram=-1;

    private int mPositionHandle;
    private int mColorHandle;

    public DetectionBox(double[] ranges, double _cred, Integer _tag){
        // initialize vertex byte buffer for box coordinates
        coords = new float[]{(float)ranges[0],(float)ranges[1],0.0f,(float)ranges[0],(float)ranges[3],0.0f, (float)ranges[2],(float)ranges[3], 0.0f, (float)ranges[2],(float)ranges[1],0.0f};
        cred = (float)_cred;
        tag = tags[_tag];

        ByteBuffer bBoxBuffer = ByteBuffer.allocateDirect(16);
        bBoxBuffer.order(ByteOrder.nativeOrder());
        boxVertexBuffer = bBoxBuffer.asFloatBuffer();
        //boxVertexBuffer.put(coords);
        boxVertexBuffer.put(squareCoords);
        boxVertexBuffer.position(0);

        // initialize byte buffer for box draw list
        ByteBuffer bDrawListBuffer = ByteBuffer.allocateDirect(12);
        bDrawListBuffer.order(ByteOrder.nativeOrder());
        boxDrawListBuffer = bDrawListBuffer.asShortBuffer();
        boxDrawListBuffer.put(boxDrawOrder);
        boxDrawListBuffer.position(0);

        int vertexShader = BoxRenderer.loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader = BoxRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram,vertexShader);

        GLES20.glAttachShader(mProgram,fragmentShader);

        GLES20.glLinkProgram(mProgram);



    }

    public void draw(float[] mvpMatrix) {
        GLES20.glClearColor(1f,1f,1f,1f);

        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);


        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, boxVertexBuffer);

        mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);

        GLES20.glDisableVertexAttribArray(mPositionHandle);

        //glText.begin(1.0f, 1.0f, 1.0f, 1.0f, mvpMatrix);

        //glText.draw(tag + cred,coords[0],coords[1]);

        //glText.end();
    }

}
