package com.r.raul.tools.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.r.raul.tools.DB.Consultas;
import com.r.raul.tools.Inspector.InspectorTable;
import com.r.raul.tools.Inspector.Machine;
import com.r.raul.tools.R;
import com.r.raul.tools.Utils2.IpScan;
import com.r.raul.tools.Utils2.Port;
import com.r.raul.tools.Utils2.PortScan;
import com.r.raul.tools.Utils2.PortScanCallback;
import com.r.raul.tools.Utils2.ScanRange;
import com.r.raul.tools.Utils2.ScanResult;

import org.apache.commons.net.util.SubnetUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jcifs.Config;
import jcifs.netbios.NbtAddress;

import static com.r.raul.tools.Utils.LogUtils.LOGE;

public class ObtenMaquinas extends AsyncTask<Void, Integer, Void> {

	

	private Activity ac;
	private Connectivity con;
	private ArrayList<Machine> array;

	private Consultas consultas;

	private ArrayList<InspectorTable> arrayInspectorTable;

	private String macPadre;
	private String gateway;
	private String loacalIp;
	private String macMyDevice;
	
	private int tot = 0; // total ips analizadas para barra de progreso
	private float totalMachine;

	public ObtenMaquinas(Activity ac, ArrayList<Machine> array) {
		this.ac = ac;
		this.array = array;
		this.consultas = new Consultas(ac);
	}

	private int calculoPercent(int valor, float tot) {
		return (valor * 100 / tot);
	}

	@Override
	protected Void doInBackground(Void... params) {

		WifiManager wifiManager = (WifiManager) ac.getSystemService(Context.WIFI_SERVICE);
		macPadre = wifiManager.getConnectionInfo().getBSSID();
		macMyDevice = wifiManager.getConnectionInfo().getMacAddress();
		gateway = con.parseIP(wifiManager.getDhcpInfo().gateway);
		loacalIp = con.getLocalAddress().getHostAddress();
		// consultamos las conexiones guardadas para la mac padre
		arrayInspectorTable = consultas.getAllInspectorTableFromMacPadre(macPadre);
		String prefix = "";
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
		try {
			InetAddress inetAddress = InetAddress.getByName(con.parseIP(dhcpInfo.ipAddress));
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
			for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
				prefix = String.valueOf(address.getNetworkPrefixLength());
			}
		} catch (IOException e) {

		}

		SubnetUtils utils = new SubnetUtils(gateway + "/" + prefix);
		totalMachine = utils.getInfo().getAddressCountLong();

