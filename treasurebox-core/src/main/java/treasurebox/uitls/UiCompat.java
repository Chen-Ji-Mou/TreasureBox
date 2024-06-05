package treasurebox.uitls;

import android.animation.ValueAnimator;
import android.graphics.Rect;

public class UiCompat
{
    private CompatJBPropertyAnimationCallback mCallback = null;
    private UiCompat(){}
    private static class Holder{
        static UiCompat mInstance = new UiCompat();
    }

    public static UiCompat getInstance(){
        return Holder.mInstance;
    }
    public interface CompatJBPropertyAnimationCallback {
        public void forceRefresh(Rect rect);
    }

    public void setCompatJBPropertyAnimationCallback(CompatJBPropertyAnimationCallback callback){
        mCallback = callback;
    }


    public void destroy(){
        mCallback = null;
    }


    public void forceRefresh(Rect rect){
        if (mCallback != null)
            mCallback.forceRefresh(rect);
    }

    public static class CompatAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener{
        private ValueAnimator.AnimatorUpdateListener listener = null;
        public CompatAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener l){
            listener = l;
        }
        public CompatAnimatorUpdateListener(){}
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            UiCompat.getInstance().forceRefresh(null);
            if (listener != null)
                listener.onAnimationUpdate(animation);
        }
    }
}
