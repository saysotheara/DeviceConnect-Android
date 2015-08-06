package org.deviceconnect.android.deviceplugin.theta.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import org.deviceconnect.android.deviceplugin.theta.opengl.model.UVSphere;

import org.deviceconnect.android.deviceplugin.theta.utils.Quaternion;
import org.deviceconnect.android.deviceplugin.theta.utils.Vector3D;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Renderer class for photo display.
 */
public class SphereRenderer implements Renderer {

    /** Radius of sphere for photo */
    private static final int TEXTURE_SHELL_RADIUS = 1;
    /** Number of sphere polygon partitions for photo, which must be an even number */
    private static final int SHELL_DIVIDES = 40;

    private final String VSHADER_SRC =
            "attribute vec4 aPosition;\n" +
            "attribute vec2 aUV;\n" +
            "uniform mat4 uProjection;\n" +
            "uniform mat4 uView;\n" +
            "uniform mat4 uModel;\n" +
            "varying vec2 vUV;\n" +
            "void main() {\n" +
            "  gl_Position = uProjection * uView * uModel * aPosition;\n" +
            "  vUV = aUV;\n" +
            "}\n";

    private final String FSHADER_SRC =
            "precision mediump float;\n" +
            "varying vec2 vUV;\n" +
            "uniform sampler2D uTex;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(uTex, vUV);\n" +
            "}\n";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.0f;

    private static final Vector3D DEFAULT_CAMERA_DIRECTION = new Vector3D(1.0f, 0.0f, 0.0f);

    private int mScreenWidth;
    private int mScreenHeight;
    private float mCameraFovDegree = 72;
    private Vector3D mCameraPos = new Vector3D(0.0f, 0.0f, 0.0f);
    private Vector3D mCameraDirection = new Vector3D(1.0f, 0.0f, 0.0f);

    private final UVSphere mShell;

    private Bitmap mTexture;
    private boolean mTextureUpdate = false;
    private int[] mTextures = new int[1];

    private int mPositionHandle;
    private int mProjectionMatrixHandle;
    private int mViewMatrixHandle;
    private int mUVHandle;
    private int mTexHandle;
    private int mModelMatrixHandle;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];

    /**
     * Constructor
     */
    public SphereRenderer() {
        mShell = new UVSphere(TEXTURE_SHELL_RADIUS, SHELL_DIVIDES);
    }


    /**
     * onDrawFrame Method
     * @param gl GLObject (not used)
     */
    //@Override
    public void onDrawFrame(final GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, mScreenWidth, mScreenHeight);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjectionMatrix, 0);

        if (mTextureUpdate && null != mTexture && !mTexture.isRecycled()) {
            loadTexture(mTexture);
            //mTexture.recycle();
            mTextureUpdate = false;
        }

        Matrix.setLookAtM(mViewMatrix, 0, mCameraPos.x(), mCameraPos.y(), mCameraPos.z(), mCameraDirection.x(), mCameraDirection.y(), mCameraDirection.z(), 0.0f, 1.0f, 0.0f);
        Matrix.perspectiveM(mProjectionMatrix, 0, mCameraFovDegree, getScreenAspect(), Z_NEAR, Z_FAR);

        GLES20.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, mProjectionMatrix, 0);
        GLES20.glUniformMatrix4fv(mViewMatrixHandle, 1, false, mViewMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextures[0]);
        GLES20.glUniform1i(mTexHandle,0);

        mShell.draw(mPositionHandle, mUVHandle);
    }

    /**
     * onSurfaceChanged Method
     * @param gl GLObject (not used)
     * @param width Screen width
     * @param height Screen height
     */
    //@Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
    }

    /**
     * onSurfaceCreated Method
     * @param gl GLObject (not used)
     * @param config EGL Setting Object
     */
    //@Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {

        int vShader;
        int fShader;
        int program;

        vShader = loadShader(GLES20.GL_VERTEX_SHADER, VSHADER_SRC);
        fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FSHADER_SRC);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vShader);
        GLES20.glAttachShader(program, fShader);
        GLES20.glLinkProgram(program);

        GLES20.glUseProgram(program);

        mPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        mUVHandle = GLES20.glGetAttribLocation(program, "aUV");
        mProjectionMatrixHandle = GLES20.glGetUniformLocation(program, "uProjection");
        mViewMatrixHandle = GLES20.glGetUniformLocation(program, "uView");
        mTexHandle = GLES20.glGetUniformLocation(program, "uTex");
        mModelMatrixHandle = GLES20.glGetUniformLocation(program, "uModel");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    private float getScreenAspect() {
        return (float) mScreenWidth / (float) (mScreenHeight == 0 ? 1 : mScreenHeight);
    }

    /**
     * Sets the texture for the sphere
     * @param texture Photo object for texture
     */
    public void setTexture(Bitmap texture) {
        mTexture = texture;
        mTextureUpdate = true;
    }

    /**
     * Acquires the set texture
     * @return Photo object for texture
     */
    public Bitmap getTexture() {
        return mTexture;
    }


    /**
     * GL error judgment method for debugging
     * @param TAG TAG output character string
     * @param glOperation Message output character string
     */
    public static void checkGlError(String TAG, String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Texture setting method
     * @param texture Setting texture
     */
    public void loadTexture(final Bitmap texture) {
        GLES20.glGenTextures(1, mTextures, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
    }

    private int loadShader(final int type, final String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public void setScreenWidth(final int screenWidth) {
        mScreenWidth = screenWidth;
    }

    public void setScreenHeight(final int screenHeight) {
        mScreenHeight = screenHeight;
    }

    public void setCameraFovDegree(float cameraFovDegree) {
        mCameraFovDegree = cameraFovDegree;
    }

    public void setCameraPos(final float x, final float y, final float z) {
        mCameraPos = new Vector3D(x, y, z);
    }

    public void setCameraDirectionByEulerAngle(final float x, final float y, final float z) {
        float radianPerDegree = ((float) Math.PI) / 180.0f;
        float radianX = x * radianPerDegree;
        float radianY = y * radianPerDegree;
        float radianZ = z * radianPerDegree;
        mCameraDirection = Quaternion.rotateXYZ(DEFAULT_CAMERA_DIRECTION, radianX, radianY, radianZ);
    }
}