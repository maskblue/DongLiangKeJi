package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LineView extends View{
	private Paint paint;
	
	public LineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setColor(Color.BLUE);
	}

	private static final int NUM = 3;
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int ph = (getHeight() - 3) / NUM;
		int pw = (getWidth() -3) / NUM;
		for (int i = 0; i < NUM + 1; i++) {
			canvas.drawLine(0, i * ph, getWidth(), i * ph, paint);
		}
		
		for (int i = 0; i < NUM + 1; i++) {
			canvas.drawLine(i * pw, 0, i * pw, getHeight(), paint);
		}
	}
	
	
}
