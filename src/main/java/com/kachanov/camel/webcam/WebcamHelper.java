package com.kachanov.camel.webcam;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.spi.ExceptionHandler;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionEvent;

/**
 * Commonly used Webcam utilities.
 */
public class WebcamHelper {

    /**
     * Creates an OutOnly exchange with the BufferedImage.
     */
    static Exchange createOutOnlyExchangeWithBodyAndHeaders(WebcamEndpoint endpoint, BufferedImage image) throws IOException {
        Exchange exchange = endpoint.createExchange();
        Message message = exchange.getIn();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, endpoint.getFormat(), output);
            message.setBody(new BufferedInputStream(new ByteArrayInputStream(output.toByteArray())));
            message.setHeader(Exchange.FILE_NAME, message.getMessageId() + "." + endpoint.getFormat());
        } 

        return exchange;
    }

    /**
     * Consume the java.awt.BufferedImage from the webcam, all params required.
     * 
     * @param image The image to process.
     * @param processor Processor that handles the exchange.
     * @param endpoint WebcamEndpoint receiving the exchange.
     */
    public static void consumeBufferedImage(BufferedImage image, Processor processor, WebcamEndpoint endpoint, ExceptionHandler exceptionHandler) {
    	Objects.requireNonNull( image);
    	Objects.requireNonNull( processor);
    	Objects.requireNonNull( endpoint);
        
        try {
            Exchange exchange = createOutOnlyExchangeWithBodyAndHeaders(endpoint, image);
            processor.process(exchange);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    /**
     * Consume the motion event from the webcam, all params required.
     * The event is stored in the header while the latest image is available as the body.
     *
     * @param motionEvent The motion event that triggered.
     * @param processor Processor that handles the exchange.
     * @param endpoint WebcamEndpoint receiving the exchange.
     */
    public static void consumeWebcamMotionEvent(WebcamMotionEvent motionEvent, Processor processor, WebcamEndpoint endpoint, ExceptionHandler exceptionHandler){
        Objects.requireNonNull( motionEvent);
        Objects.requireNonNull( processor);
        Objects.requireNonNull( endpoint);

        try {
            Exchange exchange = createOutOnlyExchangeWithBodyAndHeaders(endpoint, motionEvent.getCurrentImage());
            exchange.getIn().setHeader(WebcamConstants.WEBCAM_MOTION_EVENT_HEADER, motionEvent);
            processor.process(exchange);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    /**
     * Returns true if there's a webcam available, false otherwise.
     */
    public static boolean isWebcamPresent(){
        try {
            Webcam.getDefault(WebcamConstants.DEFAULT_WEBCAM_LOOKUP_TIMEOUT);
            return true;
        } catch (Throwable exception) {
            return false;
        }
    }

    /**
     * Close the default webcam.
     */
    public static void closeWebcam() throws TimeoutException {
        if (isWebcamPresent()) {
            Webcam.getDefault(WebcamConstants.DEFAULT_WEBCAM_LOOKUP_TIMEOUT).close();
        }
    }

}