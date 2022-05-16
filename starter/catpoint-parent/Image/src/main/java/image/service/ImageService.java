package image.service;

import java.awt.image.BufferedImage;

public interface ImageService {
    //Interface for the Image Service
    boolean imageContainsCat(BufferedImage image, float confidenceThreshhold);

}
