package org.guvnor.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

public class GuvnorLocationManager {
	
	private static final String REP_CACHE_NAME = ".replist";
	
	private ArrayList<GuvnorRepository> repList = new ArrayList<GuvnorRepository>();
	
	/*
	 * Package restricted
	 */
	GuvnorLocationManager() {
		try {
			load();
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	public void addRespository(String location, String username, String password) throws Exception {
		addRepository(new GuvnorRepository(location, username, password));
	}
	
	public void addRepository(GuvnorRepository rep) throws Exception {
		if (findRepository(rep.getLocation()) != null) {
			throw new Exception("Duplicate repository: " + rep.getLocation());
		}
		repList.add(rep);
		notifyListeners(IRepositorySetListener.REP_ADDED);
		try {
			commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<GuvnorRepository> getRepositories() {
		return repList;
	}
	
	public boolean removeRepository(String rep) {
		GuvnorRepository theRep = findRepository(rep);
		if (theRep == null) {
			return false;
		}
		boolean res = repList.remove(theRep);
		notifyListeners(IRepositorySetListener.REP_ADDED);
		try {
			commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	private GuvnorRepository findRepository(String rep) {
		GuvnorRepository theRep = null;
		for (int i = 0; i < repList.size(); i++) {
			if (repList.get(i).getLocation().equals(rep)) {
				theRep = repList.get(i);
				break;
			}
		}
		return theRep;
	}
	
	private void commit() throws Exception {
		File repFile = Activator.getDefault().getStateLocation().
										append(REP_CACHE_NAME).toFile();
		FileOutputStream fos = new FileOutputStream(repFile);
		PrintWriter writer = new PrintWriter(fos);
		for (int i = 0; i < repList.size(); i++) {
			GuvnorRepository oneRep = repList.get(i);
			writer.println(oneRep.getLocation());
			writer.println(oneRep.getUsername());
			writer.println(oneRep.getPassword());
		}
		writer.flush();
		fos.flush();
		fos.close();
	}
	
	private void load() throws Exception {
		repList.clear();
		File repFile = Activator.getDefault().getStateLocation().
									append(REP_CACHE_NAME).toFile();
		if (!repFile.exists()) {
			return;
		}
		FileInputStream fis = new FileInputStream(repFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
		String oneRep = null;
		do {
			oneRep = reader.readLine();
			if (oneRep != null && oneRep.trim().length() == 0) {
				oneRep = null;
			}
			String oneUN = reader.readLine();
			String onePW = reader.readLine();
			if (oneRep != null) {
				if (oneUN != null && onePW != null) {
					repList.add(new GuvnorRepository(oneRep, oneUN, onePW));
				} else {
					System.out.println("incomplete rep: " + oneRep);
				}
			}
		} while (oneRep != null);
	}
	
	public interface IRepositorySetListener {
		public static final int REP_ADDED = 0;
		public static final int REP_REMOVED = 1;
		
		public void repositorySetChanged(int type, List<GuvnorRepository> repList);
	}
	
	private List<IRepositorySetListener> repsetListeners = new ArrayList<IRepositorySetListener>();
	
	public boolean addRepositorySetListener(IRepositorySetListener listener) {
		return repsetListeners.add(listener);
	}
	
	public boolean removeRepositorySetListener(IRepositorySetListener listener) {
		return repsetListeners.remove(listener);
	}
	
	private void notifyListeners(int eventType) {
		for (int i = 0; i < repsetListeners.size(); i++) {
			try {
				repsetListeners.get(i).repositorySetChanged(eventType, repList);
			} catch (Throwable t) {
				Activator.getDefault().writeLog(IStatus.ERROR, t.getMessage(), t);
			}
		}
	}
}