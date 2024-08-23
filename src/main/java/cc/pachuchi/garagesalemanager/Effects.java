package cc.pachuchi.garagesalemanager;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class Effects {

    /**
     * Creates a fade out effect on a label's text over a given duration.
     * @param label The label whose text should fade out.
     * @param durationSeconds The duration of the fade effect in seconds.
     */
    public static void fadeOutText(Label label, double durationSeconds) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), label);
        fadeTransition.setFromValue(1.0); // Start fully visible
        fadeTransition.setToValue(0.0); // Fade to completely transparent
        fadeTransition.setOnFinished(event -> label.setText("")); // Optionally clear the text after fade out
        fadeTransition.play();
    }
}
