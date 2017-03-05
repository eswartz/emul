/*
  DemoManager.java

  (c) 2012-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.demos.DemoContentSource;
import v9t9.common.demos.DemoFormat;
import v9t9.common.demos.DemoHeader;
import v9t9.common.demos.IDemo;
import v9t9.common.demos.IDemoActorProvider;
import v9t9.common.demos.IDemoEventFormatter;
import v9t9.common.demos.IDemoFormatterRegistry;
import v9t9.common.demos.IDemoInputStream;
import v9t9.common.demos.IDemoManager;
import v9t9.common.demos.IDemoOutputStream;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.events.NotifyException;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.PathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.engine.demos.actors.TimerTickActor;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.format.DemoFormatInputStream;
import v9t9.engine.demos.format.DemoFormatOutputStream;
import v9t9.engine.demos.format.SoundDataEventFormatter;
import v9t9.engine.demos.format.SoundRegisterEventFormatter;
import v9t9.engine.demos.format.SpeechEventFormatter;
import v9t9.engine.demos.format.VdpV9938AccelCommandEventFormatter;
import v9t9.engine.demos.format.VideoDataEventFormatter;
import v9t9.engine.demos.format.VideoRegisterEventFormatter;
import v9t9.engine.demos.format.old.OldDemoFormat;
import v9t9.engine.demos.format.old.OldDemoFormatInputStream;
import v9t9.engine.demos.format.old.OldDemoFormatOutputStream;
import ejs.base.utils.CountingOutputStream;

/**
 * @author ejs
 *
 */
public class DemoManager implements IDemoManager {
	private static final Logger logger = Logger.getLogger(DemoManager.class);

	private List<IDemo> demos = new ArrayList<IDemo>();
	private IPathFileLocator locator;
	private final IMachine machine;
	private Map<String, IDemoActorProvider> actorProviders = new LinkedHashMap<String, IDemoActorProvider>();
	
