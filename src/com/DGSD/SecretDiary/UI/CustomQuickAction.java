package com.DGSD.SecretDiary.UI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import greendroid.widget.QuickAction;

/**
 * Created by: Daniel Grech
 * Date: 12/11/11
 * Time: 5:35 PM
 */

public class CustomQuickAction extends QuickAction {

        private static final ColorFilter BLACK_CF = new LightingColorFilter(Color.BLACK, Color.BLACK);

        public CustomQuickAction(Context ctx, int drawableId, int titleId) {
            super(ctx, buildDrawable(ctx, drawableId), titleId);
        }

        public CustomQuickAction(Context ctx, int drawableId, String title) {
            super(ctx, buildDrawable(ctx, drawableId), title);
        }

        private static Drawable buildDrawable(Context ctx, int drawableId) {
            Drawable d = ctx.getResources().getDrawable(drawableId);
            d.setColorFilter(BLACK_CF);
            return d;
        }

    }