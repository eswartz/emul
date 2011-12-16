/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The IP address utility provides a safe method to get all local IP addresses,
 * check if any given address refers to the local host, and compare IP addresses.
 */
public class IPAddressUtil {

	/**
	 * Constants for address types. They are sorted by "quality", i.e. the higher the total value of all flags is, the
	 * "better" or "more canonical" an address is assumed to be. Any "global" address type is better than all other
	 * addresses; among address types, the canonical name is better than the address which is better than a plain name;
	 * IPv4 is better than IPv6.
	 */
	public final static int HOSTMAP_IPV6 = 0x01;
	public final static int HOSTMAP_IPV4 = 0x02;

	public final static int HOSTMAP_NAME = 0x04;
	public final static int HOSTMAP_ADDR = 0x08;
	public final static int HOSTMAP_CANONICALNAME = 0x10;
	public final static int HOSTMAP_CANONICALADDR = 0x20;

	public final static int HOSTMAP_MULTICAST = 0x40;
	public final static int HOSTMAP_LOOPBACK = 0x80;
	public final static int HOSTMAP_LINKLOCAL = 0x100;
	public final static int HOSTMAP_SITELOCAL = 0x200;
	public final static int HOSTMAP_GLOBAL = 0x400;

	// shortcuts
	public final static int HOSTMAP_ANY_UNICAST = HOSTMAP_LOOPBACK | HOSTMAP_LINKLOCAL | HOSTMAP_SITELOCAL | HOSTMAP_GLOBAL;

	private final Map<String, Integer> fLocalHostAddresses = new HashMap<String, Integer>();
	private final Set<String> fNonLocalHostAddresses = new HashSet<String>();
	private String fCanonicalAddress = null;

	IPAddressUtil() {
		initializeHostCache();
	}

	private synchronized void initializeHostCache() {
		// first, add the known interfaces. This is the only safe method to get
		// the _real_ IP addresses, and get _all_ of them.
		addLocalAddressesByInterface();
		try {
			// Add what Java thinks is the local host.
			InetAddress localHostJava = InetAddress.getLocalHost();
			// Do _not_ add the address that Java thinks the local host has,
			// since it may be wrong! This is due to the method Java uses:
			// it takes the host _name_ and does a reverse name lookup
			// to get the address. This may be _wrong_ in case the DNS server
			// points to a different (or outdated) address for the name.
			// In reality, _only_ the addresses given by our own interfaces
			// are correct! (As obtained by addLocalAddressesByInterface()).
			// addLocalAddress(localHostJava);

			// Add what Java thinks is the local host name.
			// The local host name correct in the sense that it is configured
			// locally and thus known locally. Note that in case of DNS inconsistency,
			// DNS servers will return a _different_ address for the name than the
			// local one, in this case the host name will be added as non-local.
			addHostName(localHostJava.getHostName());
		} catch (UnknownHostException e) {
			/* no error */
		}
		// finally, add the "localhost" special host name since it might not be covered
		// by the methods above (we cannot get all names for a given address, only the other way round).
		addHostName("localhost"); //$NON-NLS-1$
		// and initialize the "canonical hostname" cache.
		getCanonicalAddress();
	}

