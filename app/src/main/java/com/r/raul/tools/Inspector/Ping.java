package com.r.raul.tools.Inspector;
public class Ping {
	
	private String[] ips;
	private static final int NUM_HILOS = 36;
	private ExecutorService pool;

	public Ping(String[] ips) {
	 this.ips= ips;
	}
	
	public void start() {

        pool = Executors.newFixedThreadPool(NUM_HILOS);
        for (String ip : addresses) {
			if (!pool.isShutdown()) {
				pool.execute(new SingleRunnable(i, scanResult));
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
        if (pool != null && !pool.isTerminated()) {
            pool.shutdownNow();
        }
    }
	
	private class SingleRunnable implements Runnable {
        private String ip;

        SingleRunnable(String ip) {
            this.ip = ip;
        }

        @Override
        public void run() {
            scanIp(ip);
        }
    }
	
	private scanIp(String ip){	
	 try {
            final String CMD = "/system/bin/timeout 0.1 ping -c1 %s";
            Process myProcess = Runtime.getRuntime().exec(String.format(CMD, ip));
        }
        } catch (Exception e) {
            LOGE("NO SE PUEDE ESTIMULAR NATIVO");
		}	
	}
}
