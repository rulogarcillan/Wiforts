
package com.r.raul.tools.Utils;
import android.os.Vibrator;

class Utilidades {

static static void lanzaVibracion(Context mContext, int time){
	Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
	v.vibrate(time);
}

}