	public DemoManager(IMachine machine) {
		this.machine = machine;
		this.locator = new PathFileLocator();
		
		locator.addReadOnlyPathProperty(Settings.get(machine, IDemoManager.settingBootDemosPath));
		locator.addReadOnlyPathProperty(Settings.get(machine, IDemoManager.settingUserDemosPath));
		locator.setReadWritePathProperty(Settings.get(machine, IDemoManager.settingRecordedDemosPath));
		
		registerActorProvider(new TimerTickActor.Provider());
		
		IDemoFormatterRegistry.INSTANCE.registerDemoEventFormatter(
				new VideoDataEventFormatter(DemoFormat.VIDEO_DATA));
		IDemoFormatterRegistry.INSTANCE.registerDemoEventFormatter(
				new VideoRegisterEventFormatter(DemoFormat.VIDEO_REGS));
		IDemoFormatterRegistry.INSTANCE.registerDemoEventFormatter(
				new SoundDataEventFormatter(DemoFormat.SOUND_DATA));
		IDemoFormatterRegistry.INSTANCE.registerDemoEventFormatter(
				new SoundRegisterEventFormatter(DemoFormat.SOUND_REGS));
		IDemoFormatterRegistry.INSTANCE.registerDemoEventFormatter(
				new SpeechEventFormatter(DemoFormat.SPEECH_DATA));
		IDemoFormatterRegistry.INSTANCE.registerDemoEventFormatter(
				new VdpV9938AccelCommandEventFormatter(DemoFormat.V9938_ACCEL));
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#registerContributor(v9t9.common.demo.IDemoContributor)
	 */
	@Override
	public void registerActorProvider(IDemoActorProvider actorProvider) {
		actorProviders.put(actorProvider.getEventIdentifier(), actorProvider);
	}
	
	@Override
	public IDemoPlaybackActor[] createPlaybackActors() {
		List<IDemoPlaybackActor> actors = new ArrayList<IDemoPlaybackActor>(actorProviders.size());
		for (IDemoActorProvider provider : actorProviders.values()) {
			IDemoPlaybackActor actor = provider.createForPlayback();
			if (actor != null)
				actors.add(actor);
		}
		return (IDemoPlaybackActor[]) actors.toArray(new IDemoPlaybackActor[actors.size()]);
	}
	

	@Override
	public IDemoRecordingActor[] createRecordingActors() {
		List<IDemoRecordingActor> actors = new ArrayList<IDemoRecordingActor>(actorProviders.size());
		for (IDemoActorProvider provider : actorProviders.values()) {
			IDemoRecordingActor actor = provider.createForRecording();
			if (actor != null)
				actors.add(actor);
		}
		return (IDemoRecordingActor[]) actors.toArray(new IDemoRecordingActor[actors.size()]);
	}

	@Override
	public IDemoReversePlaybackActor[] createReversePlaybackActors() {
		List<IDemoReversePlaybackActor> actors = new ArrayList<IDemoReversePlaybackActor>(actorProviders.size());
		for (IDemoActorProvider provider : actorProviders.values()) {
			IDemoReversePlaybackActor actor = provider.createForReversePlayback();
			if (actor != null)
				actors.add(actor);
		}
		return (IDemoReversePlaybackActor[]) actors.toArray(new IDemoReversePlaybackActor[actors.size()]);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#getDemoLocator()
	 */
	@Override
	public IPathFileLocator getDemoLocator() {
		return locator;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#getDemos()
	 */
	@Override
	public synchronized IDemo[] getDemos() {
		return demos.toArray(new IDemo[demos.size()]);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#reload()
	 */
	@Override
	public synchronized void reload() {
		
		demos.clear();
		
		for (URI dirURI : locator.getSearchURIs()) {
			IEmulatorContentSource[] sources = new DemoContentProvider(machine).analyze(dirURI);
			for (IEmulatorContentSource source : sources) {
				if (source instanceof DemoContentSource) {
					IDemo demo = ((DemoContentSource) source).getContent();
					demos.add(demo);
				}
			}
		}
		logger.debug("done scanning");
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#addDemo(v9t9.common.demo.IDemo)
	 */
	@Override
	public void addDemo(IDemo demo) {
		if (!demos.contains(demo))
			demos.add(demo);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#removeDemo(v9t9.common.demo.IDemo)
	 */
	@Override
	public void removeDemo(IDemo demo) {
		demos.remove(demo);
	}


	/**
	 * @param uri
	 * @return
	 * @throws IOException
	 * @throws NotifyException
	 */
	public IDemoInputStream createDemoReader(URI uri) throws IOException,
			NotifyException {
		InputStream is = locator.createInputStream(uri);
		byte[] magic = new byte[4];
		is.read(magic);
		
		if (DemoHeader.isV9t9jFormat(magic)) {
			DemoFormatInputStream inputStream = new DemoFormatInputStream(machine.getModel(), magic, is);
			return inputStream;
		} else if (Arrays.equals(magic, OldDemoFormat.DEMO_MAGIC_HEADER_V910)) {
			return new OldDemoFormatInputStream(is);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoHandler#createDemoWriter(java.net.URI)
	 */
	@Override
	public IDemoOutputStream createDemoWriter(URI uri) throws IOException,
			NotifyException {
		if (! Settings.get(machine, settingUseDemoOldFormat).getBoolean()) {
			DemoHeader header = new DemoHeader(DemoHeader.DEMO_MAGIC_HEADER_V9t9);
			header.setMachineModel(machine.getModel().getIdentifier());
			for (IDemoActorProvider actor : actorProviders.values()) {
				if (actor.getEventIdentifier().equals(TimerTick.ID))
					continue;
				
				IDemoEventFormatter formatter = IDemoFormatterRegistry.INSTANCE.findFormatterByEvent(
						actor.getEventIdentifier());
				if (formatter != null) {
					header.findOrAllocateIdentifier(formatter.getBufferIdentifer());
				}
			}
			DemoFormatOutputStream demoStream = new DemoFormatOutputStream(header, 
					new CountingOutputStream(new BufferedOutputStream(locator.createOutputStream(uri))));
			
			return demoStream;
		} else {
			return new OldDemoFormatOutputStream( 
					new CountingOutputStream(new BufferedOutputStream(locator.createOutputStream(uri))));
		}
	}
}
