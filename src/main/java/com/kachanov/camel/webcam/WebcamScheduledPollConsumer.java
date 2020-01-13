package com.kachanov.camel.webcam;

import java.awt.image.BufferedImage;

import org.apache.camel.Processor;
import org.apache.camel.support.ScheduledPollConsumer;

import com.github.sarxos.webcam.Webcam;

/**
 * The scheduled Webcam consumer.
 */
public class WebcamScheduledPollConsumer extends ScheduledPollConsumer {

	public WebcamScheduledPollConsumer( WebcamEndpoint endpoint, Processor processor ) {
		super( endpoint, processor );
	}

	@Override
	protected int poll() throws Exception {
        if (getEndpoint().getWebcam() != null && getEndpoint().getWebcam().isOpen()) {
            Webcam webcam = getEndpoint().getWebcam();
            BufferedImage image = webcam.getImage();
            if (image != null) {
                WebcamHelper.consumeBufferedImage(image, getProcessor(), getEndpoint(), getExceptionHandler());
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
	}

	@Override
	public WebcamEndpoint getEndpoint() {
		return (WebcamEndpoint) super.getEndpoint();
	}

}