package com.lc.core.utils.qqway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IPSeeker {
	
	private static Logger log = LoggerFactory
	.getLogger(IPSeeker.class);
	private String IP_FILE = "QQWry.Dat";
	private String INSTALL_DIR = "d:/qqwry";

	private static final int IP_RECORD_LENGTH = 7;
	private static final byte REDIRECT_MODE_1 = 0x01;
	private static final byte REDIRECT_MODE_2 = 0x02;

	private Map<String, IPLocation> ipCache;
	private RandomAccessFile ipFile;
	private MappedByteBuffer mbb;
	private long ipBegin, ipEnd;
	private IPLocation loc;
	private byte[] buf;
	private byte[] b4;
	private byte[] b3;

	public IPSeeker(String fileName,String dir)  {   
		        this.INSTALL_DIR=dir;   
		        this.IP_FILE=fileName;   
		        ipCache = new HashMap<String, IPLocation>();   
		        loc = new IPLocation();   
		        buf = new byte[100];   
		        b4 = new byte[4];   
		        b3 = new byte[3];   
		        try {   
		            ipFile = new RandomAccessFile(IP_FILE, "r");
		        } catch (FileNotFoundException e) {
		            String filename = new File(IP_FILE).getName().toLowerCase();   
		            File[] files = new File(INSTALL_DIR).listFiles();   
		            for(int i = 0; i < files.length; i++) {   
		                if(files[i].isFile()) {   
		                    if(files[i].getName().toLowerCase().equals(filename)) {   
		                        try {   
		                            ipFile = new RandomAccessFile(files[i], "r");   
		                        } catch (FileNotFoundException e1) {   
		                            log.error("IP地址信息文件没有找到，IP显示功能将无法使用"+e1.getMessage());   
		                            ipFile = null;   
		                        }   
		                        break;   
		                    }   
		                }   
		            }   
		        }    
		        if(ipFile != null) {   
		            try {   
		                ipBegin = readLong4(0);   
		                ipEnd = readLong4(4);   
		                if(ipBegin == -1 || ipEnd == -1) {   
		                    ipFile.close();   
		                    ipFile = null;   
		                }              
		            } catch (IOException e) {   
		                log.error("IP地址信息文件格式有错误，IP显示功能将无法使用"+e.getMessage());   
		                ipFile = null;   
		            }              
		        }   
	}
	public List<IPEntry> getIPEntriesDebug(String s) {
		List<IPEntry> ret = new ArrayList<IPEntry>();
		try {
			long endOffset = ipEnd + 4;
			for (long offset = ipBegin + 4; offset <= endOffset; offset += IP_RECORD_LENGTH) {
				long temp = readLong3(offset);
				if (temp != -1) {
					IPLocation ipLoc = getIPLocation(temp);
					if (ipLoc.getCountry().indexOf(s) != -1
							|| ipLoc.getArea().indexOf(s) != -1) {
						IPEntry entry = new IPEntry();
						entry.country = ipLoc.getCountry();
						entry.area = ipLoc.getArea();
						readIP(offset - 4, b4);
						entry.beginIp = Util.getIpStringFromBytes(b4);
						readIP(temp, b4);
						entry.endIp = Util.getIpStringFromBytes(b4);
						ret.add(entry);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return ret;
	}

	public IPLocation getIPLocation(String ip) {
		IPLocation location = new IPLocation();
		location.setArea(this.getArea(ip));
		location.setCountry(this.getCountry(ip));
		return location;
	}

	public List<IPEntry> getIPEntries(String s) {
		List<IPEntry> ret = new ArrayList<IPEntry>();
		try {
			if (mbb == null) {
				FileChannel fc = ipFile.getChannel();
				mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, ipFile.length());
				mbb.order(ByteOrder.LITTLE_ENDIAN);
			}

			int endOffset = (int) ipEnd;
			for (int offset = (int) ipBegin + 4; offset <= endOffset; offset += IP_RECORD_LENGTH) {
				int temp = readInt3(offset);
				if (temp != -1) {
					IPLocation ipLoc = getIPLocation(temp);
					if (ipLoc.getCountry().indexOf(s) != -1
							|| ipLoc.getArea().indexOf(s) != -1) {
						IPEntry entry = new IPEntry();
						entry.country = ipLoc.getCountry();
						entry.area = ipLoc.getArea();
						readIP(offset - 4, b4);
						entry.beginIp = Util.getIpStringFromBytes(b4);
						readIP(temp, b4);
						entry.endIp = Util.getIpStringFromBytes(b4);
						ret.add(entry);
					}
				}
			}
		} catch (IOException e) {
			log.error("" + e.getMessage());
		}
		return ret;
	}

	private int readInt3(int offset) {
		mbb.position(offset);
		return mbb.getInt() & 0x00FFFFFF;
	}

	private int readInt3() {
		return mbb.getInt() & 0x00FFFFFF;
	}

	public String getCountry(byte[] ip) {
		if (ipFile == null)
			return Message.bad_ip_file;
		String ipStr = Util.getIpStringFromBytes(ip);
		if (ipCache.containsKey(ipStr)) {
			IPLocation ipLoc = ipCache.get(ipStr);
			return ipLoc.getCountry();
		} else {
			IPLocation ipLoc = getIPLocation(ip);
			ipCache.put(ipStr, ipLoc.getCopy());
			return ipLoc.getCountry();
		}
	}

	public String getCountry(String ip) {
		return getCountry(Util.getIpByteArrayFromString(ip));
	}

	public String getArea(byte[] ip) {
		if (ipFile == null)
			return Message.bad_ip_file;
		String ipStr = Util.getIpStringFromBytes(ip);
		if (ipCache.containsKey(ipStr)) {
			IPLocation ipLoc = ipCache.get(ipStr);
			return ipLoc.getArea();
		} else {
			IPLocation ipLoc = getIPLocation(ip);
			ipCache.put(ipStr, ipLoc.getCopy());
			return ipLoc.getArea();
		}
	}

	public String getArea(String ip) {
		return getArea(Util.getIpByteArrayFromString(ip));
	}

	private IPLocation getIPLocation(byte[] ip) {
		IPLocation info = null;
		long offset = locateIP(ip);
		if (offset != -1)
			info = getIPLocation(offset);
		if (info == null) {
			info = new IPLocation();
			info.setCountry(Message.unknown_country);
			info.setArea(Message.unknown_area);
		}
		return info;
	}

	private long readLong4(long offset) {
		long ret = 0;
		try {
			ipFile.seek(offset);
			ret |= (ipFile.readByte() & 0xFF);
			ret |= ((ipFile.readByte() << 8) & 0xFF00);
			ret |= ((ipFile.readByte() << 16) & 0xFF0000);
			ret |= ((ipFile.readByte() << 24) & 0xFF000000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	private long readLong3(long offset) {
		long ret = 0;
		try {
			ipFile.seek(offset);
			ipFile.readFully(b3);
			ret |= (b3[0] & 0xFF);
			ret |= ((b3[1] << 8) & 0xFF00);
			ret |= ((b3[2] << 16) & 0xFF0000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	private long readLong3() {
		long ret = 0;
		try {
			ipFile.readFully(b3);
			ret |= (b3[0] & 0xFF);
			ret |= ((b3[1] << 8) & 0xFF00);
			ret |= ((b3[2] << 16) & 0xFF0000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	private void readIP(long offset, byte[] ip) {
		try {
			ipFile.seek(offset);
			ipFile.readFully(ip);
			byte temp = ip[0];
			ip[0] = ip[3];
			ip[3] = temp;
			temp = ip[1];
			ip[1] = ip[2];
			ip[2] = temp;
		} catch (IOException e) {
			log.error("" + e.getMessage());
		}
	}

	private void readIP(int offset, byte[] ip) {
		mbb.position(offset);
		mbb.get(ip);
		byte temp = ip[0];
		ip[0] = ip[3];
		ip[3] = temp;
		temp = ip[1];
		ip[1] = ip[2];
		ip[2] = temp;
	}

	private int compareIP(byte[] ip, byte[] beginIp) {
		for (int i = 0; i < 4; i++) {
			int r = compareByte(ip[i], beginIp[i]);
			if (r != 0)
				return r;
		}
		return 0;
	}

	private int compareByte(byte b1, byte b2) {
		if ((b1 & 0xFF) > (b2 & 0xFF))
			return 1;
		else if ((b1 ^ b2) == 0)
			return 0;
		else
			return -1;
	}

	private long locateIP(byte[] ip) {
		long m = 0;
		int r;
		readIP(ipBegin, b4);
		r = compareIP(ip, b4);
		if (r == 0)
			return ipBegin;
		else if (r < 0)
			return -1;
		for (long i = ipBegin, j = ipEnd; i < j;) {
			m = getMiddleOffset(i, j);
			readIP(m, b4);
			r = compareIP(ip, b4);
			if (r > 0)
				i = m;
			else if (r < 0) {
				if (m == j) {
					j -= IP_RECORD_LENGTH;
					m = j;
				} else
					j = m;
			} else
				return readLong3(m + 4);
		}
		m = readLong3(m + 4);
		readIP(m, b4);
		r = compareIP(ip, b4);
		if (r <= 0)
			return m;
		else
			return -1;
	}

	private long getMiddleOffset(long begin, long end) {
		long records = (end - begin) / IP_RECORD_LENGTH;
		records >>= 1;
		if (records == 0)
			records = 1;
		return begin + records * IP_RECORD_LENGTH;
	}

	private IPLocation getIPLocation(long offset) {
		try {
			ipFile.seek(offset + 4);
			byte b = ipFile.readByte();
			if (b == REDIRECT_MODE_1) {
				long countryOffset = readLong3();
				ipFile.seek(countryOffset);
				b = ipFile.readByte();
				if (b == REDIRECT_MODE_2) {
					loc.setCountry(readString(readLong3()));
					ipFile.seek(countryOffset + 4);
				} else
					loc.setCountry(readString(countryOffset));
				loc.setArea(readArea(ipFile.getFilePointer()));
			} else if (b == REDIRECT_MODE_2) {
				loc.setCountry(readString(readLong3()));
				loc.setArea(readArea(offset + 8));
			} else {
				loc.setCountry(readString(ipFile.getFilePointer() - 1));
				loc.setArea(readArea(ipFile.getFilePointer()));
			}
			return loc;
		} catch (IOException e) {
			return null;
		}
	}

	private IPLocation getIPLocation(int offset) {
		mbb.position(offset + 4);
		byte b = mbb.get();
		if (b == REDIRECT_MODE_1) {
			int countryOffset = readInt3();
			mbb.position(countryOffset);
			b = mbb.get();
			if (b == REDIRECT_MODE_2) {
				loc.setCountry(readString(readInt3()));
				mbb.position(countryOffset + 4);
			} else
				loc.setCountry(readString(countryOffset));
			loc.setArea(readArea(mbb.position()));
		} else if (b == REDIRECT_MODE_2) {
			loc.setCountry(readString(readInt3()));
			loc.setArea(readArea(offset + 8));
		} else {
			loc.setCountry(readString(mbb.position() - 1));
			loc.setArea(readArea(mbb.position()));
		}
		return loc;
	}

	private String readArea(long offset) throws IOException {
		ipFile.seek(offset);
		byte b = ipFile.readByte();
		if (b == REDIRECT_MODE_1 || b == REDIRECT_MODE_2) {
			long areaOffset = readLong3(offset + 1);
			if (areaOffset == 0)
				return Message.unknown_area;
			else
				return readString(areaOffset);
		} else
			return readString(offset);
	}

	private String readArea(int offset) {
		mbb.position(offset);
		byte b = mbb.get();
		if (b == REDIRECT_MODE_1 || b == REDIRECT_MODE_2) {
			int areaOffset = readInt3();
			if (areaOffset == 0)
				return Message.unknown_area;
			else
				return readString(areaOffset);
		} else
			return readString(offset);
	}

	private String readString(long offset) {
		try {
			ipFile.seek(offset);
			int i;
			for (i = 0, buf[i] = ipFile.readByte(); buf[i] != 0; buf[++i] = ipFile
					.readByte())
				;
			if (i != 0)
				return Util.getString(buf, 0, i, "GBK");
		} catch (IOException e) {
			log.error("" + e.getMessage());
		}
		return "";
	}

	private String readString(int offset) {
		try {
			mbb.position(offset);
			int i;
			for (i = 0, buf[i] = mbb.get(); buf[i] != 0; buf[++i] = mbb.get())
				;
			if (i != 0)
				return Util.getString(buf, 0, i, "GBK");
		} catch (IllegalArgumentException e) {
			log.error("" + e.getMessage());
		}
		return "";
	}
}