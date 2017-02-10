/**
 * Interface to listen for shape change
 * notifications
 */
public interface ModelListener {

    /**
     * This method acts as a notification
     * for when the argument DShapeModel
     * has been changed in some way
     * @param model the DShapeModel to listen to
     *              for any changes
     */
    void modelChanged(DShapeModel model);
}
