package com.r.raul.tools.Utils;

import android.os.Vibrator;

class Utilidades {

	public static void lanzaVibracion(Context mContext, int time) {
		Vibrator v = (Vibrator) mContext
				.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(time);
	}

	public static Future<Puerto> portIsOpen(final ExecutorService es, final String ip, final int puertoTratar, final int timeOut) {

		return es.submit(new Callable<Puerto>() {
			@Override
			public Puerto call() {
				// tratamiento
				try {
					Socket socket = new Socket();
					if (timeOut == 0) {
						socket.connect(new InetSocketAddress(ip, puertoTratar));
					} else {
						socket.connect(new InetSocketAddress(ip, puertoTratar),timeOut);
					}
					socket.close();
					LogUtils.LOGE("Puerto: " + puertoTratar + " Abierto");
					return new Puerto(puertoTratar, 0);
				} catch (SocketTimeoutException exTime) {
					LogUtils.LOGE("Puerto: " + puertoTratar + " TimeOut");
					return new Puerto(puertoTratar, 2);
				} catch (Exception ex) {
					LogUtils.LOGE("Puerto: " + puertoTratar + " Cerrado");
					return new Puerto(puertoTratar, 1);
				}

			}
		});

	}
}
