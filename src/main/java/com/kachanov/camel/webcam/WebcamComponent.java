package com.kachanov.camel.webcam;

import java.awt.Dimension;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.github.sarxos.webcam.WebcamDriver;

@Component("webcam")
public class WebcamComponent extends DefaultComponent implements WebcamDiscoveryListener {

	private Map<String, Webcam> webcams = new HashMap<>();

	private int timeout = 30_000;

	private boolean webcamStarted;

	private String driver;

	public WebcamComponent() {
		// nothing
	}

	protected Endpoint createEndpoint( String uri, String remaining, Map<String, Object> parameters ) throws Exception {
		Endpoint endpoint = new WebcamEndpoint( uri, this );
		setProperties( endpoint, parameters );

		return endpoint;
	}

	// Life-cycle

	@Override
	protected void doStart() throws Exception {
		super.doStart();

		// Use the provided webcam/s
		if (getWebcams().size() == 0) {
			loadWebcamDriver();

			List<Webcam> webcamList = Webcam.getWebcams( timeout );
			if (webcamList == null || webcamList.size() == 0) {
				throw new IllegalStateException( "No webcams found" );
			}
			webcamList.forEach( w -> webcams.put( w.getDevice().getName(), w ) );
		}

	}

	protected void loadWebcamDriver() {

		if (webcamStarted) {
			return;
		}

		try {

			// use specified driver
			if (getDriver() != null) {
				Class<WebcamDriver> clazz = (Class<WebcamDriver>) Class.forName( getDriver() );
				Constructor<WebcamDriver> constructor = clazz.getConstructor();
				WebcamDriver driver = constructor.newInstance();
				Webcam.setDriver( driver );
				webcamStarted = true;

			}

		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	protected void doStop() throws Exception {
		webcams.values().forEach( w -> w.close() );

		super.doStop();
	}

	// Webcam listeners
	@Override
	public void webcamFound( WebcamDiscoveryEvent event ) {
		Webcam webcam = event.getWebcam();
		webcams.put( webcam.getDevice().getName(), webcam );
	}

	@Override
	public void webcamGone( WebcamDiscoveryEvent event ) {
		webcams.remove( event.getWebcam().getDevice().getName() );
	}

	/**
	 * Returns the webcam by name, null if not found.
	 * 
	 * @param name
	 *            webcam name
	 * @return webcam instance
	 */
	public Webcam getWebcam( String name, Dimension dimension ) {
		if (name == null) {
			return getWebcam( dimension );
		}

		Webcam webcam = webcams.get( name );
		openWebcam( webcam, dimension );
		return webcam;
	}

	/**
	 * Returns the first webcam.
	 */
	public Webcam getWebcam( Dimension dimension ) {
		if (webcams.size() == 0) {
			throw new IllegalStateException( "No webcams found" );
		}

		Webcam webcam = webcams.values().iterator().next();
		openWebcam( webcam, dimension );

		return webcam;
	}

	private Webcam openWebcam( Webcam webcam, Dimension dimension ) {
		// Once started another endpoint may want to change from low res to high
		// res, for example
		if (webcam.isOpen() && isStarted() && !dimension.equals( webcam.getViewSize() )) {
			webcam.close();
		}

		if (!webcam.isOpen() && dimension != null) {
			webcam.setCustomViewSizes( new Dimension[] { dimension } );
			webcam.setViewSize( dimension );
			webcam.open( true );
		} else if (!webcam.isOpen()) {
			webcam.open( true );
		}

		return webcam;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver( String driver ) {
		this.driver = driver;
	}

	public Map<String, Webcam> getWebcams() {
		return webcams;
	}

	public void setWebcams( Map<String, Webcam> webcams ) {
		this.webcams = webcams;
	}

}
