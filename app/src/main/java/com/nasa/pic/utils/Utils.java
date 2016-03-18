package com.nasa.pic.utils;


import java.math.BigDecimal;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chopping.application.LL;
import com.chopping.utils.DeviceUtils;
import com.chopping.utils.DeviceUtils.ScreenSize;
import com.nasa.pic.R;
import com.nasa.pic.app.App;

public final class Utils {
	public static void buildListView(Context cxt,  RecyclerView recyclerView) {
		ScreenSize screenSize = DeviceUtils.getScreenSize(App.Instance);
		LL.d("Screen width: " + screenSize.Width);
		float basic = cxt.getResources().getDimension(R.dimen.basic_card_width);
		LL.d("Basic: " + basic);
		float div = screenSize.Width / basic;
		LL.d("Div: " + div);
		BigDecimal cardCount = new BigDecimal(div).setScale(0, BigDecimal.ROUND_HALF_UP);
		LL.d("CardCount: " + cardCount);
		recyclerView.setLayoutManager(new GridLayoutManager(cxt, cardCount.intValue()));
	}
}
