package com.spartacusrex.spartacuside;

import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class LearnGestureListener extends GestureDetector.SimpleOnGestureListener {
	
	
    @Override
    public boolean onSingleTapUp(MotionEvent ev) {
//        Log.d("DEBUG","onSingleTapUp true");
//        Term.uuup = true;
//        Log.d("DEBUG","save ing2");
//        Log.d("DEBUG","save end");
//        Term.save = false;
        return true;
    }

    @Override
    public void onShowPress(MotionEvent ev) {
//        Log.d("DEBUG","onShowPress");
    }

    @Override
    public void onLongPress(MotionEvent ev) {
//        Log.d("DEBUG","onLongPress");
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
            float distanceX, float distanceY) {
//        Log.d("DEBUG","onScroll");
        return true;
    }

    @Override
    public boolean onDown(MotionEvent ev) {
    	
        Log.d("DEBUG","onDownd true");
//        Term.down_tmp = false;
        Term.down_tmp = true;
//        if(down_tmp == false){
//        	down_tmp = true;
//        }
        
//        Term.dddown = true;
//        Log.d("DEBUG","save ing");
//        Term.save = true;
////        Log.d("DEBUG","onDownd false");
//        if(Term.uuup == true && Term.dddown == true){
//        	Term.uuup = false;
//            Term.dddown = false;
//        }
        
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
//        Log.d("DEBUG","onFling");
        return true;
    }
    public boolean onDoubleTap(MotionEvent event){
//        Log.d("DEBUG","onDoubleTap");
        return true;
    }
}

