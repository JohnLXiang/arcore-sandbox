package com.google.ar.core.examples.java.helloar.rendering;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.ar.core.examples.java.helloar.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TriangleRenderer {
    private static final String TAG = TriangleRenderer.class.getSimpleName();

    private int mTriangleProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private FloatBuffer vertexBuffer;
    private static final int COORDS_PER_VERTEX = 3;
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    private float[] mModelMatrix = new float[16];
    private float[] mModelViewMatrix = new float[16];
    private float[] mModelViewProjectionMatrix = new float[16];

    public void createOnGlThread(Context context) {
        int vertexShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, R.raw.triangle_vertex);
        int fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER,
                R.raw.triangle_fragment);

        mTriangleProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mTriangleProgram, vertexShader);
        GLES20.glAttachShader(mTriangleProgram, fragmentShader);
        GLES20.glLinkProgram(mTriangleProgram);

        ShaderUtil.checkGLError(TAG, "Program creation");

        mPositionHandle = GLES20.glGetAttribLocation(mTriangleProgram, "vPosition");
        mColorHandle = GLES20.glGetUniformLocation(mTriangleProgram, "vColor");

        ShaderUtil.checkGLError(TAG, "Program parameters");

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    public void updateModelMatrix(float[] modelMatrix, float scaleFactor) {
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        scaleMatrix[0] = scaleFactor;
        scaleMatrix[5] = scaleFactor;
        scaleMatrix[10] = scaleFactor;
        Matrix.multiplyMM(mModelMatrix, 0, modelMatrix, 0, scaleMatrix, 0);
    }

    public void draw(float[] cameraView, float[] projection) {
        ShaderUtil.checkGLError(TAG, "Before draw");
        GLES20.glUseProgram(mTriangleProgram);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mTriangleProgram, "uMVPMatrix");

        Matrix.multiplyMM(mModelViewMatrix, 0, cameraView, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelViewProjectionMatrix, 0, projection, 0, mModelViewMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mModelViewProjectionMatrix, 0);

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        ShaderUtil.checkGLError(TAG, "Draw");

    }

    public static float triangleCoords[] = {   // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };
}
