package com.kachanov.camel.webcam;

import java.awt.Dimension;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultPollingEndpoint;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

@UriEndpoint(scheme = "webcam", title = "webcam", syntax = "webcam:name", label = "Webcam")
public class WebcamEndpoint extends DefaultPollingEndpoint {

	@UriParam(defaultValue = "PNG", description = "Capture format, one of [PNG,GIF,JPG]")
	private String format = "PNG";

	@UriParam(description = "Default resolution to use, overriding width and height")
	private String resolution;

	@UriParam(defaultValue = "320", description = "Width in pixels, must be supported by the webcam")
	private int width = 320;

	@UriParam(defaultValue = "240", description = "Height in pixels, must be supported by the webcam")
	private int height = 240;

	@UriParam(description = "Webcam device name, on Linux this may default to /dev/video0")
	private String deviceName;

	private Webcam webcam;

	public WebcamEndpoint( String uri, WebcamComponent component ) {
		super( uri, component );
	}

	@Override
	public Consumer createConsumer( Processor processor ) throws Exception {
		WebcamScheduledPollConsumer consumer = new WebcamScheduledPollConsumer( this, processor );

		configureConsumer( consumer );
		return consumer;
	}

	@Override
	public Producer createProducer() throws Exception {
		throw new UnsupportedOperationException( "Producer is not implemented" );
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public Webcam getWebcam() {
		// Allow tests to inject a webcam, but fallback to the component that
		// takes care of the management of webcams as they come and go
		return this.webcam != null ? this.webcam : ((WebcamComponent) getComponent()).getWebcam( getDeviceName(), getDefaultResolution() );
	}

	/**
	 * Returns the default resolution by name if provided, eg HD720, otherwise
	 * the width and height.
	 */
	private Dimension getDefaultResolution() {
		if (getResolution() != null) {
			WebcamResolution res = WebcamResolution.valueOf( getResolution() );
			return res.getSize();
		} else {
			return new Dimension( getWidth(), getHeight() );
		}
	}

	public String getFormat() {
		return format;
	}

	public void setFormat( String format ) {
		this.format = format;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution( String resolution ) {
		this.resolution = resolution;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth( int width ) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight( int height ) {
		this.height = height;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName( String name ) {
		this.deviceName = name;
	}

}
