package com.adreesulhassan.puretasbeeh.ui.sky;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Lightweight sky strip: soft arc path + sun/moon markers with gentle pulse.
 * Uses a single ValueAnimator (no heavy particle systems).
 */
public class AnimatedSkyView extends View {

    private final Paint skyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arcPath = new Path();
    private final RectF arcBounds = new RectF();

    private float sunProgress = 0.35f;
    private float moonProgress = 0.7f;
    private boolean daytime = true;
    private float pulse = 0f;

    @Nullable
    private ValueAnimator pulseAnimator;

    public AnimatedSkyView(Context context) {
        super(context);
        init();
    }

    public AnimatedSkyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedSkyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(dp(1.5f));
        arcPaint.setColor(0x668B6B52);
        sunPaint.setColor(0xFFC9783A);
        moonPaint.setColor(0xFF6B5B4A);
        setWillNotDraw(false);
    }

    public void setSkyState(float sunProgress, float moonProgress, boolean daytime) {
        this.sunProgress = clamp01(sunProgress);
        this.moonProgress = clamp01(moonProgress);
        this.daytime = daytime;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startPulse();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopPulse();
        super.onDetachedFromWindow();
    }

    private void startPulse() {
        stopPulse();
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(2600L);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new LinearInterpolator());
        pulseAnimator.addUpdateListener(a -> {
            pulse = (float) a.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    private void stopPulse() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        skyPaint.setShader(new LinearGradient(
                0, 0, 0, h,
                daytime ? 0xFFE8D5C4 : 0xFF8B6B52,
                daytime ? 0xFFC49A7C : 0xFF5C4033,
                Shader.TileMode.CLAMP));
        float pad = dp(10);
        arcBounds.set(pad, pad, w - pad, h * 1.55f);
        arcPath.reset();
        arcPath.addArc(arcBounds, 200f, 140f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        // Refresh sky gradient for day/night
        skyPaint.setShader(new LinearGradient(
                0, 0, 0, h,
                daytime ? 0xFFE8D5C4 : 0xFF7A5C4A,
                daytime ? 0xFFB88968 : 0xFF3D2A20,
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(0, 0, w, h, dp(14), dp(14), skyPaint);
        canvas.drawPath(arcPath, arcPaint);

        // Horizon line
        Paint horizon = new Paint(Paint.ANTI_ALIAS_FLAG);
        horizon.setColor(0x55FFFFFF);
        horizon.setStrokeWidth(dp(1));
        canvas.drawLine(dp(8), h - dp(10), w - dp(8), h - dp(10), horizon);

        float[] sun = pointOnArc(sunProgress);
        float[] moon = pointOnArc(moonProgress);

        float glow = dp(10) + pulse * dp(4);
        if (daytime) {
            glowPaint.setShader(new RadialGradient(
                    sun[0], sun[1], glow + dp(8),
                    0x66FFD27A, 0x00FFD27A, Shader.TileMode.CLAMP));
            canvas.drawCircle(sun[0], sun[1], glow + dp(8), glowPaint);
            canvas.drawCircle(sun[0], sun[1], dp(11) + pulse * dp(1.5f), sunPaint);
            // Soft moon ghost
            moonPaint.setAlpha(90);
            canvas.drawCircle(moon[0], moon[1], dp(8), moonPaint);
            moonPaint.setAlpha(255);
        } else {
            glowPaint.setShader(new RadialGradient(
                    moon[0], moon[1], glow + dp(6),
                    0x55E8D5C4, 0x00E8D5C4, Shader.TileMode.CLAMP));
            canvas.drawCircle(moon[0], moon[1], glow + dp(6), glowPaint);
            canvas.drawCircle(moon[0], moon[1], dp(10) + pulse * dp(1.2f), moonPaint);
            // Crescent cut
            Paint cut = new Paint(Paint.ANTI_ALIAS_FLAG);
            cut.setColor(daytime ? 0xFFB88968 : 0xFF3D2A20);
            canvas.drawCircle(moon[0] + dp(3.5f), moon[1] - dp(1.5f), dp(8), cut);
            sunPaint.setAlpha(70);
            canvas.drawCircle(sun[0], sun[1], dp(7), sunPaint);
            sunPaint.setAlpha(255);
        }
    }

    private float[] pointOnArc(float progress) {
        // Map 0..1 along upper semicircle-ish path
        float angle = 200f + 140f * clamp01(progress);
        double rad = Math.toRadians(angle);
        float cx = arcBounds.centerX();
        float cy = arcBounds.centerY();
        float rx = arcBounds.width() / 2f;
        float ry = arcBounds.height() / 2f;
        return new float[]{
                cx + (float) (rx * Math.cos(rad)),
                cy + (float) (ry * Math.sin(rad))
        };
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
