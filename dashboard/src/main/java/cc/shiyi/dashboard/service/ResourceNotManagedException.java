package cc.shiyi.dashboard.service;

/**
 * Thrown when attempting to delete a K8S resource not created through this platform.
 */
public class ResourceNotManagedException extends RuntimeException {
    public ResourceNotManagedException(String message) {
        super(message);
    }
}
