package com.littlehorizons.erp;

import com.littlehorizons.erp.util.LoggerManager;
import com.littlehorizons.erp.view.MainMenuView;

/**
 * Entry point for the Little Horizons ERP System.
 */
public class Main {

    public static void main(String[] args) {
        try {
            LoggerManager.logInfo("Iniciando ERP Colegio Little Horizons...");
            MainMenuView menu = new MainMenuView();
            menu.start();
            LoggerManager.logInfo("Sesión finalizada correctamente.");
        } catch (Exception e) {
            LoggerManager.logError("Error crítico no controlado en la aplicación", e);
            System.err.println("\n[ERROR CRÍTICO] Se ha producido un error inesperado. Consulte error_log.txt.");
            e.printStackTrace();
        }
    }
}
