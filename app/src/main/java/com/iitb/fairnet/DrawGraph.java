package com.iitb.fairnet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawGraph extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder holder;
    private Thread drawThread;
    private boolean surfaceReady = false;
    private boolean drawingActive = false;
    private Paint cPaint = new Paint();
    private static final int MAX_FRAME_TIME = (int) (1000.0 / 60.0);
    private static final String LOGTAG = "surface";
    Globals.mcl_apps_enum[] app_list;
    Globals.mcl_apps_enum test_app;
    private double[][] bth;
    /* +New Algo */
    DisplayResult.twth_info[][] twth;
    int[] ntwth;
    /* +New Algo */
    float xMargin;
    float yMargin;
    float xStop;
    float yPos;
    float ySize;
    float maxTh;

    public void mcl_update_graph_data(Globals.mcl_apps_enum[] app_list,
                                      Globals.mcl_apps_enum test_app,
                                      double[][] bth,
                                      DisplayResult.twth_info[][] twth,
                                      int[] ntwth) {
        this.app_list = app_list;
        this.test_app = test_app;
        this.bth = bth;
        /* +New Algo */
        this.twth = twth;
        this.ntwth = ntwth;
        /* -New Algo */
    }

    public DrawGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // red
        cPaint.setColor(Color.BLACK);
        // smooth edges
        cPaint.setAntiAlias(true);
        cPaint.setStrokeWidth(5);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }

        // resize your UI
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;

        if (drawThread != null) {
            Log.d(LOGTAG, "draw thread still active..");
            drawingActive = false;
            try {
                drawThread.join();
            } catch (InterruptedException e) { // do nothing
            }
        }

        surfaceReady = true;
        startDrawThread();
        Log.d(LOGTAG, "Created");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface is not used anymore - stop the drawing thread
        stopDrawThread();
        // and release the surface
        holder.getSurface().release();

        this.holder = null;
        surfaceReady = false;
        Log.d(LOGTAG, "Destroyed");
    }

    public void stopDrawThread() {
        if (drawThread == null) {
            Log.d(LOGTAG, "DrawThread is null");
            return;
        }
        drawingActive = false;
        while (true) {
            try {
                Log.d(LOGTAG, "Request last frame");
                drawThread.join(5000);
                break;
            } catch (Exception e) {
                Log.e(LOGTAG, "Could not join with draw thread");
            }
        }
        drawThread = null;
    }

    public void startDrawThread() {
        if (surfaceReady && drawThread == null) {
            drawThread = new Thread(this, "Draw thread");
            drawingActive = true;
            drawThread.start();
        }
    }

    private void mcl_plot_app_speed(Globals.mcl_apps_enum app, Canvas canvas, Paint cPaint, float offset, int acount) {
        float xStart = offset;
        float xStop = xStart;
        float yStart = yPos - 2;
        float yStop;
        float yRange = yPos - 2;
        float maxTh = 7;
        cPaint.setStrokeWidth(25);
        String appName = "REFERENCE " + String.format("%d", acount);
        if (app == test_app) {
            cPaint.setColor(Color.BLUE);
            appName = test_app.name();
        }
        else
            cPaint.setColor(Color.RED);
        if (null == RunTest.adownloader)
            return;
        if (null == RunTest.adownloader[app.ordinal()])
            return;
        long ctime = System.currentTimeMillis();//RunTest.adownloader[app.ordinal()].ctime;
        long ptime = RunTest.adownloader[app.ordinal()].ptime;
        long tdiff = ctime - ptime;
        if (ctime > ptime) {
            //long dlen = RunTest.adownloader[app.ordinal()].dlen;
            long dlen = RunTest.adownloader[app.ordinal()].app_data_len;
            float app_th = (float) (dlen * 8.0 / tdiff * 1000) / 1000000;
            yStop = (yStart - app_th * (yRange / maxTh));
            canvas.drawLine(xStart, yStart, xStop, yStop, cPaint);
            if (app == test_app) {
                cPaint.setStrokeWidth(2);
                canvas.drawLine(xMargin, yStop, this.xStop, yStop, cPaint);
            }
            //cPaint.setStrokeWidth(10);
            //cPaint.setTextSize(20);
            cPaint.setStrokeWidth(15);
            cPaint.setTextSize(25);
            cPaint.setColor(Color.BLACK);
            float slen = (float) (appName.length() * 12.5);
            float alen = (float) (slen / 2.0 + 12.5);
            canvas.drawText(appName, xStart - alen, yStart + 40, cPaint);
            canvas.drawText(String.format("%.3f", app_th), xStart - 40, yStart + 80, cPaint);
            canvas.drawText("Mbps", xStart - 40, yStart + 120, cPaint);
        }
    }

    private int mcl_get_num_app() {
        int num_app = 0;
        if (null == app_list)
            return num_app;
        for (int i = 0; i < Globals.mcl_apps_enum.INVALID_APP.ordinal(); i++) {
            if (app_list[i] != Globals.mcl_apps_enum.INVALID_APP)
                num_app++;
        }
        return num_app;
    }

    private void plot_speed(Canvas canvas, Paint cPaint) {
        float xStart = 0;//xMargin;
        float xStop = getWidth();
        float offset = 0;
        int num_app = mcl_get_num_app();
        int soff = (int) (Math.round((xStop) * 1.0 / (num_app + 1.0)));
        int i = 0;
        int acount = 0;

        if (0 == num_app)
            return;

        cPaint.setStrokeWidth(5);
        canvas.drawLine(xStart, yPos, xStop, yPos, cPaint);

        offset += soff;
        //Log.d("Value","soff = "+soff);
        //Log.d("value","Offset = "+offset);
        mcl_plot_app_speed(test_app, canvas, cPaint, offset, acount);

        for (i = 0; i < Globals.mcl_apps_enum.INVALID_APP.ordinal(); i++) {
            if (app_list[i] != Globals.mcl_apps_enum.INVALID_APP && app_list[i] != test_app) {
                offset += soff;
                acount += 1;
                //Log.d("value","Offset = "+offset);
                //mcl_plot_app_speed(app_list[i], canvas, cPaint, offset);
                mcl_plot_app_speed(app_list[i], canvas, cPaint, offset, acount);
            }
        }
        cPaint.setColor(Color.BLACK);
    }

    private void mcl_plot_app_runavg_speed(Canvas canvas,
                                           Paint cPaint,
                                           Globals.mcl_apps_enum capp,
                                           float yStart){
        float maxTh = 7;
        float yRange = ySize; //(float)(getHeight() * 0.50);
        float xPosf = 0;
        float yPosf = 0;
        float xPost = xMargin;
        float yPost = yStart;

        double[] bth = this.bth[capp.ordinal()];
        int l_bth = bth.length;

        if (capp == test_app)
            cPaint.setColor(Color.BLUE);
        else
            cPaint.setColor(Color.RED);
        cPaint.setStrokeWidth(4);

        float xOff = (xStop-xMargin)/l_bth;

        for (int i = 0; i< l_bth; i++)
        {
            if (bth[i] > maxTh)
                bth[i] = maxTh;
            xPosf = xPost;
            yPosf = yPost;
            xPost = (float)(xMargin + i*xOff);
            if (xStop < xPost)
                break;
            yPost = (float)(yStart - bth[i] * (yRange/maxTh) );
            canvas.drawLine(xPosf, yPosf, xPost, yPost, cPaint);
        }
    }

    private void mcl_plot_app_tw_speed(Canvas canvas,
                                       Paint cPaint,
                                       Globals.mcl_apps_enum capp,
                                       float yStart){
        float maxTh = 7;
        float yRange = ySize; //(float)(getHeight() * 0.50);
        float xPosf = 0;
        float yPosf = 0;
        float xPost = xMargin;
        float yPost = yStart;

        DisplayResult.twth_info[] twth = this.twth[capp.ordinal()];
        int l_th = ntwth[capp.ordinal()];

        if (capp == test_app)
            cPaint.setColor(Color.BLUE);
        else
            cPaint.setColor(Color.RED);
        cPaint.setStrokeWidth(4);

        float xOff = (xStop-xMargin)/l_th;

        for (int i = 0; i< l_th; i++)
        {
            if (twth[i].twth > maxTh)
                twth[i].twth = maxTh;
            xPosf = xPost;
            yPosf = yPost;
            xPost = (float)(xMargin + i*xOff);
            if (xStop < xPost)
                break;
            yPost = (float)(yStart - twth[i].twth * (yRange/maxTh) );
            canvas.drawLine(xPosf, yPosf, xPost, yPost, cPaint);
        }
    }

    private  float mcl_draw_axis(Canvas canvas, Paint cPaint){
        float xStart = xMargin;
        float yStart = (float) (yPos - getHeight() * 0.45);
        float yStop = (float) (yStart - ySize); //getHeight() * 0.50);
        /* Draw x-axis */
        cPaint.setColor(Color.BLACK);
        cPaint.setStrokeWidth(4);
        canvas.drawLine(xStart, yStart, xStop, yStart, cPaint);
        canvas.drawLine(xStart, yStart, xStart, yStop, cPaint);
        canvas.drawLine(xStart, yStop, xStop, yStop, cPaint);
        canvas.drawLine(xStop, yStart, xStop, yStop, cPaint);
        cPaint.setTextSize(30);
        canvas.drawText("Time", xStop-90, yStart+35, cPaint);
        return yStart;
    }

    private void mcl_draw_grid(Canvas canvas, Paint cPaint, float yStart){
        float xStart = xMargin;
        float yStop = (float) (yStart - ySize);//getHeight() * 0.50);
        float xOff = (xStop - xStart)/10;
        float yOff = (yStop - yStart)/maxTh;
        cPaint.setColor(Color.LTGRAY);
        cPaint.setStrokeWidth(2);
        cPaint.setTextSize(20);

        for (int i = 0; i < maxTh; i++){
            cPaint.setColor(Color.LTGRAY);
            cPaint.setStrokeWidth(2);
            canvas.drawLine(xStart, yStart+(i*yOff), xStop, yStart+(i*yOff), cPaint);
            cPaint.setColor(Color.BLACK);
            cPaint.setStrokeWidth(10);
            canvas.drawText(Integer.toString(i), xStart-30, yStart+(i*yOff), cPaint);
        }
        cPaint.setColor(Color.LTGRAY);
        cPaint.setStrokeWidth(2);
        for (int i = 0; i < 10; i++){
            canvas.drawLine(xStart+(i*xOff), yStart, xStart+(i*xOff), yStop, cPaint);
        }
    }

    private void plot_runavg_speed(Canvas canvas, Paint cPaint) {
        boolean draw_ax = true;
        float yStart = 0;
        if (null != bth ) {
            for (int i = 0; i < Globals.mcl_apps_enum.INVALID_APP.ordinal(); i++) {
                Globals.mcl_apps_enum capp = app_list[i];
                if (capp != Globals.mcl_apps_enum.INVALID_APP) {
                    if (null != bth[capp.ordinal()]) {
                        if (draw_ax) {
                            yStart = mcl_draw_axis(canvas, cPaint);
                            mcl_draw_grid(canvas, cPaint, yStart);
                            draw_ax = false;
                        }
                        mcl_plot_app_runavg_speed(canvas, cPaint, capp, yStart);
                    }
                }
            }
        }
        else
            canvas.drawColor(Color.BLUE);
    }


    private void mcl_show_legends(Canvas canvas, Paint cPaint, float xStart, float yStart, String capp) {
        int ocol = cPaint.getColor();
        float tsize = cPaint.getTextSize();
        int stsize = 40;
        if (capp.equals(test_app.name())) {
            cPaint.setColor(Color.BLUE);
            //Log.d("Status","Test app");
        }
        else {
            cPaint.setColor(Color.RED);
            //Log.d("Status","Other app");
        }
        cPaint.setTextSize(stsize);
        canvas.drawText(capp, xStart, yStart+stsize-5, cPaint);
        cPaint.setColor(ocol);
        cPaint.setTextSize(tsize);
    }


    private void plot_tw_speed(Canvas canvas, Paint cPaint) {
        boolean draw_ax = true;
        float yStart = 0;
        xMargin = 60;
        int acount = 0;
        if (null != twth ) {
            for (int i = 0; i < Globals.mcl_apps_enum.INVALID_APP.ordinal(); i++) {
                Globals.mcl_apps_enum capp = app_list[i];
                if (capp != Globals.mcl_apps_enum.INVALID_APP) {
                    if (null != twth[capp.ordinal()]) {
                        if (draw_ax) {
                            cPaint.setStrokeWidth(10);
                            cPaint.setTextSize(30);
                            cPaint.setColor(Color.BLACK);
                            float xpos = Math.round((xStop - xMargin)/2);
                            canvas.drawText("Speed (in Mbps)", xpos-100, yStart + 25, cPaint);
                            cPaint.setStrokeWidth(5);
                            yStart = mcl_draw_axis(canvas, cPaint);
                            mcl_draw_grid(canvas, cPaint, yStart);
                            draw_ax = false;
                        }
                        mcl_plot_app_tw_speed(canvas, cPaint, capp, yStart);
                        acount++;
                    }
                }
            }
            mcl_show_legends(canvas, cPaint, xMargin, yStart, test_app.name());
        }
        else
            canvas.drawColor(Color.BLUE);
        xMargin = 10;
    }

    @Override
    public void run()
    {
        Log.d(LOGTAG, "Draw thread started");
        long frameStartTime;
        long frameTime;
        xMargin = 10;//(float)(getWidth()*0.05);
        yMargin = (float)(getHeight()*0.25);
        xStop = getWidth() - xMargin;
        yPos = getHeight()-yMargin;
        ySize = (float)(getHeight() * 0.25);
        maxTh = 7;

        //try
        //{
            while (drawingActive)
            {
                if (holder == null)
                {
                    return;
                }

                frameStartTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                if (canvas != null)
                {
                    canvas.drawColor(Color.WHITE);
                    try
                    {
                        //if (null != bth)
                        //    plot_runavg_speed(canvas,cPaint);
                        //else
                        if (null != twth)
                            plot_tw_speed(canvas,cPaint);
                        else
                            plot_speed(canvas,cPaint);
                    } finally
                    {
                        try
                        {
                            holder.unlockCanvasAndPost(canvas);
                        } catch (Exception e)
                        {
                            // ignore
                        }
                    }
                }
                // calculate the time required to draw the frame in ms
                frameTime = (System.nanoTime() - frameStartTime) / 1000000;

                if (frameTime < MAX_FRAME_TIME) // faster than the max fps - limit the FPS
                {
                    try
                    {
                        if (null == twth)
                            Thread.sleep(1000);
                        else
                            Thread.sleep(2000);
                    } catch (InterruptedException e)
                    {
                        // ignore
                    }
                }
            }
        //} catch (Exception e)
        //{
        //    Log.w(LOGTAG, "Exception while locking/unlocking");
        //    //startDrawThread();
        //}
        Log.d(LOGTAG, "Draw thread finished" + drawingActive + drawThread);
        //startDrawThread();
    }
}
