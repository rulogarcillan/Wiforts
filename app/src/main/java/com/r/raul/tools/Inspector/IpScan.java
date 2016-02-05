package com.r.raul.tools.Inspector;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.r.raul.tools.Utils.LogUtils.LOGE;
import static com.r.raul.tools.Utils.LogUtils.LOGI;

public class IpScan {
	private ScanResult scanResult;
	public static final int DEFAULT_TIME_OUT = 1800;
	public static final int DEFAULT_FIXED_POOL = 100;
	public ExecutorService pool;
	private int pt_move = 2; // 1=backward 2=forward

	public IpScan(ScanResult scanResult) {
		this.scanResult = scanResult;
	}

	public void scanAll(ScanRange scanRange) {
		long ip = scanRange.getRouterIp();
		long start = scanRange.getScanStart();
		long end = scanRange.getScanEnd();
		pool = Executors.newFixedThreadPool(DEFAULT_FIXED_POOL);
		if (ip <= end && ip >= start) {
			launch(start);

			long pt_backward = ip;
			long pt_forward = ip + 1;
			long size_hosts = scanRange.size() - 1;

			for (int i = 0; i < size_hosts; i++) {
				// Set pointer if of limits
				if (pt_backward <= start) {
					pt_move = 2;
				} else if (pt_forward > end) {
					pt_move = 1;
				}
				// Move back and forth
				if (pt_move == 1) {
					launch(pt_backward);
					pt_backward--;
					pt_move = 2;
				} else if (pt_move == 2) {
					launch(pt_forward);
					pt_forward++;
					pt_move = 1;
				}
			}
		} else {
			for (long i = start; i <= end; i++) {
				launch(i);
			}
		}
		pool.shutdown();
		try {
			if (!pool.awaitTermination(3600, TimeUnit.SECONDS)) {
				pool.shutdownNow();
			}
		} catch (InterruptedException e) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	public void stop() {
		pool.shutdownNow();
	}

	private void launch(long i) {
		if (!pool.isShutdown()) {
			pool.execute(new SingleRunnable(IpTranslator.getIpFromLongUnsigned(i), scanResult));
		}
	}

	public void scanSingleIp(String ip, int timeout) {
		try {
			final String CMD = "/system/bin/ping -q -n -w 100 -c 1 %s";
			Process myProcess = Runtime.getRuntime().exec(String.format(CMD, ip));
			myProcess.waitFor();
			if (myProcess.exitValue() == 0) {
				LOGI("IP OK PING " + ip);
				scanResult.onActiveIp(ip);
			} else {
				try {
					InetAddress h = InetAddress.getByName(ip);
					if (h.isReachable(timeout)) {
						LOGI("IP OK isReachable 1 " + ip);
						scanResult.onActiveIp(ip);
					} else {
						LOGI("IP KO isReachable 1 " + ip);
						scanResult.onInActiveIp(ip);
					}
				} catch (Exception e) {
					LOGE(e.getMessage());
				}
			}

		} catch (Exception e) {
			LOGE("NO SE PUEDE HACER PING NATIVO");
			try {
				InetAddress h = InetAddress.getByName(ip);
				if (h.isReachable(timeout)) {
					LOGI("IP OK isReachable 2 " + ip);
					scanResult.onActiveIp(ip);
				} else {
					LOGI("IP KO isReachable 2 " + ip);
					scanResult.onInActiveIp(ip);
				}
			} catch (UnknownHostException e2) {
				LOGE(e2.getMessage());
			} catch (IOException e2) {
				LOGE(e2.getMessage());
			}
		}
	}

	private class SingleRunnable implements Runnable {
		private String ip;

		SingleRunnable(String ip, ScanResult scanResult) {
			this.ip = ip;
		}

		@Override
		public void run() {
			scanSingleIp(ip, DEFAULT_TIME_OUT);
		}
	}
}


