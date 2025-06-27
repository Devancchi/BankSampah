package main;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.io.InputStream;
import java.util.Properties;

public class AppInitializer {

    public static void setupLookAndFeel() {
        try {
            // Muat tema kustom dari flatlaf.properties
            Properties properties = new Properties();
            InputStream stream = AppInitializer.class.getResourceAsStream("/themes/flatlaf.properties");

            if (stream != null) {
                properties.load(stream);
                stream.close();

                // Gunakan FlatLaf dengan properties kustom
                FlatLaf.setup(new FlatLightLaf() {
                    @Override
                    public Properties getAdditionalDefaults() {
                        return properties;
                    }
                });

                System.out.println("Successfully loaded custom FlatLaf theme");
            } else {
                // Fallback jika file tema tidak ditemukan
                System.err.println("Warning: Could not find custom theme file. Using default theme.");
                FlatLightLaf.setup();
            }

        } catch (Exception e) {
            System.err.println("Failed to initialize custom theme: " + e.getMessage());
            e.printStackTrace();

            // Fallback ke tema default jika terjadi error
            try {
                FlatLightLaf.setup();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