		ScanRange scanRange = null;
		try {
			scanRange = new ScanRange(gateway, utils.getInfo().getNetmask());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		////////////////////////**********************INICIO ESCANEO*********************////////////////////////
		IpScan ipScan = new IpScan(new ScanResult() {

			@Override
			public void onActiveIp(String ip) {
				// TODO Auto-generated method stub
				encontrada(ip);
				publishProgress(calculoPercent(tot++, totalMachine));
			}

			@Override
			public void onInActiveIp(String ip) {
				// TODO Auto-generated method stub
				PortScan portScan = new PortScan(new ScanResult() {

					@Override
					public void onActiveIp(String ip2) {
						encontrada(ip);
						publishProgress(calculoPercent(tot++, totalMachine));
					}

					@Override
					public void onInActiveIp(String ip2) {
						publishProgress(calculoPercent(tot++, totalMachine));
					}
				});
				try {
					portScan.start(ip);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
		ipScan.scanAll(scanRange);

		/*
		 * for (String ip : addresses) { InetAddress ipA = null; try { ipA =
		 * InetAddress.getByName(ip); } catch (UnknownHostException e) {
		 * e.printStackTrace(); } if (!isCancelled()) {
		 * futures.add(Utilidades.machineExist((ExecutorService)
		 * THREAD_POOL_EXECUTOR, ipA, TIME_UP)); } else {
		 * 
		 * } }
		 * 
		 * 
		 * 
		 * int tot = 0; //total ips analizadas para barra de progreso for (final
		 * Future<Machine> f : futures) { tot++; if (!isCancelled()) { try { if
		 * (f.get().isConectado()) {
		 * 
		 * Boolean isGateway = false; Boolean isInBBDD = false; Boolean
		 * isMyDevice = false;
		 * 
		 * LogUtils.LOGI(f.get().getIp());
		 * 
		 * f.get().setMacPadre(macPadre); //padre
		 * f.get().setMac(getMacFromArpCache(f.get().getIp())); //propia o hija
		 * 
		 * if (f.get().getIp().equals(gateway)) { isGateway = true;
		 * f.get().setTipoImg(Constantes.TIPE_GATEWAY); } else if
		 * (f.get().getIp().equals(loacalIp)) { isMyDevice = true;
		 * f.get().setTipoImg(Constantes.TIPE_DEVICE);
		 * f.get().setMac(macMyDevice); //propia o hija } else {
		 * f.get().setTipoImg(Constantes.TIPE_OTHERS); }
		 * 
		 * publishProgress(calculoPercent(tot, addresses.length)); for
		 * (InspectorTable item : arrayInspectorTable) { if
		 * (f.get().getMac().equals(item.getMacdevice())) { isInBBDD = true;
		 * f.get().setNombre(item.getNombre());
		 * f.get().setConocido(item.getFavorito()); break; } } if (!isInBBDD) {
		 * InspectorTable itemIns = new InspectorTable(f.get().getMac(),
		 * macPadre, "", (isGateway || isMyDevice) ? true : false);
		 * consultas.setItemInspectorTable(itemIns);
		 * arrayInspectorTable.add(itemIns); f.get().setNombre("");
		 * f.get().setConocido((isGateway || isMyDevice) ? true : false); }
		 * 
		 * //agregamos el nombre del hardware NbtAddress[] nbts = new
		 * NbtAddress[0]; try { publishProgress(calculoPercent(tot,
		 * addresses.length)); Config.setProperty("jcifs.smb.client.soTimeout",
		 * "100"); Config.setProperty("jcifs.smb.client.responseTimeout",
		 * "100"); Config.setProperty("jcifs.netbios.soTimeout", "100");
		 * Config.setProperty("jcifs.netbios.retryTimeout", "100");
		 * 
		 * nbts = NbtAddress.getAllByAddress(f.get().getIp()); String
		 * netbiosname = nbts[0].getHostName();
		 * 
		 * f.get().setNombre(netbiosname); } catch (UnknownHostException e) {
		 * f.get().setNombre("-"); if (isMyDevice) {
		 * f.get().setNombre(ac.getString(R.string.midevice)); }
		 * e.printStackTrace(); }
		 * 
		 * f.get().setNombreSoft(consultas.getNameFromMac(f.get().getMac()));
		 * 
		 * array.add(f.get()); publishProgress(calculoPercent(tot,
		 * addresses.length)); }
		 * 
		 * } catch (InterruptedException e) { LogUtils.LOGI(e.getMessage()); }
		 * catch (ExecutionException e) { LOGE(e.getMessage()); }
		 * publishProgress(calculoPercent(tot, addresses.length)); } else { //
		 * es.shutdownNow(); } }
		 */

		return null;
	}

	private void encontrada(String ip) {

		Machine item = new Machine();
		item.setIp(ip);
		item.setConectado(true);

		Boolean isGateway = false;
		Boolean isInBBDD = false;
		Boolean isMyDevice = false;

		LogUtils.LOGI(item.getIp());

		item.setMacPadre(macPadre); // padre
		item.setMac(getMacFromArpCache(item.getIp())); // propia o hija

		if (item.getIp().equals(gateway)) {
			isGateway = true;
			item.setTipoImg(Constantes.TIPE_GATEWAY);
		} else if (item.getIp().equals(loacalIp)) {
			isMyDevice = true;
			item.setTipoImg(Constantes.TIPE_DEVICE);
			item.setMac(macMyDevice); // propia o hija
		} else {
			item.setTipoImg(Constantes.TIPE_OTHERS);
		}

		for (InspectorTable item2 : arrayInspectorTable) {
			if (item.getMac().equals(item2.getMacdevice())) {
				isInBBDD = true;
				item.setNombre(item2.getNombre());
				item.setConocido(item2.getFavorito());
				break;
			}
		}
		if (!isInBBDD) {
			InspectorTable itemIns = new InspectorTable(item.getMac(), macPadre, "", (isGateway || isMyDevice) ? true : false);
			consultas.setItemInspectorTable(itemIns);
			arrayInspectorTable.add(itemIns);
			item.setNombre("");
			item.setConocido((isGateway || isMyDevice) ? true : false);
		}

		// agregamos el nombre del hardware
		NbtAddress[] nbts = new NbtAddress[0];
		try {
			Config.setProperty("jcifs.smb.client.soTimeout", "100");
			Config.setProperty("jcifs.smb.client.responseTimeout", "100");
			Config.setProperty("jcifs.netbios.soTimeout", "100");
			Config.setProperty("jcifs.netbios.retryTimeout", "100");

			nbts = NbtAddress.getAllByAddress(item.getIp());
			String netbiosname = nbts[0].getHostName();

			item.setNombre(netbiosname);
		} catch (UnknownHostException e) {
			item.setNombre("-");
			if (isMyDevice) {
				item.setNombre(ac.getString(R.string.midevice));
			}
			e.printStackTrace();
		}

		array.add(item);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		for (Machine ips : array) {
			ips.setNombreSoft(consultas.getNameFromMac(ips.getMac()));

		}
	}

	@Override
	protected void onCancelled() {
		// es.shutdownNow();
		super.onCancelled();
	}

	@Override
	protected void onCancelled(Void aVoid) {
		// es.shutdownNow();
		super.onCancelled(aVoid);
	}

	private String getMacFromArpCache(String ip) {
		if (ip == null)
			return null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;

			while ((line = br.readLine()) != null) {

				String[] splitted = line.split(" +");
				if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
					String mac = splitted[3];
					if (mac.matches("..:..:..:..:..:..")) {
						return mac;
					} else {
						return ac.getString(R.string.desconocido);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ac.getString(R.string.desconocido);
	}

}
