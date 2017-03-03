import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Created by olivier on 22/02/2017.
 */
public class TestBinding extends Application {

    @Override
    public void start(Stage primaryStage) {

        JfxUiManager uiManager = new JfxUiManager();

//        JfxInstanceUI<String> ui = uiManager.buildInstanceUI(String.class);
        JfxInstanceUI<StringContainer> ui = uiManager.buildInstanceUI(StringContainer.class);

        StringContainer sc = new StringContainer();
        sc.setText("TEST");
        ui.bind(sc);

        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }

}
