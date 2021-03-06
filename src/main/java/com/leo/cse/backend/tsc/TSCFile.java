package com.leo.cse.backend.tsc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.leo.cse.backend.StrTools;
import com.leo.cse.backend.exe.ExeData;

/**
 * Stores events loaded from a TSC file.
 * 
 * @author Leo
 *
 */
public class TSCFile {

	/**
	 * Raw script.
	 */
	private String script;
	/**
	 * Event map.
	 */
	private Map<Integer, String> events;

	/**
	 * Reads TSC from a file.
	 * 
	 * @param in
	 *            source file
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public TSCFile(File in) throws IOException {
		FileInputStream fileIn = new FileInputStream(in);
		FileChannel inChan = fileIn.getChannel();
		ByteBuffer dataBuf = null;
		int fileSize = 0;
		fileSize = (int) inChan.size();
		dataBuf = ByteBuffer.allocate(fileSize);
		inChan.read(dataBuf);
		fileIn.close();
		byte[] datArray = null;
		if (fileSize > 0) {
			int cypher = dataBuf.get(fileSize / 2);
			datArray = dataBuf.array();
			if (in.getName().endsWith(".tsc")) {
				for (int i = 0; i < fileSize; i++) {
					if (i != fileSize / 2) {
						datArray[i] -= cypher;
					}
				}
			}
		}
		// now read the input as text
		if (datArray != null)
			script = StrTools.CString(datArray, ExeData.getEncoding());
		// divide script into events
		events = new HashMap<>();
		String[] lines = script.split("\n");
		int currentEvent = -1;
		String eventBuf = null;
		for (String line : lines) {
			if (line.startsWith("#")) {
				// new event
				if (currentEvent > 0)
					events.put(currentEvent, eventBuf);
				currentEvent = Integer.parseUnsignedInt(line.substring(1, 5));
				eventBuf = null;
			} else {
				// normal line
				if (eventBuf == null)
					eventBuf = line;
				else
					eventBuf += line;
			}
		}
	}

	/**
	 * Reads TSC from a file.
	 * 
	 * @param inName
	 *            source file name
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public TSCFile(String inName) throws IOException {
		this(new File(inName));
	}

	/**
	 * Gets the raw script.
	 * 
	 * @return script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Gets all event IDs.
	 * 
	 * @return event IDs
	 */
	public Set<Integer> getEventIDs() {
		return events.keySet();
	}

	/**
	 * Gets the event map.
	 * 
	 * @return event map
	 */
	public Map<Integer, String> getEvents() {
		return events;
	}

	/**
	 * Gets an event.
	 * 
	 * @param number
	 *            event ID
	 * @return script in event
	 */
	public String getEvent(int number) {
		return events.get(number);
	}

}
