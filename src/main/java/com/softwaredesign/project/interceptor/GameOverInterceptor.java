package com.softwaredesign.project.interceptor;

/**
 * Interceptor that checks for game over conditions and handles shutdown
 */
public class GameOverInterceptor implements Interceptor {
    @Override
    public void intercept(InterceptorContext context) {
        boolean allEmpty = context.getStockLevels().values().stream()
                .allMatch(quantity -> quantity <= 0);
        
        if (allEmpty) {
            context.setGameOver(true);
            displayGameOver();
            System.exit(0);
        }
    }

    private void displayGameOver() {
        System.out.println("\n" +
            "  _____                         ____                 \n" +
            " / ____|                       / __ \\                \n" +
            "| |  __  __ _ _ __ ___   ___ | |  | |_   _____ _ __\n" +
            "| | |_ |/ _` | '_ ` _ \\ / _ \\| |  | \\ \\ / / _ \\ '__|\n" +
            "| |__| | (_| | | | | | |  __/| |__| |\\ V /  __/ |   \n" +
            " \\_____|\\__,_|_| |_| |_|\\___| \\____/  \\_/ \\___|_|   \n" +
            "\nAll ingredients have been depleted! Restaurant is closing.\n");
    }
}
