package com.example.com.passwordview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by administer on 2017/8/30.
 */


public class PswView extends View {

    private OnInputListener onInputListener;


    //尺寸
    private int viewWidth;
    private int viewHeight;
    private int borderWidth;
    private int spaceWidth;
    private int paddingTop = 10;

    //内容规格
    private int textSize;
    private int initBorderColor;
    private int inputBorderColor;
    private int dotRadius;

    //数据
    private int pswlength ;
    private List<Integer> result = new ArrayList<>();
    private int savedCount;

    //是否延时绘制
    private boolean invalidated = false;

    //绘制画笔
    //绘制画笔
    private Paint dotPaint ;
    private Paint textPaint;
    private Paint initborderPaint;
    private Paint inputBorderPaint;

    private InputMethodManager inputMethodManager;

    //输入监听,完成输入时执行
    interface OnInputListener{
        void OnInput ( String password );
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    invalidated = true;
                    invalidate();
                    break;
            }
        }
    };


    public PswView (Context context){super(context);}
    public PswView (Context context, @Nullable AttributeSet attrs){
        this(context,attrs,0);
    }
    public PswView (Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super( context, attrs,defStyleAttr);
        initView();
    }


    public void setOnInputListener (OnInputListener onInputListener){
        this.onInputListener = onInputListener;
    }


    private void initView(){
        this.setOnKeyListener(new NumberKeyListener());


        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        viewWidth = getWidth();
        viewHeight = getWidth();

        borderWidth = 4*viewWidth/(5*pswlength-1);
        spaceWidth = borderWidth/4;

        textSize =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,18,getResources().getDisplayMetrics());
        dotRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        initBorderColor = Color.parseColor( "#FF000000" ) ;
        inputBorderColor = Color.parseColor("#3779e3");

        pswlength =6;

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.STROKE);
        dotPaint.setStrokeCap(Paint.Cap.ROUND);
        dotPaint.setStrokeWidth(dotRadius);
        dotPaint.setColor(inputBorderColor);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);
        textPaint.setColor(inputBorderColor);

        initborderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        initborderPaint.setStyle(Paint.Style.STROKE);
        initborderPaint.setStrokeWidth(3);//才能看到效果
        initborderPaint.setColor(initBorderColor);

        inputBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        inputBorderPaint.setStyle(Paint.Style.STROKE);
        inputBorderPaint.setStrokeWidth(3);
        inputBorderPaint.setColor(inputBorderColor);



        savedCount = result.size();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpec = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpec = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


        if (widthSpec == MeasureSpec.EXACTLY ){

            //高度已知，宽度未知
            if ( heightSpec == MeasureSpec.AT_MOST){
                borderWidth = 4*widthSize/(5*pswlength);
                spaceWidth = borderWidth/4;
                heightSize = 4*widthSize/(5*pswlength)+ paddingTop;
            }
        }

        viewWidth = widthSize;
        viewHeight = heightSize;

        setMeasuredDimension(widthSize,heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        dotRadius = borderWidth / 3;//密码圆点为边框宽度的六分之一
        dotPaint.setStrokeWidth(dotRadius);
		/*
		* 如果明文密码字体大小为默认大小，则取边框宽度的八分之一，否则用自定义大小
		* */
        if (textSize == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics())) {
            textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, borderWidth / 8, getResources().getDisplayMetrics());
        }
        textPaint.setTextSize(textSize);

        //绘制初始边框
        drawInitBorder( canvas );

        //绘制输入密码
        drawPsw(canvas);


        savedCount = result.size();


    }

    /**
     * 绘制输入密码
     * @param canvas
     */
    private void drawPsw (Canvas canvas){
        for (int i=0;i<result.size();i++){

            //强制重绘时直接画圆点
            if ( !invalidated && savedCount == result.size() ){
                    drawInputBorder(canvas,i);
                    float dotX = (int) ( 0.5*spaceWidth+ (i)*( borderWidth + spaceWidth ) ) + borderWidth/2;
                    float dotY = paddingTop + borderWidth/2;
                    canvas.drawPoint(dotX,dotY,dotPaint);
                   continue;
            }

            if (invalidated){

                drawDelayDot(canvas);
                return;
            }

            drawInputBorder(canvas,i);

            //绘制明文密码
            Rect textBound = new Rect();
            textPaint.getTextBounds(result.get(i).toString(),0,1,textBound);
            float textBoundWidth = textPaint.measureText(result.get(i).toString(),0,1);

            float x = (int) ( 0.5*spaceWidth+i*( borderWidth + spaceWidth ))  +
                    ( borderWidth/2-textBoundWidth/2 ) ;
            float y = paddingTop + borderWidth - ( borderWidth/2 - textBound.height()/2 );
            canvas.drawText(result.get(i).toString(),0,1,x,y,textPaint);

            //当i>=1时，绘制输入点后的圆点密码
            if (i>0){
                float dotX = (int) ( 0.5*spaceWidth+ (i-1 )*( borderWidth + spaceWidth ) ) + borderWidth/2;
                float dotY = paddingTop + borderWidth/2;

                canvas.drawPoint(dotX,dotY,dotPaint);
            }

            //判断是否绘制延迟圆点
            if (i+1 == result.size()){
                handler.sendEmptyMessageDelayed(1,1000);
            }
        }
    }

    /**
     * 绘制延时圆点
     * @param canvas
     */
    private void drawDelayDot(Canvas canvas){
        invalidated = false;

        for (int i=0;i<result.size();i++){

            drawInputBorder(canvas,i);

            float dotX = (int) ( 0.5*spaceWidth+ (i)*( borderWidth + spaceWidth ) ) + borderWidth/2;
            float dotY = paddingTop + borderWidth/2;

            canvas.drawPoint(dotX,dotY,dotPaint);


        }

        handler.removeMessages(1);//移除后续的重绘制延时圆点
    }

    /**
     * 绘制输入时边框
     * @param canvas
     */
    @TargetApi(21)
    private void drawInputBorder(Canvas canvas,int i){

            int left =(int) ( 0.5*spaceWidth+i*( borderWidth + spaceWidth ) );
            int top = paddingTop;
            int right = left + borderWidth;
            int bottom = top + borderWidth;

            canvas.drawRoundRect(left,top,right,bottom,10,10,inputBorderPaint);

    }

    /**
     * 绘制初始边框
     */
    @TargetApi(21)
    private void drawInitBorder(Canvas canvas){
        for (int i=0;i<pswlength;i++){
            int left =(int) ( 0.5*spaceWidth+i*( borderWidth + spaceWidth ) );
            int top = paddingTop;
            int right = left + borderWidth;
            int bottom = top + borderWidth;

            canvas.drawRoundRect(left,top,right,bottom,10,10,initborderPaint);
        }
    }


    class NumberKeyListener implements OnKeyListener{
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN){
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {//只处理数字
                    if (result.size() < pswlength) {
                        result.add(keyCode - 7);
                        invalidate();
                        finishInput();
                    }
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (!result.isEmpty()) {//不为空时，删除最后一个数字
                        result.remove(result.size() - 1);
                        invalidate();
                    }
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    finishInput();
                    return true;
                }
            }

            return false;
        }
    }


    /**
     * 输入完成
     */
    private void finishInput(){
        if (result.size() == pswlength){
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(),0);
            Toast.makeText(getContext(),""+result,Toast.LENGTH_SHORT).show();

            if (onInputListener != null){
                StringBuilder builder = new StringBuilder();
                for (int i:result){
                    builder.append(i);
                }
                onInputListener.OnInput(builder.toString());
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {//点击弹出键盘
            requestFocus();
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus){
            inputMethodManager.hideSoftInputFromWindow(this.getWindowToken(),0);
        }
    }

    //IME enable


    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {

        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;//只允许输入数字，同EditText的inputType属性
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;//完成文本图标

        return new NumberInputConnection(this,false);
    }

    class NumberInputConnection extends BaseInputConnection{

        public NumberInputConnection(View targetView, boolean fullEditor){
            super( targetView,  fullEditor);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {

            if (beforeLength == 1 && afterLength == 0) {
                super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                return true;
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }


}