	/**
	 * Iterate over local interfaces and add IP-addresses. This is the only safe method to get all local IP-addresses,
	 * since InetAddr.getAllByName() may fail to get the local IP address in case the local hostname is configured to be
	 * resolved to the loopback adapter (in this case, only the loopback adapter's address is returned). When this
	 * method has run, we know that we have all IP addresses of this machine. We can not know all the names of this
	 * machine since there s no method to query all name servers for all addresses. Therefore, new names may be added
	 * later by @see addHostName(String).
	 */
	private synchronized void addLocalAddressesByInterface() {
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			/* no error, try other method */
		}
		while (interfaces != null && interfaces.hasMoreElements()) {
			NetworkInterface iface = interfaces.nextElement();
			Enumeration<InetAddress> addresses = iface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				addLocalAddress(addr);
			}
		}
	}

	private synchronized void addLocalAddress(InetAddress addr) {
		int addrtype;
		if (addr.isLoopbackAddress()) {
			addrtype = HOSTMAP_LOOPBACK;
		} else if (addr.isLinkLocalAddress()) {
			addrtype = HOSTMAP_LINKLOCAL;
		} else if (addr.isSiteLocalAddress()) {
			addrtype = HOSTMAP_SITELOCAL;
		} else if (addr.isMulticastAddress()) {
			addrtype = HOSTMAP_MULTICAST;
		} else {
			addrtype = HOSTMAP_GLOBAL;
		}
		if (addr.getAddress().length == 4) {
			addrtype |= HOSTMAP_IPV4;
		} else {
			addrtype |= HOSTMAP_IPV6;
		}
		String addrAsString = addr.getHostAddress();
		fLocalHostAddresses.put(addrAsString, new Integer(addrtype | HOSTMAP_ADDR));
		if (0 == (addrtype & (HOSTMAP_LINKLOCAL | HOSTMAP_SITELOCAL | HOSTMAP_MULTICAST))) {
			// Don't do DNS Reverse Loopkup's for non-routable addresses.
			// They won't be known to the Name Server anyway, and they
			// make startup _much_ slower.
			String addrAsNameCan = addr.getCanonicalHostName().toLowerCase();
			// query the name after the canonical name, it will re-use
			// cached canonical name (if the name was not explicitly set)
			String addrAsName = addr.getHostName().toLowerCase();
			if (!addrAsNameCan.equals(addrAsString)) {
				// We must check if we really got a name, since InetAddress.getHostName()
				// returns the original address in case it thinks the name is spoofed!
				if (0 == (addrtype & HOSTMAP_LOOPBACK)) {
					// Not loopback --> found a Canonical Name.
					fLocalHostAddresses.put(addrAsNameCan, new Integer(addrtype | HOSTMAP_NAME | HOSTMAP_CANONICALNAME));
					// override the address as canonical-address
					fLocalHostAddresses.put(addrAsString, new Integer(addrtype | HOSTMAP_ADDR | HOSTMAP_CANONICALADDR));
				} else {
					// Loopback --> add the found name as non-canonical.
					fLocalHostAddresses.put(addrAsNameCan, new Integer(addrtype | HOSTMAP_NAME));
				}
			}
			if (!addrAsName.equals(addrAsString) && !addrAsName.equals(addrAsNameCan)) {
				// don't override the canonical name by the name.
				fLocalHostAddresses.put(addrAsName, new Integer(addrtype | HOSTMAP_NAME));
			}
		}
	}

	private synchronized boolean addHostAddress(InetAddress addr, String hostName) {
		if (addr == null) {
			// Address for host name could not be resolved --> add to non-local-addresses
			fNonLocalHostAddresses.add(hostName);
			return false;
		}

		// Get the host address
		String hostAddr = addr.getHostAddress();

		// Newly discovered loopback addresses are added
		// to the local host address list first
		if (!fLocalHostAddresses.containsKey(hostAddr) && addr.isLoopbackAddress()) {
			addLocalAddress(addr);
		}

		Integer entryType = fLocalHostAddresses.get(hostAddr);
		if (entryType != null) {
			// found a new name for a known local address ?
			if (!fLocalHostAddresses.containsKey(hostName)) {
				int addrtype = entryType.intValue() & (~(HOSTMAP_ADDR | HOSTMAP_CANONICALADDR));
				fLocalHostAddresses.put(hostName, new Integer(addrtype | HOSTMAP_NAME));
			}
			return true;
		}

		fNonLocalHostAddresses.add(hostName);
		fNonLocalHostAddresses.add(hostAddr);
		return false;
	}

	/**
	 * Add a host name to internal caching tables.
	 *
	 * @param hostName String host name or address as String
	 * @return <code>true</code> if the added Host was considered "local", false otherwise.
	 */
	public boolean addHostName(String hostName) {
		hostName = hostName.toLowerCase();
		try {
			// Only take the first address: in case of multiple addresses
			// on the remote host, some of them could be on different
			// subnetworks and thus be non-local although they match one
			// of our local addresses!
			//
			// Make sure that the name service resolving (probably time-consuming!)
			// is outside our synchronized block.
			InetAddress addr = InetAddress.getByName(hostName);
			return addHostAddress(addr, hostName);
		} catch (UnknownHostException e) {
			/* got an illegal name --> add as non-local. */
			return addHostAddress(null, hostName);
		}
	}

	/**
	 * Return a list of hostnames or addresses for the local host. In case loopback addresses were asked for, these will
	 * appear first in the list. Example: String[] addresses =
	 * getLocalHostAddresses(HOSTMAP_ADDR|HOSTMAP_LOOPBACK|HOSTMAP_IPV4);
	 *
	 * @param typesToGet an integer bitmask of the types to get, uses the HOSTMAP constants declared in this class:
	 *            HOSTMAP_NAME - get hostnames HOSTMAP_ADDR - get IP-addresses HOSTMAP_CANONICALNAME - get the
	 *            "canonical" hostnames for each interface HOSTMAP_CANONICALADDR - get the "canonical" addresses for each
	 *            interface HOSTMAP_LOOPBACK - get names/addresses for the loopback interface HOSTMAP_LINKLOCAL - get
	 *            names/addresses for link-local non-routable interfaces HOSTMAP_SITELOCAL - get names/addresses for
	 *            site-local non-routable interfaces HOSTMAP_MULTICAST - get multicast names/addresses HOSTMAP_GLOBAL -
	 *            get names/addresses that are globally valid HOSTMAP_ANY_UNICAST - get names/addresses for any local
	 *            non-unicast address HOSTMAP_IPV4 - get IPv4 names/addresses HOSTMAP_IPV6 - get IPv6 names/addresses
	 * @return String[] array of IP-addresses in String representation.
	 */
	public synchronized String[] getLocalHostAddresses(int typesToGet) {
		if ((typesToGet & HOSTMAP_ADDR) != 0) {
			typesToGet |= HOSTMAP_CANONICALADDR; // plain address query includes the canonical address
		}
		if ((typesToGet & HOSTMAP_NAME) != 0) {
			typesToGet |= HOSTMAP_CANONICALNAME; // plain name query includes the canonical name
		}
		List<String> addresses = new ArrayList<String>(fLocalHostAddresses.size());
		Iterator<Entry<String, Integer>> it = fLocalHostAddresses.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Integer> entry = it.next();
			int addrtype = entry.getValue().intValue();
			if ((addrtype & typesToGet) == addrtype) {
				if ((addrtype & HOSTMAP_LOOPBACK) != 0) {
					addresses.add(0, entry.getKey()); // add loopback addresses first
				} else {
					addresses.add(entry.getKey());
				}
			}
		}
		return addresses.toArray(new String[addresses.size()]);
	}

	/**
	 * Returns an IPv4 address to safely connect to the local host. This method works around limitations in the Java
	 * library's provisions for obtaining the local host address: - InetAddress.getByName("localhost") might be IPv6,
	 * and it might be mapped to a non-local host by the name service - InetAddress.getByName(null) might fail due to
	 * disabled loopback adapter (ifconfig lo down) - InetAddress.getLocalHost() returns non-loopback-address which is
	 * not optimal (slower than loopback, may disappear when removing a network cable or disconnecting from dial-up
	 * network connection) Therefore, the method below relies on information obtained from Enumeration
	 * NetworkInterface.getNetworkInterfaces() to know if an address is local.
	 *
	 * @return String representation of IPv4 address referring to the local host, or <code>null</code> if no address is
	 *         found that allows to connect to the local host via IPv4.
	 */
	public synchronized String getIPv4LoopbackAddress() {
		// first, try the official IPv4 loopback address as per Internet RFC.
		// This can fail only in case of disabled loopback adapter ("ifconfig lo down").
		// Should perhaps be removed here in order to allow configuring preferred
		// localhost connection from the outside.
		if (isLocalHost("127.0.0.1")) { //$NON-NLS-1$
			return "127.0.0.1"; //$NON-NLS-1$
		}
		// next, check if "localhost" is configured as an IPv4 address:
		// if yes, it can be used directly. This allows to configure
		// the preferred method for local host connections from the outside.
		Integer key = fLocalHostAddresses.get("localhost"); //$NON-NLS-1$
		if (key != null && (key.intValue() & HOSTMAP_IPV4) != 0) { return "localhost"; //$NON-NLS-1$
		}
		// finally ("localhost" mis-configured), obtain an address from NetworkInterfaces.
		// loopback addresses are sorted first, so they are preferred
		int typemask = HOSTMAP_ADDR | HOSTMAP_IPV4 | HOSTMAP_ANY_UNICAST;
		String[] candidates = getLocalHostAddresses(typemask);
		if (candidates.length == 0) {
			// re-initialize the cache, perhaps some interfaces were brought up
			// in the meantime (e.g. hot-plug network cards)
			addLocalAddressesByInterface();
			candidates = getLocalHostAddresses(typemask);
		}
		return candidates.length > 0 ? candidates[0] : null;
	}

	/**
	 * Returns the canonical name or address of this host. A "best effort" is made to return the address that is assumed
	 * to be "most canonical". A global IP address is considered better than a host name, since name service
	 * configuration might not be global.
	 *
	 * @return String IP address of the local host as it should be reachable from the outside.
	 */
	public synchronized String getCanonicalAddress() {
		if (fCanonicalAddress == null) {
			Iterator<Entry<String, Integer>> it = fLocalHostAddresses.entrySet().iterator();
			String bestAddress = null;
			int bestAddrType = 0;
			while (it.hasNext()) {
				Entry<String, Integer> curEntry = it.next();
				int curAddrType = curEntry.getValue().intValue();
				if (curAddrType > bestAddrType) {
					bestAddress = curEntry.getKey();
					bestAddrType = curAddrType;
				}
			}
			// fCanonicalAddress = InetAddress.getByName(bestAddress);
			fCanonicalAddress = bestAddress;
		}
		return fCanonicalAddress;
	}

	/**
	 * Returns a list of host names that are considered "most canonical" on this host. The names are guaranteed to refer
	 * to global IP addresses of this machine. The list may be empty if no proper name is configured.
	 *
	 * @return String[] host names, may be empty
	 */
	public synchronized String[] getCanonicalHostNames() {
		int typeMask = IPAddressUtil.HOSTMAP_CANONICALNAME | IPAddressUtil.HOSTMAP_GLOBAL | IPAddressUtil.HOSTMAP_IPV4;
		String canonicalNames[] = IPAddressUtil.getInstance().getLocalHostAddresses(typeMask);
		if (canonicalNames.length == 0) {
			typeMask |= IPAddressUtil.HOSTMAP_NAME;
			canonicalNames = IPAddressUtil.getInstance().getLocalHostAddresses(typeMask);
		}
		return canonicalNames;
	}

	/**
	 * Find out if the given host name is the local host.
	 *
	 * @param host String hostname or IP address
	 * @return <code>true</code> if the given host refers to the local host.
	 */
	public boolean isLocalHost(String host) {
		if (host == null) { return false; }
		String hostLower = host.toLowerCase();
		// Don't trim, perhaps it is possible to have addresses ended by space
		// hostLower = hostLower.trim();
		synchronized (this) {
			if (fLocalHostAddresses.containsKey(hostLower)) {
				return true;
			} else if (fNonLocalHostAddresses.contains(hostLower)) { return false; }
		}
		// Make sure that the name service lookup (probably time consuming!)
		// is outside the synchronized block.
		return addHostName(hostLower);
	}

	/**
	 * Find out if two IP Addresses refer to the same host.
	 *
	 * @param h1 host name or IP address
	 * @param h2 host name or IP address
	 * @return
	 */
	public boolean isSameHost(String h1, String h2) {
		if (h1 == null) {
			return (h2 == null);
		} else if (h2 == null) { return false; }
		h1 = h1.trim();
		h2 = h2.trim();
		if (h1.equalsIgnoreCase(h2)) { return true; }
		// The local host can be referred to by several methods...
		if (isLocalHost(h1) && isLocalHost(h2)) { return true; }
		// Compare IP-addresses? How to know if two hosts are the same?
		// Only the first IP-Address we get should be checked. But what if
		// h1 and h2 are different IP-Addresses referring to the same host?
		// The check would be complex and probably slow since name service
		// must be invoked. So we do not do it for now.
		return false;
	}

	// Initialize-On-Demand Holder Class idiom:
	// Lazy initialization and thread-safe single instance.
	// See http://www-106.ibm.com/developerworks/java/library/j-jtp03304/
	private static class LazyHolder {
		public static IPAddressUtil instance = new IPAddressUtil();
	}

	/**
	 * Return the singleton instance.
	 */
	public static IPAddressUtil getInstance() {
		// LazyRegistryHolder class will be loaded in thread-safe manner
		// the first time it is used.
		return LazyHolder.instance;
	}

}
